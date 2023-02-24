package carpet.mixin.redstoneMultimeter;

import carpet.CarpetServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Inject(
			method = "method_12827",
			at = @At("TAIL")
	)
	private void onPlayerJoin(ClientConnection arg, ServerPlayerEntity arg2, CallbackInfo ci) {
		CarpetServer.getInstance().rsmmServer.onPlayerJoin(arg2);
	}

	@Inject(
			method = "method_12830",
			at = @At("HEAD")
	)
	private void onPlayerLeave(ServerPlayerEntity arg, CallbackInfo ci) {
		CarpetServer.getInstance().rsmmServer.onPlayerLeave(arg);
	}
}
