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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow @Final private Map<StatusEffect, StatusEffectInstance> statusEffects;
    @Shadow protected int despawnCounter;
    @Shadow protected abstract void method_2649(StatusEffectInstance effect);
    @Shadow protected abstract void method_2582(StatusEffectInstance id);
    @Shadow protected abstract void method_6108(StatusEffectInstance id, boolean p_70695_2_);
    @Shadow public abstract void addStatusEffect(StatusEffectInstance potioneffectIn);

    @Inject(
            method = "addStatusEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/effect/StatusEffectInstance;setFrom(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void combinePotions(StatusEffectInstance added, CallbackInfo ci, StatusEffectInstance current) {
        StatusEffectInstance newEffect = ((ExtendedStatusEffectInstance) current).combine(added);
        if (newEffect != current) {
            // carpet
            this.statusEffects.put(newEffect.getStatusEffect(), newEffect);
            this.method_2649(current);
            this.method_2582(newEffect);
        } else {
            // vanilla
            this.method_6108(newEffect, true);
        }
        ci.cancel();
    }

    @Inject(
            method = "method_2649",
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
                    target = "Lnet/minecraft/entity/LivingEntity;despawnCounter:I"
            )
    )
    private void potionDespawnFix1(LivingEntity entity, int despawnCounter) {
        if (!CarpetSettings.potionsDespawnFix) {
            this.despawnCounter = despawnCounter;
        }
    }

    // CM reset entity age is connected to making a hurt noise
    @Inject(
            method = "damage",
            at = @At("TAIL")
    )
    private void potionDespawnFix2(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.potionsDespawnFix) {
            this.despawnCounter = 0;
        }
    }
}
