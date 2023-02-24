package carpet.mixin.redstoneMultimeter;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import redstone.multimeter.block.MeterableBlock;
import redstone.multimeter.block.PowerSource;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin implements MeterableBlock, PowerSource {

	@Shadow @Final public static IntProperty POWER;

	@Override
	public boolean logPoweredOnBlockUpdate() {
		return false;
	}

	// This method is only called on blocks where 'logPoweredOnBlockUpdate'
	// returns 'true', so it does not really matter that a potentially
	// incorrect value is returned.
	@Override
	public boolean isPowered(World world, BlockPos pos, BlockState state) {
		return state.get(POWER) > MIN_POWER;
	}

	@Override
	public boolean isActive(World world, BlockPos pos, BlockState state) {
		return state.get(POWER) > MIN_POWER;
	}

	@Override
	public int getPowerLevel(World world, BlockPos pos, BlockState state) {
		return state.get(POWER);
	}

	@Inject(
			method = "update(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/block/RedstoneWireBlock;POWER:Lnet/minecraft/state/property/IntProperty;",
					ordinal = 1,
					shift = At.Shift.BY,
					by = -4
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void logPowered(World world, BlockPos pos1, BlockPos pos2, BlockState state, CallbackInfoReturnable<BlockState> cir, BlockState lv, int i, int j) {
		logPowered(world, pos1, j > MIN_POWER);
	}
}
