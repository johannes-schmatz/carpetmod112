package carpet.mixin.accessors;

import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DispenserBlockEntity.class)
public interface DispenserBlockEntityAccessor {
    @Accessor("field_15153")
    DefaultedList<ItemStack> getInventory();
}
