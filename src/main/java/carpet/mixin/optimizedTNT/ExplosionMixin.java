package carpet.mixin.optimizedTNT;

import carpet.CarpetSettings;
import carpet.helpers.OptimizedTNT;
import carpet.mixin.accessors.ExplosionAccessor;
import net.minecraft.world.explosion.Explosion;

import carpet.utils.extensions.ExtendedExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class ExplosionMixin {
    @Inject(
            method = "damageEntities",
            at = @At("HEAD"),
            cancellable = true
    )
    private void damageEntities(CallbackInfo ci) {
        if (CarpetSettings.optimizedTNT) {
            OptimizedTNT.damageEntities((ExplosionAccessor) this);
            ci.cancel();
        }
    }

    @Inject(
            method = "damageBlocks",
            at = @At("HEAD"),
            cancellable = true
    )
    private void damageBlocks(boolean spawnParticles, CallbackInfo ci) {
        if (CarpetSettings.optimizedTNT) {
            OptimizedTNT.damageBlocks((ExplosionAccessor) this, (ExtendedExplosion) this, spawnParticles);
            ci.cancel();
        }
    }
}
