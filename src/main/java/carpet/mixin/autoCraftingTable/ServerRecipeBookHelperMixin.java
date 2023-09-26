package carpet.mixin.autoCraftingTable;

import carpet.helpers.ContainerAutoCraftingTable;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.menu.CraftingTableMenu;
import net.minecraft.unmapped.C_1544625;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(C_1544625.class)
public class ServerRecipeBookHelperMixin {
    @Redirect(
            method = "m_1260733",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/inventory/menu/CraftingTableMenu;craftingTable:Lnet/minecraft/inventory/CraftingInventory;"
            )
    )
    private CraftingInventory getCraftMatrix(CraftingTableMenu container) {
        return container instanceof ContainerAutoCraftingTable ? ((ContainerAutoCraftingTable) container).getInventoryCrafting() : container.craftingTable;
    }
}
