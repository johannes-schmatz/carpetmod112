package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.ItemAction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Redirect(
            method = "onClickWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/ScreenHandler;method_3252(IILnet/minecraft/util/ItemAction;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack slotClick(ScreenHandler container, int slotId, int dragType, ItemAction clickTypeIn, PlayerEntity player) {
        try {
            CarpetMod.playerInventoryStacking.set(Boolean.TRUE);
            return container.method_3252(slotId, dragType, clickTypeIn, player);
        } finally {
            CarpetMod.playerInventoryStacking.set(Boolean.FALSE);
        }
    }
}
