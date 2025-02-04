package carpet.helpers;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashSet;

public final class PistonHelper {
	private static ThreadLocal<HashSet<BlockPos>> dupeFixLocations = new ThreadLocal<>();

	private PistonHelper() {
	}

	// Movable Tile entity fix CARPET-2No2Name
	public static boolean isPushableTileEntityBlock(Block block) {
		//Making PISTON_EXTENSION (BlockPistonMoving) pushable would not work as its createNewTileEntity()-method returns null
		return block != Blocks.ENDER_CHEST
				&& block != Blocks.ENCHANTING_TABLE
				&& block != Blocks.END_GATEWAY
				&& block != Blocks.END_PORTAL
				&& block != Blocks.MOB_SPAWNER
				&& block != Blocks.MOVING_BLOCK;
	}

	// Added method for checking if block is being pushed for duping fixes CARPET-XCOM
	public static boolean isBeingPushed(BlockPos pos) {
		HashSet<BlockPos> locations = dupeFixLocations.get();
		return locations != null && locations.contains(pos);
	}

	public static void registerPushed(Collection<BlockPos> blocks) {
		dupeFixLocations.set(new HashSet<>(blocks));
	}

	public static void finishPush() {
		dupeFixLocations.set(null);
	}
}
