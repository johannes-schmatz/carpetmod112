package carpet.mixin.huskSpawningInTemples;

import carpet.CarpetSettings;
import carpet.mixin.accessors.StructureFeatureAccessor;

import net.minecraft.entity.living.mob.MobCategory;
import net.minecraft.entity.living.mob.hostile.HuskEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.OverworldChunkGenerator;
import net.minecraft.world.gen.structure.StructurePiece;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.TemplePieces;
import net.minecraft.world.gen.structure.TempleStructure;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(OverworldChunkGenerator.class)
public class OverworldChunkGeneratorMixin {
    private static final List<Biome.SpawnEntry> HUSK_SPAWN_LIST = Collections.singletonList(new Biome.SpawnEntry(HuskEntity.class, 1, 1, 1));

    @Shadow @Final private TempleStructure witchHut;

    private static boolean isPyramid(TempleStructure temple, BlockPos pos) {
        StructureStart start = ((StructureFeatureAccessor) temple).invokeGetStructureAt(pos);
        if (!(start instanceof TempleStructure.Start) || start.getPieces().isEmpty()) return false;
        StructurePiece piece = start.getPieces().get(0);
        return piece instanceof TemplePieces.DesertPyramid;
    }

    @Inject(
            method = "getSpawnEntries",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/structure/TempleStructure;isWitchHut(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private void huskSpawningInTemples(MobCategory creatureType, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnEntry>> cir) {
        if (CarpetSettings.huskSpawningInTemples && isPyramid(witchHut, pos)) {
            cir.setReturnValue(HUSK_SPAWN_LIST);
        }
    }
}
