package carpet.mixin.mobsDontControlMinecarts;

import carpet.CarpetSettings;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.vehicle.MinecartEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecartEntity.class)
public class AbstractMinecartEntityMixin {
    @Redirect(
            method = "moveOnRail",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/living/LivingEntity;forwardSpeed:F"
            )
    )
    private float mobsDontControlMinecarts(LivingEntity entity) {
        if (!CarpetSettings.mobsDontControlMinecarts || entity instanceof PlayerEntity) return entity.f_6499510;
        return 0;
    }
}
