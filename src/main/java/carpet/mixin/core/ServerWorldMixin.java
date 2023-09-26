package carpet.mixin.core;

import carpet.helpers.TickSpeed;

import net.minecraft.server.ChunkMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.NaturalSpawner;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(value = ServerWorld.class, priority = 1001)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(WorldStorage levelProperties, WorldData levelProperties2, Dimension dimension, Profiler profiler, boolean isClient) {
        super(levelProperties, levelProperties2, dimension, profiler, isClient);
    }

    @Shadow protected abstract void doBlockEvents();

    // TODO: consider actually overwriting tick() once and for all!
    // see the if (TickSpeed.processEntities) and how sky cancels the mixin instead
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/NaturalSpawner;spawnEntities(Lnet/minecraft/server/world/ServerWorld;ZZZ)I"
            )
    )
    private int findChunksForSpawning(NaturalSpawner worldEntitySpawner, ServerWorld worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs,
            boolean spawnOnSetTickRate) {
        return TickSpeed.process_entities ? worldEntitySpawner.spawnEntities(worldServerIn, spawnHostileMobs, spawnPeacefulMobs, spawnOnSetTickRate) : 0;
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldData;setTime(J)V"
            )
    )
    private void setWorldTotalTime(WorldData worldInfo, long time) {
        if (TickSpeed.process_entities) {
            worldInfo.setTime(time);
        }

    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldData;setTimeOfDay(J)V"
            )
    )
    private void setWorldTime(WorldData worldInfo, long time) {
        if (TickSpeed.process_entities) worldInfo.setTimeOfDay(time);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;doScheduledTicks(Z)Z"
            )
    )
    private boolean tickUpdates(ServerWorld worldServer, boolean runAllPending) {
        return TickSpeed.process_entities && worldServer.doScheduledTicks(runAllPending);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ChunkMap;tick()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void cancelIfNotProcessEntities(CallbackInfo ci) {
        if (!TickSpeed.process_entities) {
            this.profiler.pop();
            this.doBlockEvents();
            ci.cancel();
        }
    }

    @Redirect(
            method = "tickChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ChunkMap;getTickingChunks()Ljava/util/Iterator;",
                    ordinal = 1
            )
    )
    private Iterator<WorldChunk> getChunkIterator(ChunkMap map) {
        Iterator<WorldChunk> iterator = map.getTickingChunks();
        if (!TickSpeed.process_entities) {
            while (iterator.hasNext()) {
                this.profiler.push("getChunk");
                WorldChunk chunk = iterator.next();
                this.profiler.swap("checkNextLight");
                chunk.checkLight();
                this.profiler.swap("tickChunk");
                chunk.tick(false);
                this.profiler.pop();
            }
            // now the iterator is done and the vanilla loop won't run
            // this acts like a `continue` after chunk.onTick(false)
        }
        return iterator;
    }
}
