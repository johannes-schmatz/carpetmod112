package carpet.mixin.redstoneMultimeter;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import redstone.multimeter.block.MeterableBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(TrapdoorBlock.class)
public class TrapdoorBlockMixin implements MeterableBlock {
	@Shadow @Final public static BooleanProperty OPEN;

	@Override
	public boolean logPoweredOnBlockUpdate() {
		return false;
	}

	@Override
	public boolean isActive(World world, BlockPos pos, BlockState state) {
		return state.get(OPEN);
	}

	@Inject(
			method = "neighborUpdate",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;",
					shift = At.Shift.AFTER
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void logPowered(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, CallbackInfo ci, boolean bl) {
		logPowered(world,  pos, bl);
	}
}
