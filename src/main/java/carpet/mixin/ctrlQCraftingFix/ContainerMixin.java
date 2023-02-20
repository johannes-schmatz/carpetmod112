package carpet.mixin.ctrlQCraftingFix;

import carpet.CarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ItemAction;

import java.util.List;

@Mixin(ScreenHandler.class)
public abstract class ContainerMixin {
    @Shadow public List<Slot> slots;
    @Shadow public abstract ItemStack method_3252(int slotId, int dragType, ItemAction clickTypeIn, PlayerEntity player);
    @Shadow public abstract void sendContentUpdates();

    @Inject(
            method = "method_3252",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/inventory/slot/Slot;takeStack(I)Lnet/minecraft/item/ItemStack;",
                    ordinal = 2
            ),
            cancellable = true
    )
    private void ctrlQCrafting(int slotId, int dragType, ItemAction clickTypeIn, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
        if (CarpetSettings.ctrlQCraftingFix && slotId == 0 && dragType == 1) {
            Slot slot = this.slots.get(slotId);
            while (slot.hasStack()) {
                this.method_3252(slotId, 0, ItemAction.THROW, player);
            }
            this.sendContentUpdates();
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
