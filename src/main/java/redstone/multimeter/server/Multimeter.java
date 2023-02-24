package redstone.multimeter.server;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.world.BlockAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.ScheduledTick;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import redstone.multimeter.block.Meterable;
import redstone.multimeter.block.PowerSource;
import redstone.multimeter.common.DimPos;
import redstone.multimeter.common.meter.Meter;
import redstone.multimeter.common.meter.MeterGroup;
import redstone.multimeter.common.meter.MeterProperties;
import redstone.multimeter.common.meter.MeterProperties.MutableMeterProperties;
import redstone.multimeter.common.meter.event.EventType;
import redstone.multimeter.common.network.packets.ClearMeterGroupPacket;
import redstone.multimeter.common.network.packets.MeterGroupDefaultPacket;
import redstone.multimeter.common.network.packets.MeterGroupRefreshPacket;
import redstone.multimeter.common.network.packets.MeterGroupSubscriptionPacket;
import redstone.multimeter.server.meter.ServerMeterGroup;
import redstone.multimeter.server.meter.ServerMeterPropertiesManager;
import redstone.multimeter.server.meter.event.MeterEventPredicate;
import redstone.multimeter.server.meter.event.MeterEventSupplier;
import redstone.multimeter.server.option.Options;
import redstone.multimeter.server.option.OptionsManager;

