package carpet.mixin.accessors;

import com.google.gson.JsonObject;
import net.minecraft.recipe.RecipeDispatcher;
import net.minecraft.recipe.RecipeType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeDispatcher.class)
public interface RecipeManagerAccessor {
    @Invoker("load") static RecipeType invokeParseRecipeJson(JsonObject json) { throw new AbstractMethodError(); }
}
