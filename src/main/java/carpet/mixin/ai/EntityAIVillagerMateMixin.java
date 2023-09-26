package carpet.mixin.ai;

import carpet.helpers.AIHelper;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.VillagerMatingGoal;
import net.minecraft.entity.living.mob.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerMatingGoal.class)
public abstract class EntityAIVillagerMateMixin extends Goal {
    @Shadow @Final private VillagerEntity villagerOne;

    @Shadow private int breedTimer;


    @Inject(
            method = "canStart",
            at = @At("HEAD")
    )
    private void readyToMate(CallbackInfoReturnable<Boolean> cir) {
        if (this.villagerOne.getBreedingAge() < 5 && this.villagerOne.getBreedingAge() > 0) {
            AIHelper.setDetailedInfo(this.villagerOne, this, "Ready to Mate");
        }
    }

    @Inject(
            method = "canStart",
            at = @At(
                    value = "RETURN",
                    ordinal = 0
            )
    )
    private void waiting(CallbackInfoReturnable<Boolean> cir) {
        int growingAge = this.villagerOne.getBreedingAge();
        if (growingAge >= 5) {
            AIHelper.setDetailedInfo(this.villagerOne, this, () -> "Waiting: " + growingAge);
        }
    }

    @Inject(
            method = "canStart",
            at = @At(
                    value = "RETURN",
                    ordinal = 2
            )
    )
    private void outsideOfVillage(CallbackInfoReturnable<Boolean> cir) {
        AIHelper.setDetailedInfo(this.villagerOne, this, "Outside of a village");
    }

    @Inject(
            method = "canStart",
            at = @At(
                    value = "RETURN",
                    ordinal = 5
            )
    )
    private void dontWantToMate(CallbackInfoReturnable<Boolean> cir) {
        AIHelper.setDetailedInfo(this.villagerOne, this, "Don't want to mate");
    }

    @Inject(
            method = "start",
            at = @At("RETURN")
    )
    private void inLove300(CallbackInfo ci) {
        AIHelper.setDetailedInfo(this.villagerOne, this, "In love: 300");
    }

    @Inject(
            method = "stop",
            at = @At("RETURN")
    )
    private void onResetTask(CallbackInfo ci) {
        AIHelper.setDetailedInfo(this.villagerOne, this, "Ready to Mate");
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/control/LookControl;setLookatValues(Lnet/minecraft/entity/Entity;FF)V"
            )
    )
    private void onUpdateTask(CallbackInfo ci) {
        int matingTimeout = this.breedTimer;
        if (matingTimeout > 0) {
            AIHelper.setDetailedInfo(this.villagerOne, this, () -> "In love: " + matingTimeout);
        } else {
            AIHelper.setDetailedInfo(this.villagerOne, this, "Ready to Mate");
        }
    }
}
