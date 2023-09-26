package carpet.mixin.enableStableLcgNetherEnd;

import carpet.CarpetSettings;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(WorldStorage storage, WorldData data, Dimension dimension, Profiler profiler,
            boolean isClient) {
        super(storage, data, dimension, profiler, isClient);
    }

    @Unique private boolean shouldLcg() {
        return !CarpetSettings.enableStableLCGNetherEnd || dimension.getType().getId() == 0;
    }

    @Redirect(
            method = "tickChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Random;nextInt(I)I",
                    remap = false
            )
    )
    private int nextInt(Random random, int bound) {
        return shouldLcg() ? random.nextInt(bound) : -1;
    }
}
