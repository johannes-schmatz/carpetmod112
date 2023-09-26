package carpet.mixin.accessors;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.menu.InventoryMenu;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingInventory.class)
public interface CraftingInventoryAccessor {
    @Accessor("stacks")
    DefaultedList<ItemStack> getStacks();
    @Mutable @Accessor("menu") void setContainer(InventoryMenu container);
}
