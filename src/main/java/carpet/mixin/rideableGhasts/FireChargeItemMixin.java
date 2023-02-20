package carpet.mixin.rideableGhasts;

import carpet.CarpetSettings;
import carpet.helpers.GhastHelper;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireChargeItem.class)
public class FireChargeItemMixin extends Item {
    @Override
    public TypedActionResult<ItemStack> method_13649(World world, PlayerEntity player, Hand hand) {
        if (!(CarpetSettings.rideableGhasts && player.getVehicle() instanceof GhastEntity)) {
            return super.method_13649(world, player, hand);
        }
        ItemStack itemstack = player.getStackInHand(hand);
        GhastEntity ghast = (GhastEntity) player.getVehicle();
        player.getItemCooldownManager().method_11384(this, 40);
        GhastHelper.set_off_fball(ghast, world, player);
        if (!player.abilities.creativeMode) {
            itemstack.decrement(1);
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
    }
}
