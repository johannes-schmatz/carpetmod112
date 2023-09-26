package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;

import net.minecraft.server.ChunkMap;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.world.chunk.WorldChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkMap.class)
public class PlayerChunkMapMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/chunk/ServerChunkCache;scheduleUnloadAll()V"
            )
    )
    private void queueUnloadAll(ServerChunkCache provider) {
        try {
            CarpetClientChunkLogger.setReason("Dimensional unloading due to no players");
            provider.scheduleUnloadAll();
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }

    @Redirect(
            method = "unload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/chunk/ServerChunkCache;scheduleUnload(Lnet/minecraft/world/chunk/WorldChunk;)V"
            )
    )
    private void queueUnload(ServerChunkCache provider, WorldChunk chunk) {
        try {
            CarpetClientChunkLogger.setReason("Player leaving chunk, queuing unload");
            provider.scheduleUnload(chunk);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
