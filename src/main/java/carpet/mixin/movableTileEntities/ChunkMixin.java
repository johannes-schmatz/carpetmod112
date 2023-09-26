package carpet.mixin.movableTileEntities;

import carpet.CarpetSettings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldChunk.class)
public class ChunkMixin {
    @Shadow @Final private World world;

    @Redirect(
            method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;)Lnet/minecraft/block/state/BlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;getBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/WorldChunk$BlockEntityCreationType;)Lnet/minecraft/block/entity/BlockEntity;"
            )
    )
    private BlockEntity betterGetTileEntity(WorldChunk chunk, BlockPos pos, WorldChunk.BlockEntityCreationType creationMode) {
        //this.getTileEntity(...) doesn't check pending TileEntities
        if (CarpetSettings.movableTileEntities || CarpetSettings.autocrafter) {
            return world.getBlockEntity(pos);
        } else {
            return chunk.getBlockEntity(pos, creationMode);
        }
    }
}
