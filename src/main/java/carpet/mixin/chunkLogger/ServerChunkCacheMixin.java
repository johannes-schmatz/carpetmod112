package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.world.chunk.ChunkGenerator;
import net.minecraft.world.chunk.ChunkSource;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
    @Shadow @Final private ServerWorld world;

    @Inject(
            method = "scheduleUnload",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;add(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private void logUnload(WorldChunk chunk, CallbackInfo ci) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, chunk.chunkX, chunk.chunkZ, CarpetClientChunkLogger.Event.QUEUE_UNLOAD);
        }
    }

    @Inject(
            method = "getChunk",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;removed:Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void logCancelUnload(int x, int z, CallbackInfoReturnable<WorldChunk> cir, long key, WorldChunk chunk) {
        if (CarpetClientChunkLogger.logger.enabled && chunk.removed) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.CANCEL_UNLOAD);
        }
    }

    @Inject(
            method = "loadChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;put(JLjava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            )
    )
    private void logLoad(int x, int z, CallbackInfoReturnable<WorldChunk> cir) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.LOADING);
        }
    }

    @Redirect(
            method = "loadChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;populate(Lnet/minecraft/world/chunk/ChunkSource;Lnet/minecraft/world/chunk/ChunkGenerator;)V"
            )
    )
    private void populate(WorldChunk chunk, ChunkSource provider, ChunkGenerator generator) {
        try {
            CarpetClientChunkLogger.setReason("Population triggering neighbouring chunks to cancel unload");
            chunk.populate(provider, generator);
        } finally {
            CarpetClientChunkLogger.resetToOldReason();
        }
    }

    @Inject(
            method = "getChunkNow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkGenerator;getChunk(II)Lnet/minecraft/world/chunk/WorldChunk;",
                    shift = At.Shift.AFTER
            )
    )
    private void logGenerator(int x, int z, CallbackInfoReturnable<WorldChunk> cir) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.GENERATING);
        }
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;iterator()Ljava/util/Iterator;",
                    remap = false
            )
    )
    private void setUnloadingReason(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.setReason("Unloading chunk and writing to disk");
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;remove(Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void logUnload(CallbackInfoReturnable<Boolean> cir, Iterator<Long> iterator, int i, Long key, WorldChunk chunk) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, chunk.chunkX, chunk.chunkZ, CarpetClientChunkLogger.Event.UNLOADING);
        }
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/storage/ChunkStorage;tick()V"
            )
    )
    private void resetUnloadingReason(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.resetReason();
    }
}
