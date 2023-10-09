package carpet.helpers;

import carpet.mixin.accessors.RedstoneWireBlockAccessor;

import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class RedstoneOreRedirectHelper {
	public static boolean canConnectToCM(BlockState blockState, @Nullable Direction side) {
		Block block = blockState.getBlock();

		if (block == Blocks.REDSTONE_WIRE) {
			return true;
		} else if (Blocks.REPEATER.isSameDiode(blockState)) {
			Direction direction = blockState.get(HorizontalFacingBlock.FACING);
			return direction == side || direction.getOpposite() == side;
		} else if (Blocks.OBSERVER == blockState.getBlock()) {
			return side == blockState.get(FacingBlock.FACING);
		} else {
			return (blockState.isSignalSource() || block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE) && side != null;
		}
	}

	public static int getWeakPowerCM(RedstoneWireBlock wire, BlockState blockState, WorldView blockAccess, BlockPos pos, Direction side) {
		BlockState iblockstate = blockAccess.getBlockState(pos.offset(side.getOpposite()));
		if (((RedstoneWireBlockAccessor) wire).getWiresGivePower()) {
			int i = blockState.get(RedstoneWireBlock.POWER);

			if (i == 0) {
				return 0;
			} else if (side == Direction.UP) {
				return i;
			}
			// [CM] Redstone ore redirects dust - give power if its redstone ore block
			else if (side.getAxis().isHorizontal() && ((iblockstate.getBlock() == Blocks.REDSTONE_ORE || iblockstate.getBlock() == Blocks.LIT_REDSTONE_ORE))) {
				return i;
			} else {
				EnumSet<Direction> enumset = EnumSet.<Direction>noneOf(Direction.class);

				for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
					if (((RedstoneWireBlockAccessor) wire).invokeCouldConnectTo(blockAccess, pos, enumfacing)) {
						enumset.add(enumfacing);
					}
				}

				if (side.getAxis().isHorizontal() && enumset.isEmpty()) {
					return i;
				} else if (enumset.contains(side) && !enumset.contains(side.counterClockwiseY()) && !enumset.contains(side.clockwiseY())) {
					return i;
				} else {
					return 0;
				}
			}
		} else {
			return 0;
		}
	}

}
