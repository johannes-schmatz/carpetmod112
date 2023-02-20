package carpet.mixin.lazyChunkBehavior;

import carpet.CarpetSettings;
import carpet.helpers.LazyChunkBehaviorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@Mixin(World.class)
public class WorldMixin {
	@Redirect(
			method = "checkChunk(Lnet/minecraft/entity/Entity;Z)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;tickRiding()V"
			)
	)
	public void tickRidingRedirect(Entity entity) {
		if (!CarpetSettings.commandLazyChunkBehavior || LazyChunkBehaviorHelper.shouldUpdate(entity)) {
			entity.tickRiding();
		}
	}

	@Redirect(
			method = "checkChunk(Lnet/minecraft/entity/Entity;Z)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;tick()V"
			)
	)
	public void tickRedirect(Entity entity) {
		if (!CarpetSettings.commandLazyChunkBehavior || LazyChunkBehaviorHelper.shouldUpdate(entity)) {
			entity.tick();
		}
	}
}
