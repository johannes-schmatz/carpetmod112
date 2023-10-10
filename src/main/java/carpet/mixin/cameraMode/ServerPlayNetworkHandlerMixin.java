package carpet.mixin.cameraMode;

import carpet.CarpetSettings;
import carpet.utils.extensions.CameraPlayer;

import net.minecraft.network.packet.c2s.play.PlayerSpectateC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static carpet.commands.CommandGMS.setPlayerToSurvival;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Shadow @Final private MinecraftServer server;

    @Inject(
            method = "onDisconnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;remove(Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;)V"
            )
    )
    private void onLogout(Text reason, CallbackInfo ci) {
        // Fix exploit related to camera mode and logging out CARPET-XCOM
        if(CarpetSettings.cameraModeRestoreLocation && ((CameraPlayer) player).getGameModeCamera()){
            setPlayerToSurvival(server, player,true);
        }
    }

    @Inject(
            method = "handlePlayerSpectate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSpectate(PlayerSpectateC2SPacket packet, CallbackInfo ci) {
        // Disables spectating other players when using /c and carpet rule cameraModeDisableSpectatePlayers is true CARPET-XCOM
        if (((CameraPlayer) player).isDisableSpectatePlayers()) ci.cancel();
    }
}
