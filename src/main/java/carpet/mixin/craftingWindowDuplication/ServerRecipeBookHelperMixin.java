package carpet.mixin.craftingWindowDuplication;

import carpet.CarpetSettings;
import carpet.utils.extensions.DupingPlayer;
import carpet.utils.extensions.ExtendedItemStack;

import net.minecraft.crafting.recipe.Recipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.ResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.crafting.RecipePlacementHelper;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipePlacementHelper.class)
public class ServerRecipeBookHelperMixin {
    @Shadow private CraftingInventory craftingInventory;
    @Shadow private ResultInventory resultInventory;

    // Intentional duping bug added back for compatibility with 12.0, community request. CARPET-XCOM
    @Inject(
            method = "clickRecipe(Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;Lnet/minecraft/crafting/recipe/Recipe;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/crafting/RecipePlacementHelper;clickRecipe()V",
                    shift = At.Shift.AFTER
            )
    )
    private void craftingWindowDupingBugAddedBack(ServerPlayerEntity player, Recipe p_194327_2_, boolean p_194327_3_, CallbackInfo ci){
        if (!CarpetSettings.craftingWindowDuplication) return;
        int slot = ((DupingPlayer) player).getDupeItem();
        if(slot == Integer.MIN_VALUE) return;
        ItemStack dupeItem = player.inventory.getStack(slot);
        if(dupeItem.isEmpty()) return;

        int size = dupeItem.getSize();

        for (int j = 0; j < this.craftingInventory.getSize(); ++j)
        {
            ItemStack itemstack = this.craftingInventory.getStack(j);

            if (!itemstack.isEmpty())
            {
                size += itemstack.getSize();
                itemstack.setSize(0);
            }
        }

        ((ExtendedItemStack) (Object) dupeItem).forceStackSize(size);
        resultInventory.clear();
        ((DupingPlayer) player).clearDupeItem();
    }
}
