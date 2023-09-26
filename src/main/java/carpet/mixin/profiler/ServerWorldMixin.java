package carpet.mixin.profiler;

import carpet.helpers.LagSpikeHelper;
import carpet.utils.CarpetProfiler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static carpet.helpers.LagSpikeHelper.PrePostSubPhase.*;
import static carpet.helpers.LagSpikeHelper.TickPhase.*;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    protected ServerWorldMixin(WorldStorage storage, WorldData data, Dimension dimension, Profiler profiler,
            boolean isClient) {
        super(storage, data, dimension, profiler, isClient);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/NaturalSpawner;spawnEntities(Lnet/minecraft/server/world/ServerWorld;ZZZ)I"
            )
    )
    private void preSpawning(CallbackInfo ci) {
        CarpetProfiler.start_section(this.dimension.getType().getKey(), "spawning");

        LagSpikeHelper.processLagSpikes(this, MOB_SPAWNING, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/NaturalSpawner;spawnEntities(Lnet/minecraft/server/world/ServerWorld;ZZZ)I",
                    shift = At.Shift.AFTER
            )
    )
    private void postSpawning(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, MOB_SPAWNING, POST);

        CarpetProfiler.end_current_section();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkSource;tick()Z"
            )
    )
    private void preChunkUnloading(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, CHUNK_UNLOADING, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkSource;tick()Z",
                    shift = At.Shift.AFTER
            )
    )
    private void postChunkUnloading(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, CHUNK_UNLOADING, POST);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;doScheduledTicks(Z)Z"
            )
    )
    private void preTileTick(CallbackInfo ci) {
        CarpetProfiler.start_section(this.dimension.getType().getKey(), "blocks");
        LagSpikeHelper.processLagSpikes(this, TILE_TICK, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;doScheduledTicks(Z)Z",
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
                    target = "Lnet/minecraft/server/world/ServerWorld;tickChunks()V"
            )
    )
    private void preRandomTick(CallbackInfo ci) {
        CarpetProfiler.start_section(this.dimension.getType().getKey(), "blocks");
        LagSpikeHelper.processLagSpikes(this, RANDOM_TICK, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickChunks()V",
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
                    target = "Lnet/minecraft/server/ChunkMap;tick()V"
            )
    )
    private void preChunkMap(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, PLAYER_CHUNK_MAP, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ChunkMap;tick()V",
                    shift = At.Shift.AFTER
            )
    )
    private void postChunkMap(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, PLAYER_CHUNK_MAP, POST);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/village/SavedVillageData;tick()V"
            )
    )
    private void preVillage(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, VILLAGE, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/village/VillageSiege;tick()V",
                    shift = At.Shift.AFTER
            )
    )
    private void postVillage(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, VILLAGE, POST);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;doBlockEvents()V"
            )
    )
    private void preBlockEvent(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, BLOCK_EVENT, PRE);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;doBlockEvents()V",
                    shift = At.Shift.AFTER
            )
    )
    private void postBlockEvent(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, BLOCK_EVENT, POST);
    }
}
