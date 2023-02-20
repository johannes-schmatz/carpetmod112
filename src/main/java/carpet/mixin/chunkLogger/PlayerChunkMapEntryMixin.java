package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.PlayerWorldManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ServerChunkProvider;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkPlayerManager.class)
public class PlayerChunkMapEntryMixin {
    @Shadow @Final private PlayerWorldManager playerWorldManager;
    @Shadow @Final private ChunkPos chunkPos;

    @Inject(
            method = "addPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;isEmpty()Z",
                    remap = false
            )
    )
    private void onAdd(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(this.playerWorldManager.getWorld(), chunkPos.x, chunkPos.z, CarpetClientChunkLogger.Event.PLAYER_ENTERS);
    }

    @Inject(
            method = "method_8127",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerWorldManager;method_12812(Lnet/minecraft/server/ChunkPlayerManager;)V"
            )
    )
    private void onRemove(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(this.playerWorldManager.getWorld(), chunkPos.x, chunkPos.z, CarpetClientChunkLogger.Event.PLAYER_LEAVES);
    }

    @Redirect(
            method = "method_12800",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ServerChunkProvider;getOrGenerateChunks(II)Lnet/minecraft/world/chunk/Chunk;"
            )
    )
    private Chunk provideChunk(ServerChunkProvider provider, int x, int z) {
        try {
            CarpetClientChunkLogger.setReason("Player loading new chunks and generating");
            return provider.getOrGenerateChunks(x, z);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
