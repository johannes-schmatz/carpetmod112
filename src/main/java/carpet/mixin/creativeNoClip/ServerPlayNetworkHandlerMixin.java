package carpet.mixin.creativeNoClip;

import carpet.CarpetSettings;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    // Applies noClip, using isSleeping because pistonClippingFix already redirects noClip
    @Redirect(
            method = "handlePlayerMove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;isSleeping()Z",
                    ordinal = 2
            )
    )
    private boolean isPlayerSleeping(ServerPlayerEntity player) {
        return player.isSleeping() || (CarpetSettings.creativeNoClip && player.isCreative());
    }
}
