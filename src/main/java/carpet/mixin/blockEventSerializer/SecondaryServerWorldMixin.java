package carpet.mixin.blockEventSerializer;

import net.minecraft.server.world.ReadOnlyServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReadOnlyServerWorld.class)
public abstract class SecondaryServerWorldMixin extends ServerWorldMixin {
    protected SecondaryServerWorldMixin(WorldStorage storage, WorldData data, Dimension dimension,
            Profiler profiler, boolean isClient) {
        super(storage, data, dimension, profiler, isClient);
    }

    @Inject(
            method = "init",
            at = @At("RETURN")
    )
    private void onInit(CallbackInfoReturnable<World> cir) {
        initBlockEventSerializer();
    }
}
