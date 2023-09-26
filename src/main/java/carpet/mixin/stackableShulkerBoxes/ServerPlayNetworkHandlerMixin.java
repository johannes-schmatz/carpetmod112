package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetMod;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.menu.ActionType;
import net.minecraft.inventory.menu.InventoryMenu;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Redirect(
            method = "handleMenuClickSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/inventory/menu/InventoryMenu;onClickSlot(IILnet/minecraft/inventory/menu/ActionType;Lnet/minecraft/entity/living/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack slotClick(InventoryMenu container, int slotId, int dragType, ActionType clickTypeIn, PlayerEntity player) {
        try {
            CarpetMod.playerInventoryStacking.set(Boolean.TRUE);
            return container.onClickSlot(slotId, dragType, clickTypeIn, player);
        } finally {
            CarpetMod.playerInventoryStacking.set(Boolean.FALSE);
        }
    }
}
