package carpet.mixin.railRotationFix;

import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({
    RailBlock.class,
    DetectorRailBlock.class,
    PoweredRailBlock.class
})
public class RailBlocksMixin {
    @Inject(
            method = "rotate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fixControlFlow(BlockState state, BlockRotation rot, CallbackInfoReturnable<BlockState> cir) {
        if (rot != BlockRotation.CLOCKWISE_180) return;
        AbstractRailBlock.Shape shape = state.get(RailBlock.SHAPE);
        if (shape == AbstractRailBlock.Shape.NORTH_SOUTH || shape == AbstractRailBlock.Shape.EAST_WEST) {
            // these don't change the state but the missing cases in vanilla fall through to COUNTERCLOCKWISE_90
            // leading to incorrect results
            cir.setReturnValue(state);
        }
    }
}
