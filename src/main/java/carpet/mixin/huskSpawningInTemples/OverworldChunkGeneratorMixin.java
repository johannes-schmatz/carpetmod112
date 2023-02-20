package carpet.mixin.huskSpawningInTemples;

import carpet.CarpetSettings;
import carpet.mixin.accessors.StructureFeatureAccessor;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.HuskEntity;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.TemplePieces;
import net.minecraft.structure.TempleStructure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.SurfaceChunkGenerator;
import net.minecraft.world.gen.GeneratorConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(SurfaceChunkGenerator.class)
public class OverworldChunkGeneratorMixin {
    private static final List<Biome.SpawnEntry> HUSK_SPAWN_LIST = Collections.singletonList(new Biome.SpawnEntry(HuskEntity.class, 1, 1, 1));

    @Shadow @Final private TempleStructure witchHut;

    private static boolean isPyramid(TempleStructure temple, BlockPos pos) {
        GeneratorConfig start = ((StructureFeatureAccessor) temple).invokeGetStructureAt(pos);
        if (!(start instanceof TempleStructure.TempleGeneratorConfig) || start.method_11855().isEmpty()) return false;
        StructurePiece piece = start.method_11855().get(0);
        return piece instanceof TemplePieces.DesertPyramid;
    }

    @Inject(
            method = "getSpawnEntries",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/structure/TempleStructure;isSwampHut(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private void huskSpawningInTemples(EntityCategory creatureType, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnEntry>> cir) {
        if (CarpetSettings.huskSpawningInTemples && isPyramid(witchHut, pos)) {
            cir.setReturnValue(HUSK_SPAWN_LIST);
        }
    }
}
