package carpet.mixin.craftingWindowDuplication;

import carpet.CarpetSettings;
import carpet.utils.extensions.DupingPlayer;
import carpet.utils.extensions.ExtendedItemStack;

import net.minecraft.crafting.recipe.CraftingRecipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.ResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.unmapped.C_1544625;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C_1544625.class)
public class ServerRecipeBookHelperMixin {
    @Shadow private CraftingInventory f_3101989;
    @Shadow private ResultInventory f_6356755;

    // Intentional duping bug added back for compatibility with 12.0, community request. CARPET-XCOM
    @Inject(
            method = "m_1260733",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/unmapped/C_1544625;m_0962979()V",
                    shift = At.Shift.AFTER
            )
    )
    private void craftingWindowDupingBugAddedBack(ServerPlayerEntity player, CraftingRecipe p_194327_2_, boolean p_194327_3_, CallbackInfo ci){
        if (!CarpetSettings.craftingWindowDuplication) return;
        int slot = ((DupingPlayer) player).getDupeItem();
        if(slot == Integer.MIN_VALUE) return;
        ItemStack dupeItem = player.inventory.getStack(slot);
        if(dupeItem.isEmpty()) return;

        int size = dupeItem.getSize();

        for (int j = 0; j < this.f_3101989.getSize(); ++j)
        {
            ItemStack itemstack = this.f_3101989.getStack(j);

            if (!itemstack.isEmpty())
            {
                size += itemstack.getSize();
                itemstack.setSize(0);
            }
        }

        ((ExtendedItemStack) (Object) dupeItem).forceStackSize(size);
        f_6356755.clear();
        ((DupingPlayer) player).clearDupeItem();
    }
}
