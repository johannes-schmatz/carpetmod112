package carpet.helpers;

import carpet.mixin.optimizedTileEntities.WorldMixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

/**
 * This class contains the code responsible for optimizing tile entities by making them sleep until they receive an update.
 * It contains an interface that all optimized tile entities must implement, and the code responsible for propagating the updates.
 * @author PallaPalla
 */
public class BlockEntityOptimizer {

	/**
	 * All optimized tile entities must implement this interface so that the world object knows it should wake them up.
	 * A tile entity that implements this interface should set a sleeping flag if it becomes unused.
	 * This sleeping flag should cause it to skip all or part of its update() method and be reset by wakeUp().
	 */
	@FunctionalInterface
	public interface LazyBlockEntity {
		/**
		 * CARPET-optimizedTileEntities: Wakes up the tile entity so it updates again. Called upon receiving a comparator update in
		 * {@linkplain net.minecraft.world.World#updateNeighborComparators(net.minecraft.util.math.BlockPos, net.minecraft.block.Block)}
		 * {@linkplain WorldMixin#onComparatorUpdate(BlockPos, Block, CallbackInfo)}
		 */
		void wakeUp();
	}

	// The method called by the world object when a comparator update happens. Wakes up the tile entity causing it, and nearby hoppers.
	// Some code here is copied from world.updateComparatorOutputLevel() to perform the vanilla comparator updates.
	public static void updateComparatorsAndLazyTileEntities(World world, BlockPos pos, Block block) {
		// Wake up the tile entity that caused the comparator update
		if (block.hasBlockEntity()) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof LazyBlockEntity) {
				((LazyBlockEntity) blockEntity).wakeUp();
			}
		}

		// Perform the usual comparator updates horizontally
		// Additionally iterate over the up and down directions, since hoppers can also be vertical
		for (Direction direction : Direction.values()) {
			BlockPos blockPos = pos.offset(direction);
			boolean horizontal = direction.getAxis() != Axis.Y;

			if (world.isChunkLoaded(blockPos)) {
				BlockState state = world.getBlockState(blockPos);

				// Check for comparators like in vanilla. This check is only performed horizontally, as comparators are only horizontal
				if (horizontal && Blocks.COMPARATOR.isSameDiode(state)) {
					state.neighborChanged(world, blockPos, block, pos);
				} else if (horizontal && state.isConductor()) {
					BlockPos blockPos1 = blockPos.offset(direction);
					BlockState state1 = world.getBlockState(blockPos1);

					if (Blocks.COMPARATOR.isSameDiode(state1)) {
						state1.neighborChanged(world, blockPos1, block, pos);
					}
				}

				// Wake up nearby hoppers. Only hoppers under the block (pulling) or pointing into it (pushing) should be woken up
				else if (state.getBlock() == Blocks.HOPPER) {
					BlockEntity blockEntity = world.getBlockEntity(blockPos);
					if ((direction == Direction.DOWN || direction == HopperBlock.getFacingFromMetadata(blockEntity.getBlockMetadata()).getOpposite()) &&
							blockEntity instanceof LazyBlockEntity) {
						((LazyBlockEntity) blockEntity).wakeUp();
					}
				}
			}
		}
	}
}
