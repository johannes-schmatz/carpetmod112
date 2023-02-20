package carpet.mixin.autoCraftingTable;

import carpet.helpers.ContainerAutoCraftingTable;
import net.minecraft.class_3345;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.screen.CraftingScreenHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_3345.class)
public class ServerRecipeBookHelperMixin {
    @Redirect(
            method = "method_14907",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/screen/CraftingScreenHandler;craftingInv:Lnet/minecraft/inventory/CraftingInventory;"
            )
    )
    private CraftingInventory getCraftMatrix(CraftingScreenHandler container) {
        return container instanceof ContainerAutoCraftingTable ? ((ContainerAutoCraftingTable) container).getInventoryCrafting() : container.craftingInv;
    }
}
