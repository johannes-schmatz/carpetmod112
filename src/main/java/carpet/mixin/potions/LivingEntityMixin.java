package carpet.mixin.potions;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedStatusEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.effect.StatusEffectInstance;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow @Final private Map<StatusEffect, StatusEffectInstance> statusEffects;
    @Shadow protected int despawnTicks;
    @Shadow protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);
    @Shadow protected abstract void onStatusEffectApplied(StatusEffectInstance id);
    @Shadow protected abstract void onStatusEffectUpgraded(StatusEffectInstance id, boolean p_70695_2_);
    @Shadow public abstract void addStatusEffect(StatusEffectInstance potioneffectIn);

    @Inject(
            method = "addStatusEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/effect/StatusEffectInstance;combine(Lnet/minecraft/entity/living/effect/StatusEffectInstance;)V"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void combinePotions(StatusEffectInstance added, CallbackInfo ci, StatusEffectInstance current) {
        StatusEffectInstance newEffect = ((ExtendedStatusEffectInstance) current).combine(added);
        if (newEffect == current) {
            // vanilla
            this.onStatusEffectUpgraded(newEffect, true);
        } else {
            // carpet
            this.statusEffects.put(newEffect.getEffect(), newEffect);
            this.onStatusEffectRemoved(current);
            this.onStatusEffectApplied(newEffect);
        }
        ci.cancel();
    }

    @Inject(
            method = "onStatusEffectRemoved",
            at = @At("RETURN")
    )
    private void onPotionFinish(StatusEffectInstance effect, CallbackInfo ci) {
        if (!CarpetSettings.effectsFix) return;
        StatusEffectInstance previous = ((ExtendedStatusEffectInstance) effect).getPrevious();
        if (previous != null) addStatusEffect(previous);
    }

    @Redirect(
            method = "damage",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/living/LivingEntity;despawnTicks:I"
            )
    )
    private void potionDespawnFix1(LivingEntity entity, int despawnCounter) {
        if (!CarpetSettings.potionsDespawnFix) {
            this.despawnTicks = despawnCounter;
        }
    }

    // CM reset entity age is connected to making a hurt noise
    @Inject(
            method = "damage",
            at = @At("TAIL")
    )
    private void potionDespawnFix2(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.potionsDespawnFix) {
            this.despawnTicks = 0;
        }
    }
}
