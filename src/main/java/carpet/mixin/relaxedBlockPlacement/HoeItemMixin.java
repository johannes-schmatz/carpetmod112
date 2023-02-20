package carpet.mixin.relaxedBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.HoeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HoeItem.class)
public class HoeItemMixin {
    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;getMaterial()Lnet/minecraft/block/material/Material;"
            )
    )
    private Material allowWater(BlockState state) {
        Material material = state.getMaterial();
        if (CarpetSettings.relaxedBlockPlacement && material == Material.WATER) return Material.AIR;
        return material;
    }
}
