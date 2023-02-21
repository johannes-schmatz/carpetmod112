package carpet.mixin.asyncPacketUpdatesFix;

import carpet.CarpetSettings;
import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ServerChunkProvider;

@Mixin(ServerChunkProvider.class)
public class ServerChunkProviderMixin {
	@Shadow @Final private ServerWorld world;

	/**
	 *TODO: this can collide with {@link carpet.mixin.chunkLogger.PlayerChunkMapEntryMixin.provideChunk}, it should be
	 * behind that one (so first the access gets logged, then this happens)
	 */
	@Inject(
			method = "method_12777",
			at = @At(
					value = "INVOKE",
					target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;put(JLjava/lang/Object;)Ljava/lang/Object;",
					remap = false
			),
			locals = LocalCapture.PRINT
	)
	private void asyncPacketUpdatesFix(int x, int z, CallbackInfoReturnable<Chunk> cir, Chunk lv) {
		// Fix for chunks not updating after async updates CARPET-PUNCHSTER
		if(CarpetSettings.asyncPacketUpdatesFix) {
			ChunkPlayerManager entry = world.getPlayerWorldManager().method_12811(x, z);
			if (entry != null && entry.getChunk() != null) {
				((PlayerChunkMapEntryAccessor) entry).setChunk(lv);
				entry.method_12801();
			}
		}
	}
}
