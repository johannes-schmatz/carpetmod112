package carpet.mixin.skyblock;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ChunkGenerator;
import net.minecraft.util.TypeFilterableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
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

@Mixin(Chunk.class)
public class ChunkMixin {
    @Shadow @Final private TypeFilterableList<Entity>[] entities;
    @Shadow @Final private Map<BlockPos, BlockEntity> blockEntities;
    @Shadow private int minimumHeightmap;
    @Shadow @Final private World world;
    @Shadow @Final private int[] heightmap;
    @Shadow @Final private ChunkSection[] chunkSections;
    @Shadow @Final public int chunkX;
    @Shadow @Final public int chunkZ;

    @Inject(
            method = "populate(Lnet/minecraft/server/world/ChunkGenerator;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ChunkGenerator;populate(II)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterPopulate(ChunkGenerator generator, CallbackInfo ci) {
        // Skyblock in carpet 12
        if(CarpetSettings.skyblock) {
            for(int i = 0; i < 4; i++) {
                if(world.method_13690(chunkX + i % 2, chunkZ + i / 2)) {
                    ((ChunkMixin) (Object) world.getChunk(chunkX + i % 2, chunkZ + i / 2)).removeAllBlocks();
                }
            }
        }
    }

    public void removeAllBlocks() {
        BlockState air = Blocks.AIR.getDefaultState();
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 256; ++l) {
                    int i1 = l >> 4;
                    try {
                        if (this.chunkSections[i1] != null && this.chunkSections[i1].getBlockState(j, l & 15, k).getBlock() != Blocks.END_PORTAL_FRAME) {
                            this.chunkSections[i1].setBlockState(j, l & 15, k, air);
                            if (this.chunkSections[i1].getSkyLight() != null) {
                                this.chunkSections[i1].setSkyLight(j, l & 15, k, 15);
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
            for (TypeFilterableList<Entity> entityList : entities) {
                list.addAll(entityList);
            }
            for (Entity e : list) {
                e.remove();
            }
        }
        minimumHeightmap = 0;
        if (world.dimension.isOverworld()) {
            Arrays.fill(heightmap, 15);
        }
    }
}
