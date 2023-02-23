package carpet.mixin.core;

import carpet.helpers.TickSpeed;
import carpet.utils.JavaVersionUtil;
import carpet.utils.extensions.ExtendedWorld;

import net.minecraft.entity.Entity;
import net.minecraft.util.Tickable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(World.class)
public class WorldMixin implements ExtendedWorld {
    @Shadow @Final public Random random;

    @Shadow @Final public boolean isClient;
    private AtomicLong seed;

    @Redirect(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Tickable;tick()V"
            )
    )
    private void dontProcessTileEntities(Tickable tickable) {
        if (this.isClient || TickSpeed.process_entities) tickable.tick();
    }

    @Redirect(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;checkChunk(Lnet/minecraft/entity/Entity;)V"
            )
    )
    private void dontProcessEntities(World world, Entity entity) {
        if (this.isClient || TickSpeed.process_entities) world.checkChunk(entity);
    }

    @Override
    public long getRandSeed() {
        if (seed == null) {
            seed = JavaVersionUtil.objectFieldAccessor(Random.class, "seed", AtomicLong.class).get(random);
        }
        return seed.get();
    }
}
