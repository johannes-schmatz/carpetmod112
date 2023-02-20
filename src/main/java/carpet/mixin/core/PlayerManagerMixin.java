package carpet.mixin.core;

import carpet.CarpetServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(
            method = "sendPlayerList",
            at = @At("RETURN")
    )
    private void onPlayerConnect(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetServer.getInstance().playerConnected(player);
    }

    @Inject(
            method = "method_12830",
            at = @At("HEAD")
    )
    private void onPlayerDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetServer.getInstance().playerDisconnected(player);
    }
}
