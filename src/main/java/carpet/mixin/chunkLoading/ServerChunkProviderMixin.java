package carpet.mixin.chunkLoading;

import carpet.CarpetSettings;
import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import carpet.utils.ChunkLoading;
import carpet.utils.TickingArea;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ServerChunkProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.Nullable;
import java.util.Set;

@Mixin(ServerChunkProvider.class)
public abstract class ServerChunkProviderMixin {
    private boolean fakePermaloaderProtected;

    @Shadow @Final private ServerWorld world;
    @Shadow @Final private Long2ObjectMap<Chunk> loadedChunksMap;

    @Shadow protected abstract void saveEntities(Chunk chunkIn);
    @Shadow @Nullable public abstract Chunk getLoadedChunk(int x, int z);

    @Shadow @Nullable public abstract Chunk method_12777(int x, int z);

    @Redirect(
            method = "unload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;canChunkBeUnloaded(II)Z"
            )
    )
    private boolean canDrop(Dimension dimension, int x, int z) {
        if (CarpetSettings.tickingAreas && TickingArea.isTickingChunk(world, x, z)) return false;
        if (CarpetSettings.disableSpawnChunks) return true;
        return dimension.canChunkBeUnloaded(x, z);
    }

    @Inject(
            method = "unloadAll",
            at = @At("HEAD")
    )
    private void onQueueAll(CallbackInfo ci) {
        fakePermaloaderProtected = CarpetSettings.simulatePermaloader;
    }

    @Inject(
            method = "method_12776",
            at = @At("RETURN")
    )
    private void onSaveChunks(boolean all, CallbackInfoReturnable<Boolean> cir) {
        fakePermaloaderProtected = CarpetSettings.simulatePermaloader;
    }

    @Redirect(
            method = "tickChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;isEmpty()Z",
                    remap = false
            )
    )
    private boolean droppedChunksIsEmpty(Set<Long> droppedChunks) {
        return droppedChunks.isEmpty() || fakePermaloaderProtected;
    }

    @Redirect(
            method = "tickChunks",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/chunk/Chunk;field_12912:Z"
            )
    )
    private boolean isUnloadQueued(Chunk chunk) {
        if (chunk.field_12912) return true;
        if (CarpetSettings.whereToChunkSavestate.canUnloadNearPlayers) {
            //noinspection ConstantConditions
            if (CarpetSettings.whereToChunkSavestate == CarpetSettings.WhereToChunkSavestate.everywhere
                    || world.method_8536(Entity.class, player -> player.chunkX == chunk.chunkX && player.chunkZ == chunk.chunkZ).isEmpty()) {
                // Getting the chunk size is incredibly inefficient, but it's better than unloading and reloading the chunk
                if ((ChunkLoading.getSavedChunkSize(chunk) + 5) / 4096 + 1 >= 256) {
                    chunk.unloadFromWorld();
                    //this.saveChunkData(chunk); no point saving the chunk data, we know that won't work
                    this.saveEntities(chunk);
                    this.loadedChunksMap.remove(ChunkPos.getIdFromCoords(chunk.chunkX, chunk.chunkZ));
                    //++i; don't break stuff
                    Chunk newChunk = this.method_12777(chunk.chunkX, chunk.chunkZ);
                    if (newChunk != null)
                        newChunk.populateBlockEntities(true);
                    ChunkPlayerManager pcmEntry = world.getPlayerWorldManager().method_12811(chunk.chunkX, chunk.chunkZ);
                    if (pcmEntry != null) {
                        ((PlayerChunkMapEntryAccessor) pcmEntry).setChunk(newChunk);
                        ((PlayerChunkMapEntryAccessor) pcmEntry).setSentToPlayers(false);
                        pcmEntry.method_12801();
                    }
                }
            }
        }
        return false;
    }

    @Inject(
            method = "tickChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkStorage;method_3950()V"
            )
    )
    private void resetFakePermaloader(CallbackInfoReturnable<Boolean> cir) {
        fakePermaloaderProtected = false;
    }
}
