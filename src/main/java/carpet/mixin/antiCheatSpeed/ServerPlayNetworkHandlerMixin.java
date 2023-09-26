package carpet.mixin.antiCheatSpeed;

import carpet.CarpetSettings;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Redirect(
            method = "handlePlayerMove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;m_1692135()Z"
            )
    )
    private boolean antiCheatSpeed(ServerPlayerEntity player) {
        return CarpetSettings.antiCheatSpeed || player.m_1692135();
    }
}
