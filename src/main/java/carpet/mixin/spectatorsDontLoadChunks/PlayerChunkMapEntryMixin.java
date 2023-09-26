package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.utils.ChunkLoading;

import net.minecraft.server.ChunkHolder;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.world.chunk.WorldChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkHolder.class)
public class PlayerChunkMapEntryMixin {
    @Shadow private boolean populated;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/chunk/ServerChunkCache;loadChunk(II)Lnet/minecraft/world/chunk/WorldChunk;"
            )
    )
    private WorldChunk loadChunk(ServerChunkCache provider, int x, int z) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ServerPlayerEntity player = ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.get();
            if (player != null && player.isSpectator()) return null;
        }
        try {
            CarpetClientChunkLogger.setReason("Player loading chunk");
            return provider.loadChunk(x, z);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }

    // Return false to prevent client unloading the chunks when attempting to use spectate entitys near unloaded chunks. CARPET-XCOM
    @Redirect(
            method = "removePlayer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/ChunkHolder;populated:Z"
            )
    )
    private boolean sendPacket(ChunkHolder entry, ServerPlayerEntity player) {
        return populated && (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator());
    }
}
