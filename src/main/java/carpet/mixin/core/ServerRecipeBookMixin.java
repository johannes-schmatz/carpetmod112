package carpet.mixin.core;

import carpet.helpers.CustomCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import net.minecraft.crafting.recipe.CraftingRecipe;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.unmapped.C_5405916;

@Mixin(C_5405916.class)
public class ServerRecipeBookMixin {
    private static final ThreadLocal<ServerPlayerEntity> tlPlayer = new ThreadLocal<>();

    @Redirect(
            method = {
                    "m_7289487",
                    "m_5317270"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private boolean filter(List<CraftingRecipe> list, Object e, List<CraftingRecipe> recipesIn, ServerPlayerEntity player) {
        CraftingRecipe recipe = (CraftingRecipe) e;
        if (!CustomCrafting.filterCustomRecipesForOnlyCarpetClientUsers(recipe, player)) return false;
        return list.add(recipe);
    }

    @Redirect(
            method = "m_8402458",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private boolean filter(List<CraftingRecipe> list, Object e) {
        CraftingRecipe recipe = (CraftingRecipe) e;
        ServerPlayerEntity player = tlPlayer.get();
        if (player != null && !CustomCrafting.filterCustomRecipesForOnlyCarpetClientUsers(recipe, player)) return false;
        if (recipe == null) System.out.println("found null recipe");
        return list.add(recipe);
    }

    @Inject(
            method = "m_1614649",
            at = @At("HEAD")
    )
    private void onInitStart(ServerPlayerEntity player, CallbackInfo ci) {
        tlPlayer.set(player);
    }

    @Inject(
            method = "m_1614649",
            at = @At("RETURN")
    )
    private void onInitEnd(ServerPlayerEntity player, CallbackInfo ci) {
        tlPlayer.set(null);
    }
}
