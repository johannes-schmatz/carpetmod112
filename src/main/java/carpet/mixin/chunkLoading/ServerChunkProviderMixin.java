package carpet.mixin.chunkLoading;

import carpet.CarpetSettings;
import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import carpet.utils.ChunkLoading;
import carpet.utils.TickingArea;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.ChunkHolder;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
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

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkProviderMixin {
    private boolean fakePermaloaderProtected;

    @Shadow @Final private ServerWorld world;
    @Shadow @Final private Long2ObjectMap<WorldChunk> chunkMap;

    @Shadow protected abstract void saveEntities(WorldChunk chunkIn);
    @Shadow @Nullable public abstract WorldChunk getChunk(int x, int z);

    @Shadow @Nullable public abstract WorldChunk loadChunk(int x, int z);

    @Redirect(
            method = "scheduleUnload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;canChunkUnload(II)Z"
            )
    )
    private boolean canDrop(Dimension dimension, int x, int z) {
        if (CarpetSettings.tickingAreas && TickingArea.isTickingChunk(world, x, z)) return false;
        if (CarpetSettings.disableSpawnChunks) return true;
        return dimension.canChunkUnload(x, z);
    }

    @Inject(
            method = "scheduleUnloadAll",
            at = @At("HEAD")
    )
    private void onQueueAll(CallbackInfo ci) {
        fakePermaloaderProtected = CarpetSettings.simulatePermaloader;
    }

    @Inject(
            method = "save(Z)Z",
            at = @At("RETURN")
    )
    private void onSaveChunks(boolean all, CallbackInfoReturnable<Boolean> cir) {
        fakePermaloaderProtected = CarpetSettings.simulatePermaloader;
    }

    @Redirect(
            method = "tick",
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
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;removed:Z"
            )
    )
    private boolean isUnloadQueued(WorldChunk chunk) {
        if (chunk.removed) return true;
        if (CarpetSettings.whereToChunkSavestate.canUnloadNearPlayers) {
            //noinspection ConstantConditions
            if (CarpetSettings.whereToChunkSavestate == CarpetSettings.WhereToChunkSavestate.everywhere
                    || world.getPlayers(Entity.class, player -> player.chunkX == chunk.chunkX && player.chunkZ == chunk.chunkZ).isEmpty()) {
                // Getting the chunk size is incredibly inefficient, but it's better than unloading and reloading the chunk
                if ((ChunkLoading.getSavedChunkSize(chunk) + 5) / 4096 + 1 >= 256) {
                    chunk.unload();
                    //this.saveChunkData(chunk); no point saving the chunk data, we know that won't work
                    this.saveEntities(chunk);
                    this.chunkMap.remove(ChunkPos.toLong(chunk.chunkX, chunk.chunkZ));
                    //++i; don't break stuff
                    WorldChunk newChunk = this.loadChunk(chunk.chunkX, chunk.chunkZ);
                    if (newChunk != null)
                        newChunk.tick(true);
                    ChunkHolder holder = world.getChunkMap().getChunk(chunk.chunkX, chunk.chunkZ);
                    if (holder != null) {
                        ((PlayerChunkMapEntryAccessor) holder).setChunk(newChunk);
                        ((PlayerChunkMapEntryAccessor) holder).setSentToPlayers(false);
                        holder.populate();
                    }
                }
            }
        }
        return false;
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/storage/ChunkStorage;tick()V"
            )
    )
    private void resetFakePermaloader(CallbackInfoReturnable<Boolean> cir) {
        fakePermaloaderProtected = false;
    }
}
