package carpet.mixin.missingTools;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;

@Mixin(PickaxeItem.class)
public class PickaxeItemMixin extends ToolItem {
    protected PickaxeItemMixin(float attackDamageIn, float attackSpeedIn, ToolMaterialType materialIn, Set<Block> effectiveBlocksIn) {
        super(attackDamageIn, attackSpeedIn, materialIn, effectiveBlocksIn);
    }

    @Inject(
            method = "getBlockBreakingSpeed",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/block/BlockState;getMaterial()Lnet/minecraft/block/material/Material;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void missingTools(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir, Material material) {
        if (!CarpetSettings.missingTools) return;
        if (material == Material.PISTON || material == Material.GLASS) cir.setReturnValue(miningSpeed);
    }
}
