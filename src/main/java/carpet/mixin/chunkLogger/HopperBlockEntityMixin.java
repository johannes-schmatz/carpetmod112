package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Hopper;
import net.minecraft.inventory.Inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Shadow protected abstract Inventory getTargetInventory();

    @Shadow public static Inventory getInventoryAbove(Hopper hopper) { throw new AbstractMethodError(); }

    @Redirect(
            method = "pushItems()Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/HopperBlockEntity;getTargetInventory()Lnet/minecraft/inventory/Inventory;"
            )
    )
    private Inventory getInventoryForHopperTransferAndLog(HopperBlockEntity hopper) {
        try {
            CarpetClientChunkLogger.setReason("Hopper loading");
            return getTargetInventory();
        } finally {
            CarpetClientChunkLogger.resetToOldReason();
        }
    }

    @Redirect(
            method = "pullItems(Lnet/minecraft/inventory/Hopper;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInventoryAbove(Lnet/minecraft/inventory/Hopper;)Lnet/minecraft/inventory/Inventory;"
            )
    )
    private static Inventory getSourceInventoryAndLog(Hopper hopper) {
        try {
            CarpetClientChunkLogger.setReason("Hopper self-loading");
            return getInventoryAbove(hopper);
        } finally {
            CarpetClientChunkLogger.resetToOldReason();
        }
    }
}
