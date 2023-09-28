package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;

import net.minecraft.crafting.CraftingManager;
import net.minecraft.crafting.recipe.Recipe;
import net.minecraft.server.crafting.RecipePlacementHelper;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.resource.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipePlacementHelper.class)
public class ServerRecipeBookHelperMixin {
    @Inject(
            method = "clickRecipe(Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;Lnet/minecraft/crafting/recipe/Recipe;Z)V",
            at = @At("HEAD")
    )
    private void logRecipes(ServerPlayerEntity p_194327_1_, Recipe p_194327_2_, boolean p_194327_3_, CallbackInfo ci) {
        // Added debugger for the instance people need help debuging why there recipes don't work. CARPET-XCOM
        if (LoggerRegistry.__recipes) {
            int i = 0;
            for (Identifier r : CraftingManager.REGISTRY.keySet()) {
                String index = Integer.toString(i);
                LoggerRegistry.getLogger("recipes").log(() -> new Text[]{
                        Messenger.s(null, index + ": " + r.getPath())
                });
                i++;
            }
        }
    }
}
