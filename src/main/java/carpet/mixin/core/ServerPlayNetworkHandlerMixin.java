package carpet.mixin.core;

import carpet.CarpetServer;
import carpet.helpers.TickSpeed;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerSpectateC2SPacket;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "handleCustomPayload",
            at = @At("TAIL")
    )
    private void handleCarpetCustomPayloads(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (!packet.getChannel().startsWith("MC|")) {
            CarpetServer.getInstance().pluginChannels.process(player, packet);
        }
    }

    @Inject(
            method = "handlePlayerInput",
            at = @At("RETURN")
    )
    private void resetActiveTimeout(PlayerInputC2SPacket packet, CallbackInfo ci) {
        if (packet.getSidewaysSpeed() != 0.0F || packet.getForwardSpeed() != 0.0F || packet.getJumping() || packet.getSneaking()) {
            TickSpeed.reset_player_active_timeout();
        }
    }

    @Inject(
            method = "handlePlayerMove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;isSleeping()Z",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void resetActiveTimeout(PlayerMoveC2SPacket packet, CallbackInfo ci, ServerWorld world,
            double posX, double posY, double posZ, double posY2, double packetX, double packetY, double packetZ,
            float packetYaw, float packetPitch, double diffX, double diffY, double diffZ, double speed, double distance) {
        if (distance > 0.0001) {
            TickSpeed.reset_player_active_timeout();
        }
    }

    // Invisibility fix
    @Redirect(
            method = "handlePlayerSpectate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;removeEntityNow(Lnet/minecraft/entity/Entity;)V"
            )
    )
    private void removeEntityDangerously(ServerWorld world, Entity player) {
        world.removeEntity(player);
        world.getChunkAt(player.chunkX, player.chunkZ).removeEntity(player, player.chunkY);
    }

    @Inject(
            method = "handlePlayerSpectate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;addEntity(Lnet/minecraft/entity/Entity;)Z"
            )
    )
    private void updateRemovedPlayer(PlayerSpectateC2SPacket packet, CallbackInfo ci) {
        player.getServerWorld().tickEntity(player, false);
    }
}
