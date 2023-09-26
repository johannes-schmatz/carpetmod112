package carpet.mixin.elytraCheckFix;

import carpet.CarpetSettings;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    // Remove the falling check as in 1.15 CARPET-XCOM
    @Redirect(
            method = "handlePlayerMovementAction",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;velocityY:D"
            )
    )
    private double motionForElytraCheck(ServerPlayerEntity player) {
        return CarpetSettings.elytraCheckFix ? -1 : player.velocityY;
    }
}
