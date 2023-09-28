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

import net.minecraft.server.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(ServerChunkCache.class)
public class ServerChunkProviderMixin {
	@Shadow @Final private ServerWorld world;

	/**
	 *TODO: this can collide with {@link carpet.mixin.chunkLogger.PlayerChunkMapEntryMixin.provideChunk}, it should be
	 * behind that one (so first the access gets logged, then this happens)
	 */
	@Inject(
			method = "loadChunk",
			at = @At(
					value = "INVOKE",
					target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;put(JLjava/lang/Object;)Ljava/lang/Object;",
					remap = false
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void asyncPacketUpdatesFix(int x, int z, CallbackInfoReturnable<WorldChunk> cir, WorldChunk lv) {
		// Fix for chunks not updating after async updates CARPET-PUNCHSTER
		if(CarpetSettings.asyncPacketUpdatesFix) {
			ChunkHolder entry = world.getChunkMap().getChunk(x, z);
			if (entry != null && entry.getChunk() != null) {
				((PlayerChunkMapEntryAccessor) entry).setChunk(lv);
				entry.populate();
			}
		}
	}
}
