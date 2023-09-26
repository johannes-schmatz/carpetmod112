package carpet.mixin.itemDesynchFix;

import carpet.CarpetSettings;

import net.minecraft.network.packet.c2s.play.MenuClickSlotC2SPacket;
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
            method = "handleMenuClickSlot",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;useItemCooldown:Z",
                    ordinal = 0
            )
    )
    private void itemDesynchFix(MenuClickSlotC2SPacket packetIn, CallbackInfo ci) {
        // Update item changes before setting boolean true given it can cause desynchs. CARPET-XCOM
        if (CarpetSettings.itemDesynchFix) {
            this.player.menu.updateListeners();
        }
    }
}
