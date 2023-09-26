package carpet.mixin.rideableGhasts;

import carpet.CarpetSettings;
import carpet.helpers.GhastHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.living.mob.GhastEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.entity.living.mob.GhastEntity$ShootFireballGoal")
public abstract class GhastShootFireballGoalMixin extends Goal {
    @Shadow @Final private GhastEntity ghast;

    /**
     * @author skyrising
     * @reason carpet
     */
    @Override
    @Overwrite
    public boolean canStart() {
        if (this.ghast.getTargetEntity() == null) {
            return false;
        }
        if (CarpetSettings.rideableGhasts && this.ghast.hasCustomName()) {
            if (this.ghast.hasPassengers()) {
                //reset the attack;
                this.stop();
                return false;
            }
            Entity at = this.ghast.getTargetEntity();
            if (at instanceof PlayerEntity && GhastHelper.is_yo_bro(this.ghast, (PlayerEntity) at)) {
                //reset the attack
                this.stop();
                return false;
            }
        }
        return true;
    }

    @Inject(
            method = "stop()V",
            at = @At("RETURN")
    )
    private void onReset(CallbackInfo ci) {
        if (CarpetSettings.rideableGhasts) {
            this.ghast.setAttackTarget(null);
        }
    }
}
