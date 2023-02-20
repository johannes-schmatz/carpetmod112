package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.recipe.RecipeDispatcher;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.class_3345;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3345.class)
public class ServerRecipeBookHelperMixin {
    @Inject(
            method = "method_14907",
            at = @At("HEAD")
    )
    private void logRecipes(ServerPlayerEntity p_194327_1_, RecipeType p_194327_2_, boolean p_194327_3_, CallbackInfo ci) {
        // Added debugger for the instance people need help debuging why there recipes don't work. CARPET-XCOM
        if (LoggerRegistry.__recipes) {
            int i = 0;
            for (Identifier r : RecipeDispatcher.REGISTRY.getKeySet()) {
                String index = Integer.toString(i);
                LoggerRegistry.getLogger("recipes").log(() -> new Text[]{
                        Messenger.s(null, index + ": " + r.getPath())
                });
                i++;
            }
        }
    }
}
