package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.BlockEntityOptimizer;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.HopperProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin implements BlockEntityOptimizer.LazyBlockEntity {
    @Shadow protected abstract boolean isHopperEmpty();
    @Shadow protected abstract boolean isFull();

    // CARPET-optimizedTileEntities: Whether the tile entity is asleep or not. Hoppers have 2 different actions that can sleep: pushing and pulling.
    // False by default so tile entities wake up upon chunk loading
    private boolean pullSleeping = false;
    private boolean pushSleeping = false;

    @Override
    public void wakeUp(){
        this.pullSleeping = false;
        this.pushSleeping = false;
    }

    @Redirect(
            method = "insertAndExtract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/HopperBlockEntity;isHopperEmpty()Z"
            )
    )
    private boolean isInventoryEmptyOrOptimized(HopperBlockEntity hopper) {
        if (CarpetSettings.optimizedTileEntities) {
            pushSleeping = pushSleeping || isHopperEmpty();
            return pushSleeping;
        }
        return isHopperEmpty();
    }

    @Redirect(
            method = "insertAndExtract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/HopperBlockEntity;isFull()Z"
            )
    )
    private boolean isFullOrOptimized(HopperBlockEntity hopper) {
        if (CarpetSettings.optimizedTileEntities) {
            pullSleeping = pullSleeping || isFull();
            return pullSleeping;
        }
        return isFull();
    }

    @Inject(
            method = "insert",
            at = @At(
                    value = "RETURN",
                    ordinal = 0
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/block/entity/HopperBlockEntity;isInventoryFull(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/util/math/Direction;)Z"
                    )
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void setPushSleep(CallbackInfoReturnable<Boolean> cir, Inventory output) {
        // Push falls asleep if the container it would push into is full and
        // is an actual tile entity (not a minecart). This is because minecarts do not cause comparator updates and would keep the
        // hopper in a sleeping push state when leaving or emptying
        pushSleeping = output instanceof LockableContainerBlockEntity;
    }

    @Inject(
            method = "extract(Lnet/minecraft/util/HopperProvider;)Z",
            at = @At(
                    value = "RETURN",
                    ordinal = 0
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/block/entity/HopperBlockEntity;isInventoryEmpty(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/util/math/Direction;)Z"
                    )
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void setPullSleep1(HopperProvider hopper, CallbackInfoReturnable<Boolean> cir, Inventory input) {
        // Pull falls asleep if the container it would pull from is empty and
        // is an actual tile entity (not a minecart). This is because minecarts do not cause comparator updates and would keep the
        // hopper in a sleeping pull state when leaving or filling up
        if (hopper instanceof HopperBlockEntityMixin) {
            ((HopperBlockEntityMixin) hopper).pullSleeping = input instanceof LockableContainerBlockEntity;
        }
    }

    @Inject(
            method = "extract(Lnet/minecraft/util/HopperProvider;)Z",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void setPullSleep2(HopperProvider hopper, CallbackInfoReturnable<Boolean> cir) {
        // There is a non-empty inventory above the hopper, but for some reason the hopper cannot suck
        // items from it. Therefore the hopper pulling should sleep (if the inventory is not a minecart).
        if (hopper instanceof HopperBlockEntityMixin) {
            ((HopperBlockEntityMixin) hopper).pullSleeping = CarpetSettings.optimizedTileEntities && HopperBlockEntity.getInputInventory(hopper) instanceof LockableContainerBlockEntity;
        }
    }
}
