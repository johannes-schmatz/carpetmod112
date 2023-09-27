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
		NbtList nbttaglist = nbt.getList("blockEvents", 10);
		for (int i = 0; i < nbttaglist.size(); ++i) {
			NbtCompound nbttagcompound = nbttaglist.getCompound(i);
			BlockEvent blockeventdata = new BlockEvent(
					new BlockPos(nbttagcompound.getInt("X"), nbttagcompound.getInt("Y"), nbttagcompound.getInt("Z")),
					Block.byId(nbttagcompound.getInt("B") & 4095),
					nbttagcompound.getInt("ID"),
					nbttagcompound.getInt("P")
			);
			list.add(blockeventdata);
		}
	}

	@Override
	public NbtCompound writeNbt(NbtCompound compound) {
		NbtList nbttaglist = new NbtList();
		if (CarpetSettings.blockEventSerializer) {
			for (BlockEvent blockeventdata : getBlockEventQueue(world)) {
				NbtCompound nbttagcompound = new NbtCompound();
				nbttagcompound.putInt("X", blockeventdata.getPos().getX());
				nbttagcompound.putInt("Y", blockeventdata.getPos().getY());
				nbttagcompound.putInt("Z", blockeventdata.getPos().getZ());
				nbttagcompound.putInt("B", Block.getId(blockeventdata.getBlock()) & 4095);
				nbttagcompound.putInt("ID", blockeventdata.getType());
				nbttagcompound.putInt("P", blockeventdata.getData());
				nbttaglist.add(nbttagcompound);
			}
		}
		compound.put("blockEvents", nbttaglist);
		return compound;
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
