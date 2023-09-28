package carpet.mixin.chunkLoading;

import carpet.CarpetSettings;

import net.minecraft.server.ChunkHolder;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.world.chunk.WorldChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Shadow public abstract ServerChunkCache getChunkSource();

    @Redirect(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ChunkMap;hasChunk(II)Z"
            )
    )
    private boolean isInPlayerChunkMap(ChunkMap map, int chunkX, int chunkZ) {
        ChunkHolder entry = map.getChunk(chunkX, chunkZ);
        if (entry != null && CarpetSettings.whereToChunkSavestate.canUnloadNearPlayers) {
            WorldChunk chunk = entry.getChunk();
            getChunkSource().scheduleUnload(chunk);
            chunk.removed = false;
            return true;
        }
        return false;
    }
}
