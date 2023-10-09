package carpet.helpers;

import carpet.CarpetSettings;
import carpet.mixin.accessors.ServerWorldAccessor;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.saved.SavedData;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ScheduledBlockEventSerializer extends SavedData {
	private final ArrayList<BlockEvent> list = new ArrayList<>();
	private ServerWorld world;

	public ScheduledBlockEventSerializer() {
		this("blockEvents");
	}

	public ScheduledBlockEventSerializer(String name) {
		super(name);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		NbtList blockEvents = nbt.getList("blockEvents", 10);
		for (int i = 0; i < blockEvents.size(); ++i) {
			NbtCompound blockEventCompound = blockEvents.getCompound(i);
			BlockEvent blockEvent = new BlockEvent(
					new BlockPos(blockEventCompound.getInt("X"), blockEventCompound.getInt("Y"), blockEventCompound.getInt("Z")),
					Block.byId(blockEventCompound.getInt("B") & 4095),
					blockEventCompound.getInt("ID"),
					blockEventCompound.getInt("P")
			);
			list.add(blockEvent);
		}
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		NbtList blockEvents = new NbtList();
		if (CarpetSettings.blockEventSerializer) {
			for (BlockEvent blockEvent : getBlockEventQueue(world)) {
				NbtCompound blockEventCompound = new NbtCompound();
				blockEventCompound.putInt("X", blockEvent.getPos().getX());
				blockEventCompound.putInt("Y", blockEvent.getPos().getY());
				blockEventCompound.putInt("Z", blockEvent.getPos().getZ());
				blockEventCompound.putInt("B", Block.getId(blockEvent.getBlock()) & 4095);
				blockEventCompound.putInt("ID", blockEvent.getType());
				blockEventCompound.putInt("P", blockEvent.getData());
				blockEvents.add(blockEventCompound);
			}
		}
		nbt.put("blockEvents", blockEvents);
		return nbt;
	}

	public void setBlockEvents(ServerWorld world) {
		this.world = world;
		getBlockEventQueue(world).addAll(list);
	}

	private static ArrayList<BlockEvent> getBlockEventQueue(ServerWorld world) {
		return BlockEventQueueGetter.getBlockEventQueue(world)[((ServerWorldAccessor) world).getBlockEventCacheIndex()];
	}

	/**
	 * WorldServer.ServerBlockEventList is package private and I don't know of a way to get it with mixins so we have to find it with reflection. Inner class to
	 * lazy-initialize it.
	 */
	private static final class BlockEventQueueGetter {
		static MethodHandle handle = getMethodHandle();

		static ArrayList<BlockEvent>[] getBlockEventQueue(ServerWorld world) {
			try {
				//noinspection unchecked
				return (ArrayList<BlockEvent>[]) handle.invoke(world);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		/**
		 * Searches for the {@link MethodHandle} accessing the field {@link ServerWorld#field_2815}
		 *
		 * @return a {@link MethodHandle} of type {@code ()[Lnet/minecraft/world/ServerWorld$BlockActionList;}
		 */
		private static MethodHandle getMethodHandle() {
			// TODO: just use mixin?
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			Class<?> worldServerCls = ServerWorld.class;
			for (Field f : worldServerCls.getDeclaredFields()) {
				Class<?> type = f.getType();
				// We're looking for ServerWorld.BlockActionList[] which is an array
				if (!type.isArray()) continue;
				Class<?> baseCls = type.getComponentType();
				if (baseCls.getEnclosingClass() != worldServerCls) continue;
				// ServerWorld.BlockActionList is the only inner class of ServerWorld so this should be the field we want
				try {
					f.setAccessible(true);
					return lookup.unreflectGetter(f);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			throw new IllegalStateException("Could not get block event queue field");
		}
	}
}
