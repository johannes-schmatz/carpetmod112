package carpet.mixin.xpNoCooldown;

import carpet.CarpetSettings;
import net.minecraft.entity.XpOrbEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(XpOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @Redirect(
            method = "onPlayerCollision",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/living/player/PlayerEntity;xpCooldown:I",
                    ordinal = 0
            )
    )
    private int getCooldown(PlayerEntity player) {
        return CarpetSettings.xpNoCooldown ? 0 : player.xpCooldown;
    }
}
