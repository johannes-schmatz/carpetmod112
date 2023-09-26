package carpet.mixin.villagerAutoTrader;

import carpet.utils.extensions.AutotraderVillagerEntity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.living.mob.passive.Trader;
import net.minecraft.entity.living.mob.passive.VillagerEntity;
import net.minecraft.world.village.trade.TradeOffer;
import net.minecraft.world.village.trade.TraderInventory;

@Mixin(TraderInventory.class)
public class TraderInventoryMixin {
    @Shadow @Final private Trader trader;
    @Shadow private TradeOffer offer;

    @Inject(
            method = "updateOffer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/village/trade/TraderInventory;setStack(ILnet/minecraft/item/ItemStack;)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void addToFirstList(CallbackInfo ci) {
        if (trader instanceof VillagerEntity) {
            ((AutotraderVillagerEntity) trader).addToFirstList(offer);
        }
    }
}
