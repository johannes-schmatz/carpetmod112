package carpet.mixin.stackableShulkerBoxes;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScreenHandler.class)
public class ContainerMixin {
    @Redirect(
            method = "method_3252",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;equalsIgnoreDamage(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
                    ordinal = 0
            )
    )
    private boolean areTagsEqualAndStackable(ItemStack stackA, ItemStack stackB) {
        // Check If item can stack, Always true in vanilla CARPET-XCOM
        return ItemStack.equals(stackA, stackB) && stackB.isStackable();
    }
}
