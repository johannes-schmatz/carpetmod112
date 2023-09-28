package carpet.mixin.elytraTradable;

import carpet.CarpetSettings;

import net.minecraft.entity.living.mob.passive.PassiveEntity;
import net.minecraft.entity.living.mob.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.world.village.trade.TradeOffer;
import net.minecraft.world.village.trade.TradeOffers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends PassiveEntity {
    @Shadow public abstract int getProfession();
    @Shadow private int career;
    @Shadow private int careerLevel;
    @Shadow private TradeOffers traderOffers;

    public VillagerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "m_5773335()V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/living/mob/passive/VillagerEntity;career:I",
                    ordinal = 2
            )
    )
    private void addElytra(CallbackInfo ci) {
        // leatherworker: profession=4, career=2
        if (CarpetSettings.elytraTradable && getProfession() == 4 && career == 2 && careerLevel == 4) {
            int leatherAmount = 15 + this.random.nextInt(64 - 15 + 1);
            int emeraldAmount = 20 + this.random.nextInt(64 - 20 + 1);
            this.traderOffers.add(new TradeOffer(
                new ItemStack(Items.LEATHER, leatherAmount),
                new ItemStack(Items.EMERALD, emeraldAmount),
                new ItemStack(Items.ELYTRA)
            ));
        }
    }
}
