package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.entity.EntityTrackerEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Inject(
            method = "isInViewOfPlayer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void spectatorsDontLoadChunks(ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.spectatorsDontLoadChunks && player.isSpectator()) cir.setReturnValue(true);
    }
}
