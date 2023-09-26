package carpet.mixin.loggers;

import carpet.logging.logHelpers.DamageReporter;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.mob.SlimeEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SlimeEntity.class)
public class SlimeEntityMixin {
    @Redirect(
            method = "damageTargetEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean logDamage(LivingEntity entityLivingBase, DamageSource source, float amount) {
        DamageReporter.register_damage_attacker(entityLivingBase, (SlimeEntity) (Object) this, amount);
        return entityLivingBase.damage(source, amount);
    }
}
