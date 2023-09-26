package carpet.mixin.nitwitCrafter;

import carpet.CarpetSettings;
import carpet.helpers.EntityAICrafter;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.mob.passive.PassiveEntity;
import net.minecraft.entity.living.mob.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends PassiveEntity {
    private EntityAICrafter craftingAI;

    @Shadow public abstract int getProfession();

    @Shadow @Final private SimpleInventory f_0078634;

    public VillagerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;I)V",
            at = @At("RETURN")
    )
    private void onInit(World worldIn, int professionId, CallbackInfo ci) {
        craftingAI = new EntityAICrafter((VillagerEntity) (Object) this);
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
        if (CarpetSettings.nitwitCrafter && getProfession() == 5) {
            craftingAI.updateNitwit();
            goalSelector.addGoal(6, craftingAI);
        }
    }

    @Inject(
            method = "onKilled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/mob/passive/PassiveEntity;onKilled(Lnet/minecraft/entity/damage/DamageSource;)V"
            )
    )
    private void onDeath(DamageSource cause, CallbackInfo ci) {
        if (CarpetSettings.nitwitCrafter && getProfession() == 5 || CarpetSettings.villagerInventoryDropFix) {
            craftingAI.dropInventory();
        }
    }

    @Inject(
            method = "m_5773335",
            at = @At("HEAD"),
            cancellable = true
    )
    private void emptyNitwitBuyingList(CallbackInfo ci) {
        if (CarpetSettings.nitwitCrafter && getProfession() == 5) {
            ci.cancel();
        }
    }

    @Inject(
            method = "m_3739998",
            at = @At("HEAD"),
            cancellable = true
    )
    private void updateCraftingEquipment(ItemEntity itemEntity, CallbackInfo ci) {
        if (CarpetSettings.nitwitCrafter && craftingAI != null) {
            if (craftingAI.updateEquipment(itemEntity, f_0078634)) ci.cancel();
        }
    }
}
