package carpet.mixin.limitITTupdates;

import carpet.CarpetSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
	@Shadow @Final private MinecraftServer server;
	private int limitITTCounter;
	@Inject(
			method = "tick",
			at = @At("HEAD")
	)
	private void onTickStart(CallbackInfo ci) {
		if (CarpetSettings.limitITTupdates > 0 && server.isOnThread()) {
			limitITTCounter = 0;
		}
	}

	@Inject(
			method = "createAndScheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;",
					ordinal = 1
			),
			cancellable = true
	)
	private void onITT(BlockPos pos, Block block, int tickRate, int priority, CallbackInfo ci) {
		if (CarpetSettings.limitITTupdates > 0 && server.isOnThread()) {
			limitITTCounter++;
			if (limitITTCounter > CarpetSettings.limitITTupdates) {
				ci.cancel();
			}
		}
	}
}
