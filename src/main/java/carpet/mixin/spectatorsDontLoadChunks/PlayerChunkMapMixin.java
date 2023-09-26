package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import carpet.utils.ChunkLoading;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.ChunkHolder;
import net.minecraft.server.ChunkMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(ChunkMap.class)
public abstract class PlayerChunkMapMixin {
    @Shadow private static long chunkPosToLong(int chunkX, int chunkZ) { throw new AbstractMethodError(); }
    @Shadow @Final private Long2ObjectMap<ChunkHolder> chunks;
    @Shadow @Final private List<ChunkHolder> populating;
    @Shadow @Final private List<ChunkHolder> loading;
    @Shadow @Final private List<ChunkHolder> ticking;

    @Redirect(
            method = {
                    "addPlayer",
                    "movePlayer"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ChunkMap;getChunk(II)Lnet/minecraft/server/ChunkHolder;"
            )
    )
    private ChunkHolder getOrCreateHooks(ChunkMap map, int chunkX, int chunkZ, ServerPlayerEntity player) {
        return getOrCreateEntry(chunkX, chunkZ, player);
    }

    @Inject(
            method = "updateViewDistance",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;x:D"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void capturePlayer(int radius, CallbackInfo ci, int i, List<ServerPlayerEntity> list, Iterator<ServerPlayerEntity> iterator, ServerPlayerEntity player) {
        ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(player);
    }

    @Redirect(
            method = "updateViewDistance",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ChunkMap;getChunk(II)Lnet/minecraft/server/ChunkHolder;"
            )
    )
    private ChunkHolder getOrCreateOnSetRadius(ChunkMap map, int chunkX, int chunkZ) {
        return getOrCreateEntry(chunkX, chunkZ, null);
    }

    @Unique private ChunkHolder getOrCreateEntry(int chunkX, int chunkZ, ServerPlayerEntity player) {
        long i = chunkPosToLong(chunkX, chunkZ);
        ChunkHolder entry = this.chunks.get(i);

        if (entry == null) {
            if (player != null) ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(player);
            entry = new ChunkHolder((ChunkMap) (Object) this, chunkX, chunkZ);
            ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(null);

            if (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator())
                this.chunks.put(i, entry);
            this.ticking.add(entry);

            if (entry.getChunk() == null) {
                if (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator())
                    this.loading.add(entry);
            }

            if (!entry.populate()) {
                this.populating.add(entry);
            }
        }
        return entry;
    }
}
