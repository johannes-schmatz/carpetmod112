package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkGenerator;
import net.minecraft.world.chunk.WorldChunk;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class ChunkMixin {
    @Shadow @Final private World world;
    @Shadow @Final public int chunkX;
    @Shadow @Final public int chunkZ;

    @Inject(
            method = "populate(Lnet/minecraft/world/chunk/ChunkGenerator;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;markDirty()V",
                    ordinal = 0
            )
    )
    private void onPopulateStructures(ChunkGenerator generator, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(world, chunkX, chunkZ, CarpetClientChunkLogger.Event.GENERATING_STRUCTURES);
    }

    @Inject(
            method = "populate(Lnet/minecraft/world/chunk/ChunkGenerator;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkGenerator;populateChunk(II)V"
            )
    )
    private void onPopulate(ChunkGenerator generator, CallbackInfo ci) {
        CarpetClientChunkLogger.logger.log(world, chunkX, chunkZ, CarpetClientChunkLogger.Event.POPULATING);
    }
}
