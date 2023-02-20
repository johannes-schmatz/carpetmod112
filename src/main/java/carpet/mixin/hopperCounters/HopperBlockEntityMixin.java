package carpet.mixin.hopperCounters;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.helpers.HopperCounter;
import carpet.utils.WoolTool;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.class_2737;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends class_2737 {
    @Shadow public abstract int getInvSize();
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();

    @Inject(
            method = "insert",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onPush(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.hopperCounters != CarpetSettings.HopperCounters.off) {
            String counter = getCounterName();
            if (counter != null) {
                for (int i = 0; i < this.getInvSize(); ++i) {
                    if (!this.getInvStack(i).isEmpty()) {
                        ItemStack itemstack = this.getInvStack(i);//.copy();
                        HopperCounter.COUNTERS.get(counter).add(this.getEntityWorld().getServer(), itemstack);
                        this.setInvStack(i, ItemStack.EMPTY);
                    }
                }
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private String getCounterName() {
        if (CarpetSettings.hopperCounters == CarpetSettings.HopperCounters.all) return "all";
        // TODO: why create BlockPos from doubles, gets floored anyways
        BlockPos woolPos = new BlockPos(getX(), getY(), getZ()).offset(HopperBlock.getDirection(this.getDataValue()));
        CarpetClientChunkLogger.setReason("Hopper loading");
        DyeColor wool_color = WoolTool.getWoolColorAtPosition(getEntityWorld(), woolPos);
        CarpetClientChunkLogger.resetToOldReason();
        return wool_color.asString();
    }
}