public class Multimeter {
	
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);
	
	private final MultimeterServer server;
	private final Map<String, ServerMeterGroup> meterGroups;
	private final Map<UUID, ServerMeterGroup> subscriptions;
	private final Set<ServerMeterGroup> activeMeterGroups;
	private final Set<ServerMeterGroup> idleMeterGroups;
	private final ServerMeterPropertiesManager meterPropertiesManager;
	
	public Options options;
	
	public Multimeter(MultimeterServer server) {
		this.server = server;
		this.meterGroups = new LinkedHashMap<>();
		this.subscriptions = new HashMap<>();
		this.activeMeterGroups = new HashSet<>();
		this.idleMeterGroups = new HashSet<>();
		this.meterPropertiesManager = new ServerMeterPropertiesManager(this);
		
		reloadOptions();
	}
	
	public MultimeterServer getMultimeterServer() {
		return server;
	}
	
	public Collection<ServerMeterGroup> getMeterGroups() {
		return Collections.unmodifiableCollection(meterGroups.values());
	}
	
	public ServerMeterGroup getMeterGroup(String name) {
		return meterGroups.get(name);
	}
	
	public boolean hasMeterGroup(String name) {
		return meterGroups.containsKey(name);
	}
	
	public ServerMeterGroup getSubscription(ServerPlayerEntity player) {
		return subscriptions.get(player.getUuid());
	}
	
	public boolean hasSubscription(ServerPlayerEntity player) {
		return subscriptions.containsKey(player.getUuid());
	}
	
	public boolean isOwnerOfSubscription(ServerPlayerEntity player) {
		ServerMeterGroup meterGroup = getSubscription(player);
		return meterGroup != null && meterGroup.isOwnedBy(player);
	}
	
	public void reloadOptions() {
		if (server.isDedicated()) {
			options = OptionsManager.load(server.getConfigFolder());
		} else {
			options = new Options();
		}
	}
	
	public void tickStart(boolean paused) {
		if (!paused) {
			removeIdleMeterGroups();
			
			for (ServerMeterGroup meterGroup : meterGroups.values()) {
				meterGroup.tick();
			}
		}
	}
	
	public void tickEnd(boolean paused) {
		broadcastMeterUpdates();
		
		if (!paused) {
			broadcastMeterLogs();
		}
	}
	
	private void removeIdleMeterGroups() {
		Iterator<ServerMeterGroup> it = idleMeterGroups.iterator();
		
		while (it.hasNext()) {
			ServerMeterGroup meterGroup = it.next();
			
			if (tryRemoveMeterGroup(meterGroup)) {
				it.remove();
			}
		}
	}
	
	private boolean tryRemoveMeterGroup(ServerMeterGroup meterGroup) {
		if (meterGroup.hasMeters() && !meterGroup.isPastIdleTimeLimit()) {
			return false;
		}
		
		meterGroups.remove(meterGroup.getName(), meterGroup);
		
		if (meterGroup.hasMeters()) {
			notifyOwnerOfRemoval(meterGroup);
		}
		
		return true;
	}
	
	private void notifyOwnerOfRemoval(ServerMeterGroup meterGroup) {
		UUID ownerUUID = meterGroup.getOwner();
		ServerPlayerEntity owner = server.getPlayer(ownerUUID);
		
		if (owner != null) {
			Text message = new LiteralText(String.format("One of your meter groups, \'%s\', was idle for more than %d ticks and has been removed.",
					meterGroup.getName(), options.meter_group.max_idle_time));
			server.sendMessage(owner, message, false);
		}
	}
	
	private void broadcastMeterUpdates() {
		for (ServerMeterGroup meterGroup : meterGroups.values()) {
			meterGroup.flushUpdates();
		}
	}
	
	private void broadcastMeterLogs() {
		for (ServerMeterGroup meterGroup : meterGroups.values()) {
			meterGroup.getLogManager().flushLogs();
		}
	}
	
	public void onPlayerJoin(ServerPlayerEntity player) {
		
	}
	
	public void onPlayerLeave(ServerPlayerEntity player) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			removeSubscriberFromMeterGroup(meterGroup, player);
		}
	}
	
	public void addMeter(ServerPlayerEntity player, MeterProperties properties) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			if (meterGroup.isPastMeterLimit()) {
				Text message = new LiteralText(String.format("meter limit (%d) reached!", options.meter_group.meter_limit));
				server.sendMessage(player, message, true);
			} else if (!addMeter(meterGroup, properties)) {
				refreshMeterGroup(meterGroup, player);
			}
		}
	}
	
	public boolean addMeter(ServerMeterGroup meterGroup, MeterProperties meterProperties) {
		MutableMeterProperties properties = meterProperties.toMutable();
		
		if (!meterPropertiesManager.validate(properties) || !meterGroup.addMeter(properties)) {
			return false;
		}
		
		DimPos pos = properties.getPos();
		World world = server.getWorldOf(pos);
		BlockPos blockPos = pos.getBlockPos();
		BlockState state = world.getBlockState(blockPos);
		
		logPowered(world, blockPos, state);
		logActive(world, blockPos, state);
		
		return true;
	}
	
	public void removeMeter(ServerPlayerEntity player, long id) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null && !meterGroup.removeMeter(id)) {
			refreshMeterGroup(meterGroup, player);
		}
	}
	
	public void updateMeter(ServerPlayerEntity player, long id, MeterProperties newProperties) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null && !meterGroup.updateMeter(id, newProperties)) {
			refreshMeterGroup(meterGroup, player);
		}
	}
	
	public void clearMeterGroup(ServerPlayerEntity player) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			clearMeterGroup(meterGroup);
		}
	}
	
	public void clearMeterGroup(ServerMeterGroup meterGroup) {
		meterGroup.clear();
		
		ClearMeterGroupPacket packet = new ClearMeterGroupPacket();
		server.getPacketHandler().sendToSubscribers(packet, meterGroup);
	}
	
	public void createMeterGroup(ServerPlayerEntity player, String name) {
		if (!MeterGroup.isValidName(name) || meterGroups.containsKey(name)) {
			return;
		}
		
		ServerMeterGroup meterGroup = new ServerMeterGroup(this, name, player);
		meterGroups.put(name, meterGroup);
		
		subscribeToMeterGroup(meterGroup, player);
	}
	
	public void subscribeToMeterGroup(ServerMeterGroup meterGroup, ServerPlayerEntity player) {
		ServerMeterGroup prevSubscription = getSubscription(player);
		
		if (prevSubscription == meterGroup) {
			refreshMeterGroup(meterGroup, player);
		} else {
			if (prevSubscription != null) {
				removeSubscriberFromMeterGroup(prevSubscription, player);
			}
			
			addSubscriberToMeterGroup(meterGroup, player);
			onSubscriptionChanged(player, prevSubscription, meterGroup);
		}
	}
	
	public void subscribeToDefaultMeterGroup(ServerPlayerEntity player) {
		MeterGroupDefaultPacket packet = new MeterGroupDefaultPacket();
		server.getPacketHandler().sendToPlayer(packet, player);
	}
	
	private void addSubscriberToMeterGroup(ServerMeterGroup meterGroup, ServerPlayerEntity player) {
		UUID playerUUID = player.getUuid();
		
		subscriptions.put(playerUUID, meterGroup);
		meterGroup.addSubscriber(playerUUID);
		
		if (meterGroup.updateIdleState()) {
			activeMeterGroups.add(meterGroup);
			idleMeterGroups.remove(meterGroup);
		}
	}
	
	public void unsubscribeFromMeterGroup(ServerPlayerEntity player) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			unsubscribeFromMeterGroup(meterGroup, player);
		}
	}
	
	public void unsubscribeFromMeterGroup(ServerMeterGroup meterGroup, ServerPlayerEntity player) {
		if (meterGroup.hasSubscriber(player)) {
			removeSubscriberFromMeterGroup(meterGroup, player);
			onSubscriptionChanged(player, meterGroup, null);
		}
	}
	
	private void removeSubscriberFromMeterGroup(ServerMeterGroup meterGroup, ServerPlayerEntity player) {
		UUID playerUUID = player.getUuid();
		
		subscriptions.remove(playerUUID, meterGroup);
		meterGroup.removeSubscriber(playerUUID);
		
		if (meterGroup.updateIdleState()) {
			activeMeterGroups.remove(meterGroup);
			idleMeterGroups.add(meterGroup);
		}
	}
	
	private void onSubscriptionChanged(ServerPlayerEntity player, ServerMeterGroup prevSubscription, ServerMeterGroup newSubscription) {
		MeterGroupSubscriptionPacket packet;
		
		if (newSubscription == null) {
			packet = new MeterGroupSubscriptionPacket(prevSubscription.getName(), false);
		} else {
			packet = new MeterGroupSubscriptionPacket(newSubscription.getName(), true);
		}
		
		server.getPacketHandler().sendToPlayer(packet, player);
		//server.getPlayerManager().updatePermissionLevel(player); // TODO rsmm
	}
	
	public void clearMembersOfMeterGroup(ServerMeterGroup meterGroup) {
		for (UUID playerUUID : meterGroup.getMembers()) {
			removeMemberFromMeterGroup(meterGroup, playerUUID);
		}
	}
	
	public void addMemberToMeterGroup(ServerMeterGroup meterGroup, UUID playerUUID) {
		if (meterGroup.hasMember(playerUUID) || meterGroup.isOwnedBy(playerUUID)) {
			return;
		}
		
		ServerPlayerEntity player = server.getPlayer(playerUUID);
		
		if (player == null) {
			return;
		}
		
		meterGroup.addMember(playerUUID);
		
		Text message = new LiteralText("").
			append(new LiteralText(String.format("You have been invited to meter group \'%s\' - click ", meterGroup.getName()))).
			append(new LiteralText("[here]").setStyle(new Style().
					setFormatting(Formatting.GREEN).
					setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(String.format("Subscribe to meter group \'%s\'", meterGroup.getName())))).
					setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/metergroup subscribe %s", meterGroup.getName())))
			)).
			append(new LiteralText(" to subscribe to it."));
		server.sendMessage(player, message, false);
	}
	
	public void removeMemberFromMeterGroup(ServerMeterGroup meterGroup, UUID playerUUID) {
		if (!meterGroup.hasMember(playerUUID)) {
			return;
		}
		
		meterGroup.removeMember(playerUUID);
		
		if (meterGroup.isPrivate()) {
			ServerPlayerEntity player = server.getPlayer(playerUUID);
			
			if (player != null && meterGroup.hasSubscriber(playerUUID)) {
				unsubscribeFromMeterGroup(meterGroup, player);
				
				Text message = new LiteralText(String.format("The owner of meter group \'%s\' has removed you as a member!", meterGroup.getName()));
				server.sendMessage(player, message, false);
			}
		}
	}
	
	public void refreshMeterGroup(ServerPlayerEntity player) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			refreshMeterGroup(meterGroup, player);
		}
	}
	
	private void refreshMeterGroup(ServerMeterGroup meterGroup, ServerPlayerEntity player) {
		MeterGroupRefreshPacket packet = new MeterGroupRefreshPacket(meterGroup);
		server.getPacketHandler().sendToPlayer(packet, player);
	}
	
	public void teleportToMeter(ServerPlayerEntity player, long id) {
		if (!options.meter.allow_teleports) {
			Text message = new LiteralText("This server does not allow meter teleporting!");
			server.sendMessage(player, message, false);
			
			return;
		}
		
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			Meter meter = meterGroup.getMeter(id);
			
			if (meter != null) {
				DimPos pos = meter.getPos();
				ServerWorld newWorld = server.getWorldOf(pos);
				
				if (newWorld != null) {
					BlockPos blockPos = pos.getBlockPos();
					
					double newX = blockPos.getX() + 0.5D;
					double newY = blockPos.getY();
					double newZ = blockPos.getZ() + 0.5D;
					float yaw = player.yaw;
					float pitch = player.pitch;
					
					player.changeDimension(newWorld.dimension.getDimensionType().getId());
					//player.connection.setPlayerLocation(newX, newY, newZ, yaw, pitch); // TODO rsmm
					
					Text text = new LiteralText(String.format("Teleported to meter \"%s\"", meter.getName()));
					server.sendMessage(player, text, false);
				}
			}
		}
	}
	
	public void onBlockChange(World world, BlockPos pos, BlockState oldState, BlockState newState) {
		Block oldBlock = oldState.getBlock();
		Block newBlock = newState.getBlock();
		
		//if (oldBlock == newBlock && newBlock.isPowerSource() && ((PowerSource)newBlock).logPowerChangeOnStateChange()) { // TODO rsmm
		//	logPowerChange(world, pos, oldState, newState);
		//}
		
		//boolean wasMeterable = oldBlock.isMeterable(); // TODO rsmm
		//boolean isMeterable = newBlock.isMeterable();
		
		//if (wasMeterable || isMeterable) { // TODO rsmm
		//	logActive(world, pos, newState);
		//}
	}
	
	public void logPowered(World world, BlockPos pos, boolean powered) {
		tryLogEvent(world, pos, EventType.POWERED, powered ? 1 : 0, (meterGroup, meter, event) -> meter.setPowered(powered));
	}
	
	public void logPowered(World world, BlockPos pos, BlockState state) {
		tryLogEvent(world, pos, (meterGroup, meter, event) -> meter.setPowered(event.getMetadata() != 0), new MeterEventSupplier(EventType.POWERED, () -> {
			//return state.getBlock().isPowered(world, pos, state) ? 1 : 0; // TODO rsmm
			return 1;
		}));
	}
	
	public void logActive(World world, BlockPos pos, boolean active) {
		tryLogEvent(world, pos, EventType.ACTIVE, active ? 1 : 0, (meterGroup, meter, event) -> meter.setActive(active));
	}
	
	public void logActive(World world, BlockPos pos, BlockState state) {
		tryLogEvent(world, pos, (meterGroup, meter, event) -> meter.setActive(event.getMetadata() != 0), new MeterEventSupplier(EventType.ACTIVE, () -> {
			Block block = state.getBlock();
			//return block.isMeterable() && ((Meterable)block).isActive(world, pos, state) ? 1 : 0; // TODO rsmm
			return 1;
		}));
	}
	
	public void logMoved(World world, BlockPos blockPos, Direction dir) {
		tryLogEvent(world, blockPos, EventType.MOVED, dir.getId());
	}
	
	public void moveMeters(World world, BlockPos blockPos, Direction dir) {
		DimPos pos = new DimPos(world, blockPos);
		
		for (ServerMeterGroup meterGroup : activeMeterGroups) {
			meterGroup.tryMoveMeter(pos, dir);
		}
	}
	
	public void logPowerChange(World world, BlockPos pos, int oldPower, int newPower) {
		if (oldPower != newPower) {
			tryLogEvent(world, pos, EventType.POWER_CHANGE, (oldPower << 8) | newPower);
		}
	}
	
	public void logPowerChange(World world, BlockPos pos, BlockState oldState, BlockState newState) {
		tryLogEvent(world, pos, (meterGroup, meter, event) -> {
			int data = event.getMetadata();
			int oldPower = (data >> 8) & 0xFF;
			int newPower =  data       & 0xFF;
			
			return oldPower != newPower;
		}, new MeterEventSupplier(EventType.POWER_CHANGE, () -> {
			PowerSource block = (PowerSource)newState.getBlock();
			int oldPower = block.getPowerLevel(world, pos, oldState);
			int newPower = block.getPowerLevel(world, pos, newState);
			
			return (oldPower << 8) | newPower;
		}));
	}
	
	public void logRandomTick(World world, BlockPos pos) {
		tryLogEvent(world, pos, EventType.RANDOM_TICK, 0);
	}
	
	public void logScheduledTick(World world, ScheduledTick scheduledTick) {
		tryLogEvent(world, scheduledTick.pos, EventType.SCHEDULED_TICK, scheduledTick.priority);
	}
	
	public void logBlockEvent(World world, BlockAction blockEvent, int depth) {
		tryLogEvent(world, blockEvent.getPos(), EventType.BLOCK_EVENT, (depth << 4) | blockEvent.getType());
	}
	
	public void logEntityTick(World world, Entity entity) {
		tryLogEvent(world, entity.getBlockPos(), EventType.ENTITY_TICK, 0);
	}
	
	public void logBlockEntityTick(World world, BlockEntity blockEntity) {
		tryLogEvent(world, blockEntity.getPos(), EventType.BLOCK_ENTITY_TICK, 0);
	}
	
	public void logBlockUpdate(World world, BlockPos pos) {
		tryLogEvent(world, pos, EventType.BLOCK_UPDATE, 0);
	}
	
	public void logComparatorUpdate(World world, BlockPos pos) {
		tryLogEvent(world, pos, EventType.COMPARATOR_UPDATE, 0);
	}
	
	public void logShapeUpdate(World world, BlockPos pos, Direction dir) {
		tryLogEvent(world, pos, EventType.SHAPE_UPDATE, dir.getId());
	}
	
	public void logObserverUpdate(World world, BlockPos pos) {
		tryLogEvent(world, pos, EventType.OBSERVER_UPDATE, 0);
	}
	
	public void logInteractBlock(World world, BlockPos pos) {
		tryLogEvent(world, pos, EventType.INTERACT_BLOCK, 0);
	}
	
	private void tryLogEvent(World world, BlockPos pos, EventType type, int data) {
		tryLogEvent(world, pos, type, data, (meterGroup, meter, event) -> true);
	}
	
	private void tryLogEvent(World world, BlockPos pos, EventType type, int data, MeterEventPredicate predicate) {
		tryLogEvent(world, pos, predicate, new MeterEventSupplier(type, () -> data));
	}
	
	private void tryLogEvent(World world, BlockPos blockPos, MeterEventPredicate predicate, MeterEventSupplier supplier) {
		if (options.hasEventType(supplier.type())) {
			DimPos pos = new DimPos(world, blockPos);
			
			for (ServerMeterGroup meterGroup : activeMeterGroups) {
				meterGroup.tryLogEvent(pos, predicate, supplier);
			}
		}
	}
	
	static {
		
		NUMBER_FORMAT.setGroupingUsed(false);
		
	}
}
