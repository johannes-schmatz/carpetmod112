package carpet.mixin.accessors;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingInventory.class)
public interface CraftingInventoryAccessor {
    @Accessor("field_15100")
    DefaultedList<ItemStack> getStacks();
    @Mutable @Accessor("screenHandler") void setContainer(ScreenHandler container);
}
