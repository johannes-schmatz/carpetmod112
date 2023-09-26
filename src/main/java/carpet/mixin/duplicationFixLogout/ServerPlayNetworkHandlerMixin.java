package carpet.mixin.duplicationFixLogout;

import carpet.CarpetSettings;
import net.minecraft.network.packet.c2s.play.PlayerHandActionC2SPacket;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "handlePlayerHandAction",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onDigging(PlayerHandActionC2SPacket packet, CallbackInfo ci) {
        // Prevent player preforming actions after disconnecting. CARPET-XCOM
        if (CarpetSettings.duplicationFixLogout && player.m_6230070()) ci.cancel();
    }
}
