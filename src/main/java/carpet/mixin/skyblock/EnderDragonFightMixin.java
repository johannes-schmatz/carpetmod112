package carpet.mixin.skyblock;

import carpet.CarpetSettings;

import net.minecraft.server.DragonRespawnAnimation;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonRespawnAnimation.class)
public class EnderDragonFightMixin {
    @Shadow private BlockPos portalPos;

    @Inject(
            method = "createExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/feature/EndExitPortalFeature;generate(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private void fixExitPortalLocation(boolean active, CallbackInfo ci) {
        // Fix for the end portal somehow spawning at y = -2 when spawning the first time in skyblock CARPET-XCOM
        if(CarpetSettings.skyblock && portalPos.getY() <= 0) {
            portalPos = new BlockPos(portalPos.getX(), 63, portalPos.getZ());
        }
    }
}
