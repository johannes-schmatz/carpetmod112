package carpet.mixin.accessors;

import com.google.gson.JsonObject;

import net.minecraft.crafting.CraftingManager;
import net.minecraft.crafting.recipe.CraftingRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CraftingManager.class)
public interface RecipeManagerAccessor {
    @Invoker("parseRecipe") static CraftingRecipe invokeParseRecipeJson(JsonObject json) { throw new AbstractMethodError(); }
}
