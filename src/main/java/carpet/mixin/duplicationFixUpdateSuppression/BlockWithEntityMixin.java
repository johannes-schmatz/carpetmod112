package carpet.mixin.duplicationFixUpdateSuppression;

import carpet.CarpetSettings;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.BlockWithBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShulkerBoxItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockWithBlockEntity.class)
public class BlockWithEntityMixin {
    @Inject(
            method = "afterMinedByPlayer",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/item/Item;I)Lnet/minecraft/item/ItemStack;"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void fixShulkerBox(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity te, ItemStack stack, CallbackInfo ci, int fortune, Item item) {
        // Remove ability to drop shulker boxes given the set block to air already does it. This causes duplication with duplicationFixUpdateSuppression.
        // In vanilla this behavior never triggers CARPET-XCOM
        if(CarpetSettings.duplicationFixUpdateSuppression && item instanceof ShulkerBoxItem) ci.cancel();
    }
}
