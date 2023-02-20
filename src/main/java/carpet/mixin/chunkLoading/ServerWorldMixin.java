package carpet.mixin.chunkLoading;

import carpet.CarpetSettings;
import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.server.PlayerWorldManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ServerChunkProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Shadow public abstract ServerChunkProvider getChunkProvider();

    @Redirect(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerWorldManager;method_12808(II)Z"
            )
    )
    private boolean isInPlayerChunkMap(PlayerWorldManager map, int chunkX, int chunkZ) {
        ChunkPlayerManager entry = map.method_12811(chunkX, chunkZ);
        if (entry != null && CarpetSettings.whereToChunkSavestate.canUnloadNearPlayers) {
            Chunk chunk = entry.getChunk();
            getChunkProvider().unload(chunk);
            chunk.field_12912 = false;
            return true;
        }
        return false;
    }
}
