package carpet.mixin.profiler;

import carpet.helpers.LagSpikeHelper;
import carpet.utils.CarpetProfiler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import redstone.multimeter.common.TickTask;
import redstone.multimeter.helper.WorldHelper;

import static carpet.helpers.LagSpikeHelper.PrePostSubPhase.*;
import static carpet.helpers.LagSpikeHelper.TickPhase.*;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(WorldSaveHandler levelProperties, LevelProperties levelProperties2, Dimension dimension, Profiler profiler, boolean isClient) {
        super(levelProperties, levelProperties2, dimension, profiler, isClient);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/MobSpawnerHelper;tickSpawners(Lnet/minecraft/server/world/ServerWorld;ZZZ)I"
            )
    )
    private void preSpawning(CallbackInfo ci) {
        WorldHelper.startTickTask(TickTask.MOB_SPAWNING); // RSMM

        CarpetProfiler.start_section(this.dimension.getDimensionType().getName(), "spawning");

        LagSpikeHelper.processLagSpikes(this, MOB_SPAWNING, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/MobSpawnerHelper;tickSpawners(Lnet/minecraft/server/world/ServerWorld;ZZZ)I",
                    shift = At.Shift.AFTER
            )
    )
    private void postSpawning(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, MOB_SPAWNING, POST);

        CarpetProfiler.end_current_section();

        WorldHelper.endTickTask();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkProvider;tickChunks()Z"
            )
    )
    private void preChunkUnloading(CallbackInfo ci) {
        WorldHelper.startTickTask(TickTask.CHUNK_SOURCE); // RSMM

        LagSpikeHelper.processLagSpikes(this, CHUNK_UNLOADING, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkProvider;tickChunks()Z",
                    shift = At.Shift.AFTER
            )
    )
    private void postChunkUnloading(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, CHUNK_UNLOADING, POST);

        WorldHelper.endTickTask(); // RSMM
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;method_3644(Z)Z"
            )
    )
    private void preTileTick(CallbackInfo ci) {
        CarpetProfiler.start_section(this.dimension.getDimensionType().getName(), "blocks");
        LagSpikeHelper.processLagSpikes(this, TILE_TICK, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;method_3644(Z)Z",
                    shift = At.Shift.AFTER
            )
    )
    private void postTileTick(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, TILE_TICK, PRE);
        CarpetProfiler.end_current_section();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickBlocks()V"
            )
    )
    private void preRandomTick(CallbackInfo ci) {
        WorldHelper.startTickTask(TickTask.TICK_CHUNKS); // RSMM
        CarpetProfiler.start_section(this.dimension.getDimensionType().getName(), "blocks");
        LagSpikeHelper.processLagSpikes(this, RANDOM_TICK, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickBlocks()V",
                    shift = At.Shift.AFTER
            )
    )
    private void postRandomTick(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, RANDOM_TICK, POST);
        CarpetProfiler.end_current_section();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerWorldManager;method_2111()V"
            )
    )
    private void preChunkMap(CallbackInfo ci) {
        WorldHelper.swapTickTask(TickTask.CHUNK_MAP); // RSMM

        LagSpikeHelper.processLagSpikes(this, PLAYER_CHUNK_MAP, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerWorldManager;method_2111()V",
                    shift = At.Shift.AFTER
            )
    )
    private void postChunkMap(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, PLAYER_CHUNK_MAP, POST);

        WorldHelper.endTickTask(); // RSMM
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/VillageState;method_2839()V"
            )
    )
    private void preVillage(CallbackInfo ci) {
        WorldHelper.startTickTask(TickTask.VILLAGES); // RSMM

        LagSpikeHelper.processLagSpikes(this, VILLAGE, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/ZombieSiegeManager;method_2835()V",
                    shift = At.Shift.AFTER
            )
    )
    private void postVillage(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, VILLAGE, POST);

        WorldHelper.swapTickTask(TickTask.PORTALS); // RSMM
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;method_2131()V"
            )
    )
    private void preBlockEvent(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, BLOCK_EVENT, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;method_2131()V",
                    shift = At.Shift.AFTER
            )
    )
    private void postBlockEvent(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, BLOCK_EVENT, POST);

        WorldHelper.endTickTask(); // RSMM
    }
}
