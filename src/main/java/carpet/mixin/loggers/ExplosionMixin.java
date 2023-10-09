package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.utils.extensions.ExtendedExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

@Mixin(Explosion.class)
public class ExplosionMixin implements ExtendedExplosion {
	public ExplosionLogHelper logHelper = null;
	@Shadow @Final private World world;

	@Inject(
			method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;DDDFZZ)V",
			at = @At("TAIL")
	)
	public void setLogHelper(World world, Entity entity, double x, double y, double z, float power, boolean createFire, boolean destructive, CallbackInfo ci) {
		if (LoggerRegistry.__explosions) {
			logHelper = new ExplosionLogHelper(entity, x, y, z, power, createFire);
		}
	}

	@Inject(
			method = "damageBlocks",
			at = @At("TAIL")
	)
	public void logExplosionDone(boolean showSmallParticles, CallbackInfo ci) {
		if (LoggerRegistry.__explosions) {
			this.logHelper.onExplosionDone(this.world.getTime());
		}
	}

	public ExplosionLogHelper getLogHelper() {
		return logHelper;
	}
}
