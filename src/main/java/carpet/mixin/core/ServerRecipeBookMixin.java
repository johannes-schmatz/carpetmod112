package carpet.mixin.core;

import carpet.helpers.CustomCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import net.minecraft.class_3356;
import net.minecraft.recipe.RecipeType;
import net.minecraft.entity.player.ServerPlayerEntity;

@Mixin(class_3356.class)
public class ServerRecipeBookMixin {
    private static final ThreadLocal<ServerPlayerEntity> tlPlayer = new ThreadLocal<>();

    @Redirect(
            method = {
                    "method_14995",
                    "method_14998"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private boolean filter(List<RecipeType> list, Object e, List<RecipeType> recipesIn, ServerPlayerEntity player) {
        RecipeType recipe = (RecipeType) e;
        if (!CustomCrafting.filterCustomRecipesForOnlyCarpetClientUsers(recipe, player)) return false;
        return list.add(recipe);
    }

    @Redirect(
            method = "method_15001",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private boolean filter(List<RecipeType> list, Object e) {
        RecipeType recipe = (RecipeType) e;
        ServerPlayerEntity player = tlPlayer.get();
        if (player != null && !CustomCrafting.filterCustomRecipesForOnlyCarpetClientUsers(recipe, player)) return false;
        if (recipe == null) System.out.println("found null recipe");
        return list.add(recipe);
    }

    @Inject(
            method = "method_14997",
            at = @At("HEAD")
    )
    private void onInitStart(ServerPlayerEntity player, CallbackInfo ci) {
        tlPlayer.set(player);
    }

    @Inject(
            method = "method_14997",
            at = @At("RETURN")
    )
    private void onInitEnd(ServerPlayerEntity player, CallbackInfo ci) {
        tlPlayer.set(null);
    }
}
