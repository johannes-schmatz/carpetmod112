package carpet.mixin.loggers;

import carpet.logging.logHelpers.DamageReporter;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.attribute.EntityAttributeInstance;
import net.minecraft.entity.living.attribute.EntityAttribute;
import net.minecraft.entity.living.attribute.EntityAttributes;
import net.minecraft.unmapped.C_6686615;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow protected float damageAmount;

    @Shadow protected abstract float damageAfterArmorResistance(DamageSource source, float damage);
    @Shadow public abstract int getArmorProtection();
    @Shadow public abstract EntityAttributeInstance getAttribute(EntityAttribute attribute);

    @Inject(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/LivingEntity;getHealth()F"
            )
    )
    private void registerDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.register_damage((LivingEntity) (Object) this, source, amount);
    }

    @Inject(
            method = "damage",
            at = @At(
                    value = "RETURN",
                    ordinal = 2
            )
    )
    private void modifyDamageDead(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, 0, "Already dead and can't take more damage");
    }

    @Inject(
            method = "damage",
            at = @At(
                    value = "RETURN",
                    ordinal = 3
            )
    )
    private void modifyDamageFire(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, 0, "Resistance to fire");
    }

    @Inject(
            method = "damage",
            at = @At(
                    value = "CONSTANT",
                    args = "floatValue=0.75",
                    shift = At.Shift.AFTER
            )
    )
    private void modifyDamageHelmet(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, amount * 0.75f, "wearing a helmet");
    }

    @Inject(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/LivingEntity;m_9120351(F)V",
                    shift = At.Shift.AFTER
            )
    )
    private void modifyDamageShield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, 0, "using a shield");
    }

    @Inject(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V",
                    ordinal = 0
            )
    )
    private void modifyDamageRecentlyHitReduced(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, amount - this.damageAmount, "Recently hit");
    }

    @Inject(
            method = "damage",
            at = @At("RETURN"),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/entity/living/LivingEntity;defaultMaxHealth:I",
                            ordinal = 0
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/living/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V",
                            ordinal = 0
                    )
            )
    )
    private void modifyDamageRecentlyHitNone(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, amount, 0, "Recently hit");
    }

    @Inject(
            method = "damageAfterEffectsAndEnchantments",
            at = @At(
                    value = "CONSTANT",
                    args = "floatValue=25"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void modifyDamageResistanceEffect(DamageSource source, float damage, CallbackInfoReturnable<Float> cir, int i, int j, float f) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, damage, f / 25.0F, "Resistance status effect");
    }

    @Redirect(
            method = "damageAfterEffectsAndEnchantments",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/unmapped/C_6686615;m_4368311(FF)F"
            )
    )
    private float getDamageAfterMagicAbsorb(float damage, float enchantModifiers, DamageSource source) {
        float after = C_6686615.m_4368311(damage, enchantModifiers);
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, damage, after, String.format("enchantments (%.1f total points)", enchantModifiers));
        return after;
    }

    @Redirect(
            method = "applyDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/LivingEntity;damageAfterArmorResistance(Lnet/minecraft/entity/damage/DamageSource;F)F"
            )
    )
    private float applyArmorCalculationsAndLog(LivingEntity entity, DamageSource source, float damage) {
        float after = damageAfterArmorResistance(source, damage);
        DamageReporter.modify_damage((LivingEntity) (Object) this, source, damage, after, String.format("Armour %.1f, Toughness %.1f", (float) this.getArmorProtection(), this.getAttribute(EntityAttributes.f_8039308).get()));
        return after;
    }

    @Inject(
            method = "applyDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/LivingEntity;setAbsorption(F)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void modifyDamageAbsorption(DamageSource damageSrc, float damageAmount, CallbackInfo ci, float f) {
        DamageReporter.modify_damage((LivingEntity) (Object) this, damageSrc, damageAmount, f, "Absorption");
    }

    @Inject(
            method = "applyDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/LivingEntity;setHealth(F)V"
            )
    )
    private void registerFinalDamage(DamageSource damageSrc, float damageAmount, CallbackInfo ci) {
        DamageReporter.register_final_damage((LivingEntity) (Object) this, damageSrc, damageAmount);
    }
}
