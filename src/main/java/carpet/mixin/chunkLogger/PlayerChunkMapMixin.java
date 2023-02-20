package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.server.PlayerWorldManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ServerChunkProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerWorldManager.class)
public class PlayerChunkMapMixin {
    @Redirect(
            method = "method_2111",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ServerChunkProvider;unloadAll()V"
            )
    )
    private void queueUnloadAll(ServerChunkProvider provider) {
        try {
            CarpetClientChunkLogger.setReason("Dimensional unloading due to no players");
            provider.unloadAll();
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }

    @Redirect(
            method = "method_12812",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ServerChunkProvider;unload(Lnet/minecraft/world/chunk/Chunk;)V"
            )
    )
    private void queueUnload(ServerChunkProvider provider, Chunk chunk) {
        try {
            CarpetClientChunkLogger.setReason("Player leaving chunk, queuing unload");
            provider.unload(chunk);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
