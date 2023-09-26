package carpet.mixin.accurateBlockPlacement;

import carpet.CarpetSettings;

import net.minecraft.block.PistonBaseBlock;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonBaseBlock.class)
public class PistonBlockMixin {
    @Redirect(
            method = "onPlaced",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;I)Z"
            )
    )
    private boolean accurateSetBlockState(World world, BlockPos pos, BlockState newState, int flags) {
        return CarpetSettings.accurateBlockPlacement || world.setBlockState(pos, newState, flags);
    }
}
