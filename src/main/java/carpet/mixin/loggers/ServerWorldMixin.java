package carpet.mixin.loggers;

import carpet.CarpetSettings;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.utils.Messenger;
import carpet.utils.extensions.ExtendedWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    @Shadow @Final private MinecraftServer server;

    protected ServerWorldMixin(WorldSaveHandler levelProperties, LevelProperties levelProperties2, Dimension dimension, Profiler profiler, boolean isClient) {
        super(levelProperties, levelProperties2, dimension, profiler, isClient);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;method_3644(Z)Z",
                    shift = At.Shift.AFTER
            )
    )
    private void rngTickUpdates(CallbackInfo ci) {
        logAndSetRng("TickUp.");
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;method_2131()V",
                    shift = At.Shift.AFTER
            )
    )
    private void rngBlockEvents(CallbackInfo ci) {
        logAndSetRng("BlockEv.");
    }


    @Unique private void logAndSetRng(String phase) {
        if (LoggerRegistry.__rng) {
            LoggerRegistry.getLogger("rng").log(() -> new Text[]{
                    Messenger.s(null, String.format("RNG %s t:%d seed:%d d:%s", phase, server.getTicks(), ((ExtendedWorld) this).getRandSeed(),
                            dimension.getDimensionType().name()))
            });
        }
        if (CarpetSettings.setSeed != 0) {
            this.random.setSeed(CarpetSettings.setSeed ^ 0x5DEECE66DL);
        }
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void logLastExplosion(CallbackInfo ci) {
        // Solution for final explosion check -- not a great solution - CARPET-SYLKOS
        if(LoggerRegistry.__explosions) {
            ExplosionLogHelper.logLastExplosion();
        }
    }
}
