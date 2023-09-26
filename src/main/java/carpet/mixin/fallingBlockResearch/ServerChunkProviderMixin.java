package carpet.mixin.fallingBlockResearch;

import carpet.helpers.FallingBlockResearchHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.chunk.ServerChunkCache;

@Mixin(ServerChunkCache.class)
public class ServerChunkProviderMixin {
	@Shadow @Final private ServerWorld world;

	@Inject(
			method = "tick",
			at = @At("HEAD")
	)
	private void end(CallbackInfoReturnable<Boolean> cir) {
		FallingBlockResearchHelper.end(world);
	}
}
