package carpet.mixin.rideableGhasts;

import carpet.CarpetSettings;
import carpet.helpers.GhastHelper;
import net.minecraft.entity.living.mob.GhastEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireChargeItem.class)
public class FireChargeItemMixin extends Item {
    @Override
    public InteractionResultHolder<ItemStack> startUsing(World world, PlayerEntity player, InteractionHand hand) {
        if (!(CarpetSettings.rideableGhasts && player.getVehicle() instanceof GhastEntity)) {
            return super.startUsing(world, player, hand);
        }
        ItemStack itemstack = player.getHandStack(hand);
        GhastEntity ghast = (GhastEntity) player.getVehicle();
        player.getItemCooldownManager().set(this, 40);
        GhastHelper.setOffFireBall(ghast, world, player);
        if (!player.abilities.creativeMode) {
            itemstack.decrease(1);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }
}
