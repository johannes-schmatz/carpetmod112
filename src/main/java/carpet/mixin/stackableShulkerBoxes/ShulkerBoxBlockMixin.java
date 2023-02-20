package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetSettings;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxBlockMixin {
    @Redirect(
            method = "onBreaking",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;setNbt(Lnet/minecraft/nbt/NbtCompound;)V"
            )
    )
    private void avoidEmptyTag(ItemStack itemStack, NbtCompound nbt) {
        if (CarpetSettings.stackableEmptyShulkerBoxes && nbt.isEmpty()) return;
        itemStack.setNbt(nbt);
    }
}
