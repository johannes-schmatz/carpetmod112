package carpet.mixin.skyblock;

import carpet.CarpetSettings;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.end.DragonFight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFight.class)
public class EnderDragonFightMixin {
    @Shadow private BlockPos exitPortalPos;

    @Inject(
            method = "placeExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/feature/EndExitPortalFeature;place(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private void fixExitPortalLocation(boolean active, CallbackInfo ci) {
        // Fix for the end portal somehow spawning at y = -2 when spawning the first time in skyblock CARPET-XCOM
        if(CarpetSettings.skyblock && exitPortalPos.getY() <= 0) {
            exitPortalPos = new BlockPos(exitPortalPos.getX(), 63, exitPortalPos.getZ());
        }
    }
}
