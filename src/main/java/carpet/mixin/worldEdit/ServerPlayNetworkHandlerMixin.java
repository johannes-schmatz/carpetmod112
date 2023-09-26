package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;

import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow @Final private MinecraftServer server;
    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "handlePlayerUseItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ServerPlayerInteractionManager;useItem(Lnet/minecraft/entity/living/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
            )
    )
    private void onRightClickAir(HandSwingC2SPacket packetIn, CallbackInfo ci) {
        WorldEditBridge.onRightClickAir(server.getWorld(player.dimensionId), player);
    }
}
