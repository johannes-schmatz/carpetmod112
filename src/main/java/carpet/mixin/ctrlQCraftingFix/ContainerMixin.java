package carpet.mixin.ctrlQCraftingFix;

import carpet.CarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.menu.ActionType;
import net.minecraft.inventory.menu.InventoryMenu;
import net.minecraft.inventory.slot.InventorySlot;
import net.minecraft.item.ItemStack;

import java.util.List;

@Mixin(InventoryMenu.class)
public abstract class ContainerMixin {
    @Shadow public List<InventorySlot> slots;
    @Shadow public abstract ItemStack onClickSlot(int slotId, int dragType, ActionType clickTypeIn, PlayerEntity player);
    @Shadow public abstract void updateListeners();

    @Inject(
            method = "onClickSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/inventory/slot/InventorySlot;removeStack(I)Lnet/minecraft/item/ItemStack;",
                    ordinal = 2
            ),
            cancellable = true
    )
    private void ctrlQCrafting(int slotId, int dragType, ActionType clickTypeIn, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
        if (CarpetSettings.ctrlQCraftingFix && slotId == 0 && dragType == 1) {
            InventorySlot slot = this.slots.get(slotId);
            while (slot.hasStack()) {
                this.onClickSlot(slotId, 0, ActionType.THROW, player);
            }
            this.updateListeners();
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
