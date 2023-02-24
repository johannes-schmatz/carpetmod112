package carpet.mixin.redstoneMultimeter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import redstone.multimeter.common.TickTask;
import redstone.multimeter.helper.WorldHelper;

import net.minecraft.server.DragonRespawnAnimation;

@Mixin(DragonRespawnAnimation.class)
public class EnderDragonFightMixin {
	@Inject(
			method = "method_11805",
			at = @At("HEAD")
	)
	private void startTickTask(CallbackInfo ci) {
		WorldHelper.startTickTask(TickTask.DRAGON_FIGHT);
	}

	@Inject(
			method = "method_11805",
			at = @At("TAIL")
	)
	private void endTickTask(CallbackInfo ci) {
		WorldHelper.endTickTask();
	}
}
