package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import carpet.utils.ChunkLoading;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.server.PlayerWorldManager;

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

@Mixin(PlayerWorldManager.class)
public abstract class PlayerChunkMapMixin {
    @Shadow private static long method_12815(int chunkX, int chunkZ) { throw new AbstractMethodError(); }
    @Shadow @Final private Long2ObjectMap<ChunkPlayerManager> field_13868;
    @Shadow @Final private List<ChunkPlayerManager> field_13870;
    @Shadow @Final private List<ChunkPlayerManager> field_13871;
    @Shadow @Final private List<ChunkPlayerManager> playerInstances;

    @Redirect(
            method = {
                    "method_2109",
                    "method_2116"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerWorldManager;method_12813(II)Lnet/minecraft/server/ChunkPlayerManager;"
            )
    )
    private ChunkPlayerManager getOrCreateHooks(PlayerWorldManager map, int chunkX, int chunkZ, ServerPlayerEntity player) {
        return getOrCreateEntry(chunkX, chunkZ, player);
    }

    @Inject(
            method = "applyViewDistance",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;x:D"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void capturePlayer(int radius, CallbackInfo ci, int i, List<ServerPlayerEntity> list, Iterator<ServerPlayerEntity> iterator, ServerPlayerEntity player) {
        ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(player);
    }

    @Redirect(
            method = "applyViewDistance",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerWorldManager;method_12813(II)Lnet/minecraft/server/ChunkPlayerManager;"
            )
    )
    private ChunkPlayerManager getOrCreateOnSetRadius(PlayerWorldManager map, int chunkX, int chunkZ) {
        return getOrCreateEntry(chunkX, chunkZ, null);
    }

    @Unique private ChunkPlayerManager getOrCreateEntry(int chunkX, int chunkZ, ServerPlayerEntity player) {
        long i = method_12815(chunkX, chunkZ);
        ChunkPlayerManager entry = this.field_13868.get(i);

        if (entry == null) {
            if (player != null) ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(player);
            entry = new ChunkPlayerManager((PlayerWorldManager) (Object) this, chunkX, chunkZ);
            ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(null);

            if (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator())
                this.field_13868.put(i, entry);
            this.playerInstances.add(entry);

            if (entry.getChunk() == null) {
                if (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator())
                    this.field_13871.add(entry);
            }

            if (!entry.method_12801()) {
                this.field_13870.add(entry);
            }
        }
        return entry;
    }
}
