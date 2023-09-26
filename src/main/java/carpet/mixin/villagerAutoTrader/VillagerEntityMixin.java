package carpet.mixin.villagerAutoTrader;

import carpet.CarpetSettings;
import carpet.helpers.EntityAIAutotrader;
import carpet.utils.extensions.AutotraderVillagerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.living.mob.passive.PassiveEntity;
import net.minecraft.entity.living.mob.passive.VillagerEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.minecraft.world.village.trade.TradeOffer;
import net.minecraft.world.village.trade.TradeOffers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.Nullable;
import java.util.LinkedList;
import java.util.List;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends PassiveEntity implements AutotraderVillagerEntity {
    @Shadow @Nullable private TradeOffers traderOffers;
    private EntityAIAutotrader autotraderAI;
    private TradeOffers buyingListsorted;
    private final List<Integer> sortedTradeList = new LinkedList<>();

    public VillagerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;I)V",
            at = @At("RETURN")
    )
    private void onInit(World worldIn, int professionId, CallbackInfo ci) {
        autotraderAI = new EntityAIAutotrader((VillagerEntity) (Object) this);
    }



    @Inject(
            method = {
                    "m_4674327",
                    "m_7338286"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/mob/passive/VillagerEntity;getProfession()I"
            )
    )
    private void addCraftTask(CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            goalSelector.addGoal(6, autotraderAI);
        }
    }

    @Inject(
            method = "writeCustomNbt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/village/trade/TradeOffers;toNbt()Lnet/minecraft/nbt/NbtCompound;"
            )
    )
    private void writeOffersSorted(NbtCompound compound, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            compound.put("OffersSorted", autotraderAI.getRecipiesForSaving(sortedTradeList));
        }
    }

    @Inject(
            method = "readCustomNbt",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/living/mob/passive/VillagerEntity;traderOffers:Lnet/minecraft/world/village/trade/TradeOffers;",
                    shift = At.Shift.AFTER
            )
    )
    private void readOffersSorted(NbtCompound compound, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            autotraderAI.setRecipiesForSaving(compound.getCompound("OffersSorted"), sortedTradeList);
        }
    }

    @Inject(
            method = "setCustomer",
            at = @At("RETURN")
    )
    private void onSetCustomer(PlayerEntity player, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader && player != null) {
            autotraderAI.sortRepopulatedSortedList(traderOffers, buyingListsorted, sortedTradeList);
        }
    }

    @Inject(
            method = "getOffers",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getRecipes(PlayerEntity player, CallbackInfoReturnable<TradeOffers> cir) {
        if (CarpetSettings.villagerAutoTrader) {
            if (this.buyingListsorted == null) {
                buyingListsorted = new TradeOffers();
                autotraderAI.sortRepopulatedSortedList(traderOffers, buyingListsorted, sortedTradeList);
            } else if (buyingListsorted.size() == 0) {
                autotraderAI.sortRepopulatedSortedList(traderOffers, buyingListsorted, sortedTradeList);
            }
            cir.setReturnValue(buyingListsorted);
        }
    }

    @Inject(
            method = "m_5773335",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/living/mob/passive/VillagerEntity;traderOffers:Lnet/minecraft/world/village/trade/TradeOffers;",
                    ordinal = 0
            )
    )
    private void initBuyingListSorted(CallbackInfo ci) {
        if (this.buyingListsorted == null) {
            this.buyingListsorted = new TradeOffers();
        }
    }

    @Inject(
            method = "setCustomer",
            at = @At("RETURN")
    )
    private void onPopulateDone(CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            autotraderAI.sortRepopulatedSortedList(traderOffers, buyingListsorted, sortedTradeList);
        }
    }

    @Inject(
            method = "m_3739998",
            at = @At("RETURN")
    )
    private void onUpdateEquipment(ItemEntity itemEntity, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader && autotraderAI != null) {
            if (buyingListsorted == null) {
                buyingListsorted = new TradeOffers();
                autotraderAI.sortRepopulatedSortedList(traderOffers, buyingListsorted, sortedTradeList);
            }
            if (!itemEntity.removed) {
                autotraderAI.updateEquipment(itemEntity, buyingListsorted);
            }
        }
    }

    @Override
    public void addToFirstList(TradeOffer merchantrecipe) {
        if(!CarpetSettings.villagerAutoTrader) return;
        autotraderAI.addToFirstList(traderOffers, merchantrecipe, sortedTradeList);
    }
}
