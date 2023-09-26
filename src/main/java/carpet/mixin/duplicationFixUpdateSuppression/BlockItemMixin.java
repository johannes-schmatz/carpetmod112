package carpet.mixin.duplicationFixUpdateSuppression;

import carpet.CarpetSettings;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlaceableItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({
    BlockItem.class,
    PlaceableItem.class
})
public class BlockItemMixin {
    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;decrease(I)V"
            )
    )
    private void vanillaShrink(ItemStack stack, int quantity) {
        if (!CarpetSettings.duplicationFixUpdateSuppression) stack.decrease(quantity);
    }

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;I)Z"
            )
    )
    private boolean setBlockState(World world, BlockPos pos, BlockState newState, int flags, PlayerEntity player, World worldIn, BlockPos pos1, InteractionHand hand) {
        if (!CarpetSettings.duplicationFixUpdateSuppression) return world.setBlockState(pos, newState, flags);
        ItemStack stack = player.getHandStack(hand);
        stack.decrease(1);
        if (world.setBlockState(pos, newState, flags)) return true;
        // set block failed
        stack.increase(1);
        return false;
    }
}
