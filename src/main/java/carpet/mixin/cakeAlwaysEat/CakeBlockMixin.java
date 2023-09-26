package carpet.mixin.cakeAlwaysEat;

import carpet.CarpetSettings;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CakeBlock.class)
public class CakeBlockMixin {
    @Redirect(
            method = "tryEatCake",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/player/PlayerEntity;canEat(Z)Z"
            )
    )
    private boolean canEat(PlayerEntity player, boolean ignoreHunger) {
        return CarpetSettings.cakeAlwaysEat || player.canEat(ignoreHunger);
    }
}
