package carpet.mixin.skyblock;

import carpet.CarpetSettings;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeInstanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkGenerator;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WorldChunkSection;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Mixin(WorldChunk.class)
public class ChunkMixin {
    @Shadow @Final private TypeInstanceMultiMap<Entity>[] entities;
    @Shadow @Final private Map<BlockPos, BlockEntity> blockEntities;
    @Shadow private int lowestHeight;
    @Shadow @Final private World world;
    @Shadow @Final private int[] heightMap;
    @Shadow @Final private WorldChunkSection[] sections;
    @Shadow @Final public int chunkX;
    @Shadow @Final public int chunkZ;

    @Inject(
            method = "populate(Lnet/minecraft/world/chunk/ChunkGenerator;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkGenerator;populate(II)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterPopulate(ChunkGenerator generator, CallbackInfo ci) {
        // Skyblock in carpet 12
        if(CarpetSettings.skyblock) {
            for(int i = 0; i < 4; i++) {
                if(world.doesChunkExist(chunkX + i % 2, chunkZ + i / 2)) {
                    ((ChunkMixin) (Object) world.getChunkAt(chunkX + i % 2, chunkZ + i / 2)).removeAllBlocks();
                }
            }
        }
    }

    public void removeAllBlocks() {
        BlockState air = Blocks.AIR.defaultState();
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 256; ++l) {
                    int i1 = l >> 4;
                    try {
                        if (this.sections[i1] != null && this.sections[i1].getBlockState(j, l & 15, k).getBlock() != Blocks.END_PORTAL_FRAME) {
                            this.sections[i1].setBlockState(j, l & 15, k, air);
                            if (this.sections[i1].getSkyLightStorage() != null) {
                                this.sections[i1].setSkyLight(j, l & 15, k, 15);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        if (blockEntities != null) {
            blockEntities.clear();
        }
        if (entities != null) {
            Set<Entity> list = new LinkedHashSet<>();
            for (TypeInstanceMultiMap<Entity> entityList : entities) {
                list.addAll(entityList);
            }
            for (Entity e : list) {
                e.remove();
            }
        }
        lowestHeight = 0;
        if (world.dimension.isOverworld()) {
            Arrays.fill(heightMap, 15);
        }
    }
}
