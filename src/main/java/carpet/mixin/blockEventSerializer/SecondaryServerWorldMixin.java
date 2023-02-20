package carpet.mixin.blockEventSerializer;

import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MultiServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiServerWorld.class)
public abstract class SecondaryServerWorldMixin extends ServerWorldMixin {
    protected SecondaryServerWorldMixin(WorldSaveHandler levelProperties, LevelProperties levelProperties2, Dimension dimension, Profiler profiler, boolean isClient) {
        super(levelProperties, levelProperties2, dimension, profiler, isClient);
    }

    @Inject(
            method = "getWorld",
            at = @At("RETURN")
    )
    private void onInit(CallbackInfoReturnable<World> cir) {
        initBlockEventSerializer();
    }
}
