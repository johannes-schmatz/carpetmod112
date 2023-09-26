package carpet.mixin.inconsistentRedstoneTorchesFix;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedstoneTorchBlock.class)
public class RedstoneTorchBlockMixin {
    @Inject(
            method = "neighborChanged",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;scheduleTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"
            ),
            cancellable = true
    )
    private void inconsistentRedstoneTorchesFix(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.inconsistentRedstoneTorchesFix && world.hasScheduledTick(pos, (RedstoneTorchBlock) (Object) this)) {
            ci.cancel();
        }
    }
}
