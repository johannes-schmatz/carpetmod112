package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;

import net.minecraft.block.PistonBaseBlock;
import net.minecraft.block.state.BlockState;
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
            at = @At("HEAD")
    )
    private void onMovementCheck(World worldIn, BlockPos pos, BlockState state, CallbackInfo ci) {
        CarpetClientChunkLogger.setReason("Piston scheduled by power source");
    }

    @Inject(
            method = "checkExtended",
            at = @At("RETURN")
    )
    private void onMovementCheckEnd(World worldIn, BlockPos pos, BlockState state, CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
