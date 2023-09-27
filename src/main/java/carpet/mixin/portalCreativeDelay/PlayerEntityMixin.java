package carpet.mixin.portalCreativeDelay;

import carpet.CarpetSettings;
import carpet.helpers.PortalHelper;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(
            method = "getMaxNetherPortalTime",
            at = @At("HEAD"),
            cancellable = true
    )
    private void portalCreativeDelay(CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.portalCreativeDelay) {
            cir.setReturnValue(PortalHelper.playerHoldsObsidian((PlayerEntity) (Object) this) ? Integer.MAX_VALUE : 80);
        }
    }
}
