package carpet.mixin.pistonClippingFix;

import carpet.CarpetSettings;
import carpet.utils.PistonFixes;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.PistonBaseBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBaseBlock.class)
public class PistonBlockMixin {
    @Inject(
            method = "checkExtended",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"
            )
    )
    private void synchronize(World worldIn, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (CarpetSettings.pistonClippingFix > 0) PistonFixes.synchronizeClient();
    }
}
