package carpet.mixin.loggers;

import carpet.logging.logHelpers.DebugLogHelper;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.entity.EntityTrackerEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Inject(
            method = "notifyEntityRemoved(Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;)V",
            at = @At("HEAD")
    )
    private void invisDebug(ServerPlayerEntity playerMP, CallbackInfo ci) {
        DebugLogHelper.invisDebug(() -> "r1: " + playerMP, true);
    }
}
