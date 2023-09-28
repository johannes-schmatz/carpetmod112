package carpet.mixin.stackableShulkerBoxes;

import carpet.CarpetMod;
import carpet.mixin.accessors.ItemEntityAccessor;
import carpet.utils.extensions.ExtendedItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow public abstract ItemStack getItemStack();
    @Shadow private int pickUpDelay;
    @Shadow private int age;

    public ItemEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(
            method = "onPlayerCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;add(Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private boolean insertStack(PlayerInventory inventory, ItemStack stack) {
        try {
            CarpetMod.playerInventoryStacking.set(Boolean.TRUE);
            return inventory.add(stack);
        } finally {
            CarpetMod.playerInventoryStacking.set(Boolean.FALSE);
        }
    }

    @Inject(
            method = "canStack",
            at = @At(
                    value = "RETURN",
                    ordinal = 0
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;getMaxSize()I"
                    )
            ),
            cancellable = true
    )
    private void doGroundStacking(ItemEntity other, CallbackInfoReturnable<Boolean> cir) {
        ItemStack ownStack = this.getItemStack();
        ItemStack otherStack = other.getItemStack();
        // Add check for stacking shoulkers without NBT on the ground CARPET-XCOM
        if (((ExtendedItemStack) (Object) otherStack).isGroundStackable() && ((ExtendedItemStack) (Object) ownStack).isGroundStackable()) {
            otherStack.increase(ownStack.getSize());
            ((ItemEntityAccessor) other).setPickupDelay(Math.max(((ItemEntityAccessor) other).getPickupDelay(), this.pickUpDelay));
            ((ItemEntityAccessor) other).setAge(Math.min(((ItemEntityAccessor) other).getAge(), this.age));
            other.setItemStack(otherStack);
            this.remove();
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "canStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;increase(I)V"
            ),
            cancellable = true
    )
    private void checkStackable(ItemEntity other, CallbackInfoReturnable<Boolean> cir) {
        ItemStack ownStack = this.getItemStack();
        ItemStack otherStack = other.getItemStack();
        // make sure stackable items are checked before combining them, always true in vanilla CARPET-XCOM
        if (!ownStack.isStackable() || !otherStack.isStackable()) {
            cir.setReturnValue(false);
        }
    }
}
