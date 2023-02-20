package carpet.mixin.craftingWindowDuplication;

import carpet.CarpetSettings;
import carpet.utils.extensions.DupingPlayer;
import carpet.utils.extensions.ExtendedItemStack;

import net.minecraft.class_3345;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.recipe.RecipeType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3345.class)
public class ServerRecipeBookHelperMixin {
    @Shadow private CraftingInventory field_16363;
    @Shadow private CraftingResultInventory field_16362;

    // Intentional duping bug added back for compatibility with 12.0, community request. CARPET-XCOM
    @Inject(
            method = "method_14907",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_3345;method_14908()V",
                    shift = At.Shift.AFTER
            )
    )
    private void craftingWindowDupingBugAddedBack(ServerPlayerEntity player, RecipeType p_194327_2_, boolean p_194327_3_, CallbackInfo ci){
        if (!CarpetSettings.craftingWindowDuplication) return;
        int slot = ((DupingPlayer) player).getDupeItem();
        if(slot == Integer.MIN_VALUE) return;
        ItemStack dupeItem = player.inventory.getInvStack(slot);
        if(dupeItem.isEmpty()) return;

        int size = dupeItem.getCount();

        for (int j = 0; j < this.field_16363.getInvSize(); ++j)
        {
            ItemStack itemstack = this.field_16363.getInvStack(j);

            if (!itemstack.isEmpty())
            {
                size += itemstack.getCount();
                itemstack.setCount(0);
            }
        }

        ((ExtendedItemStack) (Object) dupeItem).forceStackSize(size);
        field_16362.clear();
        ((DupingPlayer) player).clearDupeItem();
    }
}
