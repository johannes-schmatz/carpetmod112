package carpet.mixin.removeTNTVeclocity;

import carpet.CarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.explosion.Explosion;

@Mixin(Explosion.class)
public class ExplosionMixin {
	@Inject(
			method = "damageEntities",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z",
					remap = false,
					shift = At.Shift.AFTER
			),
			cancellable = true
	)
	private void addAll(CallbackInfo ci) {
		// CARPET-SYLKOS
		// TNT shouldn't apply velocity to entities
		// This also yeets all the calculations tnt does for applying velocity and damage to entities
		if (CarpetSettings.removeTNTVelocity) ci.cancel();
	}
}