package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.DebugLogHelper;
import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.Nullable;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow protected WorldData data;
    @Shadow @Nullable public abstract MinecraftServer getServer();

    @Inject(
            method = "tickWeather",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldData;setThunderTime(I)V"
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/WorldData;setThunderTime(I)V",
                            ordinal = 1
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/WorldData;setThunderTime(I)V",
                            ordinal = 2,
                            shift = At.Shift.AFTER
                    )
            )
    )
    private void onSetThunderTime(CallbackInfo ci) {
        // Log Weather CARPET-XCOM
        if (LoggerRegistry.__weather) {
            LoggerRegistry.getLogger("weather").log(()-> new Text[]{
                Messenger.s(null,
                        "Thunder is set to: " + this.data.getThunderTime() + " time: " + this.data.getTime() + " Server time: " + getServer().getTicks())
            },
            "TYPE", "Thunder",
            "THUNDERING", this.data.getThunderTime(),
            "TIME", this.data.getTime());
        }
    }


    @Inject(
            method = "tickWeather",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldData;setRainTime(I)V"
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/WorldData;setRainTime(I)V",
                            ordinal = 1
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/WorldData;setRainTime(I)V",
                            ordinal = 2,
                            shift = At.Shift.AFTER
                    )
            )
    )
    private void onSetRainTime(CallbackInfo ci) {
        // Log Weather CARPET-XCOM
        if (LoggerRegistry.__weather) {
            LoggerRegistry.getLogger("weather").log(() -> new Text[]{
                Messenger.s(null,
                        "Rain is set to: " + this.data.getRainTime() + " time: " + this.data.getTime() + " Server time: " + getServer().getTicks())
            },
            "TYPE", "Rain",
            "RAINING", this.data.getRainTime(),
            "TIME", this.data.getTime());
        }
    }

    @Inject(
            method = "removeEntity",
            at = @At("HEAD")
    )
    private void invisDebugAtRemoveEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity) DebugLogHelper.invisDebug(() -> "r1: " + entity);
    }

    @Inject(
            method = "removeEntityNow",
            at = @At("HEAD")
    )
    private void invisDebugAtRemoveEntityDangerously(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity) DebugLogHelper.invisDebug(() -> "r1: " + entity);
    }
}
