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

import net.minecraft.server.world.ChunkGenerator;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ServerChunkProvider;

@Mixin(ServerChunkProvider.class)
public class ServerChunkCacheMixin {
    @Shadow @Final private ServerWorld world;

    @Inject(
            method = "unload",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;add(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private void logUnload(Chunk chunk, CallbackInfo ci) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, chunk.chunkX, chunk.chunkZ, CarpetClientChunkLogger.Event.QUEUE_UNLOAD);
        }
    }

    @Inject(
            method = "getLoadedChunk",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/chunk/Chunk;field_12912:Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void logCancelUnload(int x, int z, CallbackInfoReturnable<Chunk> cir, long key, Chunk chunk) {
        if (CarpetClientChunkLogger.logger.enabled && chunk.field_12912) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.CANCEL_UNLOAD);
        }
    }

    @Inject(
            method = "method_12777",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;put(JLjava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            )
    )
    private void logLoad(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.LOADING);
        }
    }

    @Redirect(
            method = "method_12777",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;populateIfMissing(Lnet/minecraft/world/chunk/ChunkProvider;Lnet/minecraft/server/world/ChunkGenerator;)V"
            )
    )
    private void populate(Chunk chunk, ChunkProvider provider, ChunkGenerator generator) {
        try {
            CarpetClientChunkLogger.setReason("Population triggering neighbouring chunks to cancel unload");
            chunk.populateIfMissing(provider, generator);
        } finally {
            CarpetClientChunkLogger.resetToOldReason();
        }
    }

    @Inject(
            method = "getOrGenerateChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ChunkGenerator;generate(II)Lnet/minecraft/world/chunk/Chunk;",
                    shift = At.Shift.AFTER
            )
    )
    private void logGenerator(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, x, z, CarpetClientChunkLogger.Event.GENERATING);
        }
    }

    @Inject(
            method = "tickChunks",
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
            method = "tickChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;remove(Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void logUnload(CallbackInfoReturnable<Boolean> cir, Iterator<Long> iterator, int i, Long key, Chunk chunk) {
        if (CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.log(world, chunk.chunkX, chunk.chunkZ, CarpetClientChunkLogger.Event.UNLOADING);
        }
    }

    @Inject(
            method = "tickChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkStorage;method_3950()V"
            )
    )
    private void resetUnloadingReason(CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.resetReason();
    }
}
