package carpet.mixin.duplicationFixMovingBlock;

import carpet.helpers.PistonHelper;

import net.minecraft.block.PistonBaseBlock;
import net.minecraft.block.piston.PistonMoveStructureResolver;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PistonBaseBlock.class)
public class PistonBlockMixin {
    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/block/piston/PistonMoveStructureResolver;getToMove()Ljava/util/List;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void dupeFixStart(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir, PistonMoveStructureResolver helper, List<BlockPos> moving) {
        PistonHelper.registerPushed(moving);
    }

    @Inject(
            method = "move",
            at = @At(
                    value = "RETURN",
                    ordinal = 1
            )
    )
    private void dupeFixEnd(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        PistonHelper.finishPush();
    }
}
