package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    @Redirect(
            method = {
                "getShape",
                "onAdded",
                "updateState",
                "updateFacing",
                "getCombinedMenuProvider"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/BlockState;"
            )
    )
    private BlockState onGetBoundingBox(World world, BlockPos pos) {
        return CarpetClientChunkLogger.getBlockState(world, pos, "Chest loading");
    }
}
