package carpet.mixin.autoCraftingTable;

import carpet.helpers.ContainerAutoCraftingTable;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.menu.CraftingTableMenu;
import net.minecraft.server.crafting.RecipePlacementHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RecipePlacementHelper.class)
public class ServerRecipeBookHelperMixin {
    @Redirect(
            method = "clickRecipe(Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;Lnet/minecraft/crafting/recipe/Recipe;Z)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/inventory/menu/CraftingTableMenu;craftingTable:Lnet/minecraft/inventory/CraftingInventory;"
            )
    )
    private CraftingInventory getCraftMatrix(CraftingTableMenu container) {
        return container instanceof ContainerAutoCraftingTable ? ((ContainerAutoCraftingTable) container).getInventoryCrafting() : container.craftingTable;
    }
}
