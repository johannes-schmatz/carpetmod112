package carpet.mixin.accessors;

import net.minecraft.crafting.recipe.Ingredient;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Ingredient.class)
public interface IngredientAccessor {
    @Accessor("stacks") ItemStack[] getStacks();
}
