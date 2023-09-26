package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;

import net.minecraft.server.ChunkHolder;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkHolder.class)
public class PlayerChunkMapEntryMixin {
    @Shadow @Final private ChunkMap chunkMap;
    @Shadow @Final private ChunkPos pos;

    @Inject(
            method = "addPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;isEmpty()Z",
                    remap = false
            )
    )
    private void onAdd(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(this.chunkMap.getWorld(), pos.x, pos.z, CarpetClientChunkLogger.Event.PLAYER_ENTERS);
    }

    @Inject(
            method = "removePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ChunkMap;unload(Lnet/minecraft/server/ChunkHolder;)V"
            )
    )
    private void onRemove(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(this.chunkMap.getWorld(), pos.x, pos.z, CarpetClientChunkLogger.Event.PLAYER_LEAVES);
    }

    @Redirect(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/chunk/ServerChunkCache;getChunkNow(II)Lnet/minecraft/world/chunk/WorldChunk;"
            )
    )
    public WorldChunk provideChunk(ServerChunkCache provider, int x, int z) {
        try {
            CarpetClientChunkLogger.setReason("Player loading new chunks and generating");
            return provider.getChunkNow(x, z);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
