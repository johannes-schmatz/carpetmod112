package carpet.mixin.asyncPacketUpdatesFix;

import carpet.CarpetSettings;
import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.ChunkHolder;
import net.minecraft.server.ChunkMap;

import java.util.List;

@Mixin(ChunkMap.class)
public class PlayerWorldManagerMixin {
	@Shadow @Final private List<ChunkHolder> ticking;

	@Inject(
			method = "tick",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/server/ChunkMap;sortLoading:Z",
					ordinal = 0
			)
	)
	private void asyncPacketUpdatesFix(CallbackInfo ci) {
		// Fix for chunks not updating after async updates CARPET-PUNCHSTER
		if (CarpetSettings.asyncPacketUpdatesFix) {
			for (ChunkHolder entry : ticking) {
				((PlayerChunkMapEntryAccessor) entry).setChanges(0);
			}
		}
	}
}
