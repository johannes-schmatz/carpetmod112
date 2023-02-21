package carpet.mixin.asyncPacketUpdatesFix;

import carpet.CarpetSettings;
import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.server.PlayerWorldManager;

import java.util.List;

@Mixin(PlayerWorldManager.class)
public class PlayerWorldManagerMixin {
	@Shadow @Final private List<ChunkPlayerManager> playerInstances;

	@Inject(
			method = "method_2111",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/server/PlayerWorldManager;field_13872:Z",
					ordinal = 0
			)
	)
	private void asyncPacketUpdatesFix(CallbackInfo ci) {
		// Fix for chunks not updating after async updates CARPET-PUNCHSTER
		if (CarpetSettings.asyncPacketUpdatesFix) {
			for (ChunkPlayerManager entry : playerInstances) {
				((PlayerChunkMapEntryAccessor) entry).setChanges(0);
			}
		}
	}
}
