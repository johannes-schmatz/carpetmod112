package carpet.mixin.loggers;

import carpet.logging.logHelpers.DamageReporter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.mob.hostile.HostileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {
    private static final ThreadLocal<Float> attackDamagePre = new ThreadLocal<>();

    @Inject(
            method = "attack",
            at = @At(
                    value = "CONSTANT",
                    args = "intValue=0"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onAttack(Entity entity, CallbackInfoReturnable<Boolean> cir, float attackDamage) {
        DamageReporter.register_damage_attacker(entity, (HostileEntity) (Object) this, attackDamage);
        attackDamagePre.set(attackDamage);
    }

    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;getKnockbackLevel(Lnet/minecraft/entity/living/LivingEntity;)I"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onAttackModified(Entity entity, CallbackInfoReturnable<Boolean> cir, float attackDamage) {
        DamageReporter.modify_damage((LivingEntity)entity, DamageSource.mob((HostileEntity) (Object) this), attackDamagePre.get(), attackDamage, "attacker enchants");
    }
}
