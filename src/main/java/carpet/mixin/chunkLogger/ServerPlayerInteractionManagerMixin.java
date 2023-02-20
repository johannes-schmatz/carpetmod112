package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Redirect(
            method = "tryBreakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setAir(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean removeBlock(World world, BlockPos pos) {
        try {
            CarpetClientChunkLogger.setReason("Player removed block");
            return world.setAir(pos);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
