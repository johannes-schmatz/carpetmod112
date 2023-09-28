package carpet.mixin.rideableGhasts;

import carpet.CarpetSettings;
import carpet.helpers.GhastHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.MobEntityPlayerTargetGoal;
import net.minecraft.entity.living.mob.FlyingEntity;
import net.minecraft.entity.living.mob.GhastEntity;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GhastEntity.class)
public class GhastEntityMixin extends FlyingEntity {
    public GhastEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "initGoals",
            at = @At("RETURN")
    )
    private void addNewTasks(CallbackInfo ci) {
        this.targetSelector.addGoal(3, new GhastHelper.AIFollowClues((GhastEntity) (Object) this));
        this.targetSelector.addGoal(4, new GhastHelper.AIFindOwner((GhastEntity) (Object) this));
    }

    @Redirect(
            method = "initGoals",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/entity/living/mob/MobEntity;)Lnet/minecraft/entity/ai/goal/MobEntityPlayerTargetGoal;"
            )
    )
    private MobEntityPlayerTargetGoal replaceTargetTask(MobEntity ghast) {
        return new GhastHelper.GhastEntityAIFindEntityNearestPlayer(ghast);
    }

    @Override
    public void tickAI() {
        super.tickAI();
        if (this.random.nextInt(400) == 0 && CarpetSettings.rideableGhasts && this.deathTime == 0 && this.hasPassengers()) {
            this.heal(1.0F);
        }
    }

    @Override
    protected boolean canInteract(PlayerEntity player, InteractionHand hand) {
        if (!(CarpetSettings.rideableGhasts) || this.hasPassengers()) {
            return super.canInteract(player, hand);
        }
        if (!GhastHelper.is_yo_bro((GhastEntity) (Object) this, player)) {
            return super.canInteract(player, hand);
        }
        boolean worked = super.canInteract(player, hand);
        if (!worked) {
            player.startRiding(this, true);
            player.getItemCooldownManager().set(Items.FIRE_CHARGE, 1);
        }
        return false;
    }

    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    @Override
    public double getMountHeight() {
        if (CarpetSettings.rideableGhasts) return this.height - 0.2;
        return super.getMountHeight();
    }
}
