package carpet.mixin.core;

import carpet.helpers.TickSpeed;
import net.minecraft.entity.MobSpawnerHelper;
import net.minecraft.server.PlayerWorldManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import redstone.multimeter.common.TickTask;
import redstone.multimeter.helper.WorldHelper;

import java.util.Iterator;

@Mixin(value = ServerWorld.class, priority = 1001)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(WorldSaveHandler levelProperties, LevelProperties levelProperties2, Dimension dimension, Profiler profiler, boolean isClient) {
        super(levelProperties, levelProperties2, dimension, profiler, isClient);
    }

    @Shadow protected abstract void method_2131();

    // TODO: consider actually overwriting tick() once and for all!
    // see the if (TickSpeed.processEntities) and how sky cancels the mixin instead
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/MobSpawnerHelper;tickSpawners(Lnet/minecraft/server/world/ServerWorld;ZZZ)I"
            )
    )
    private int findChunksForSpawning(MobSpawnerHelper worldEntitySpawner, ServerWorld worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs,
            boolean spawnOnSetTickRate) {
        return TickSpeed.process_entities ? worldEntitySpawner.tickSpawners(worldServerIn, spawnHostileMobs, spawnPeacefulMobs, spawnOnSetTickRate) : 0;
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelProperties;setTime(J)V"
            )
    )
    private void setWorldTotalTime(LevelProperties worldInfo, long time) {
        if (TickSpeed.process_entities) {
            boolean tick_time = dimension.getDimensionType() == DimensionType.OVERWORLD;

            if (tick_time) {
                WorldHelper.startTickTask(TickTask.TICK_TIME);
            }

            worldInfo.setTime(time);

            if (tick_time) {
                WorldHelper.getMultimeterServer().onOverworldTickTime();
                WorldHelper.endTickTask();
            }
        }

    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelProperties;setDayTime(J)V"
            )
    )
    private void setWorldTime(LevelProperties worldInfo, long time) {
        if (TickSpeed.process_entities) worldInfo.setDayTime(time);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;method_3644(Z)Z"
            )
    )
    private boolean tickUpdates(ServerWorld worldServer, boolean runAllPending) {
        return TickSpeed.process_entities && worldServer.method_3644(runAllPending);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerWorldManager;method_2111()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void cancelIfNotProcessEntities(CallbackInfo ci) {
        if (!TickSpeed.process_entities) {
            this.profiler.pop();
            this.method_2131();
            ci.cancel();
        }
    }

    @Redirect(
            method = "tickBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerWorldManager;method_12810()Ljava/util/Iterator;",
                    ordinal = 1
            )
    )
    private Iterator<Chunk> getChunkIterator(PlayerWorldManager map) {
        Iterator<Chunk> iterator = map.method_12810();
        if (!TickSpeed.process_entities) {
            while (iterator.hasNext()) {
                this.profiler.push("getChunk");
                WorldHelper.swapTickTask(false, TickTask.TICK_CHUNK); // RSMM
                Chunk chunk = iterator.next();
                this.profiler.swap("checkNextLight");
                chunk.method_3923();
                this.profiler.swap("tickChunk");
                chunk.populateBlockEntities(false);
                this.profiler.pop();
                WorldHelper.endTickTask(false); // RSMM
            }
            // now the iterator is done and the vanilla loop won't run
            // this acts like a `continue` after chunk.onTick(false)
        }
        return iterator;
    }
}
