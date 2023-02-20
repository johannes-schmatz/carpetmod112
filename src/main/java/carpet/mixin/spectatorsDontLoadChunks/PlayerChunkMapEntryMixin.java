package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.utils.ChunkLoading;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.world.chunk.ServerChunkProvider;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkPlayerManager.class)
public class PlayerChunkMapEntryMixin {
    @Shadow private boolean field_13865;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ServerChunkProvider;method_12777(II)Lnet/minecraft/world/chunk/Chunk;"
            )
    )
    private Chunk loadChunk(ServerChunkProvider provider, int x, int z) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ServerPlayerEntity player = ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.get();
            if (player != null && player.isSpectator()) return null;
        }
        try {
            CarpetClientChunkLogger.setReason("Player loading chunk");
            return provider.method_12777(x, z);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }

    // Return false to prevent client unloading the chunks when attempting to use spectate entitys near unloaded chunks. CARPET-XCOM
    @Redirect(
            method = "method_8127",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/ChunkPlayerManager;field_13865:Z"
            )
    )
    private boolean sendPacket(ChunkPlayerManager entry, ServerPlayerEntity player) {
        return field_13865 && (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator());
    }
}
