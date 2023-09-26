package carpet.mixin.shulkerSpawningInEndCities;

import carpet.CarpetSettings;

import net.minecraft.entity.living.mob.MobCategory;
import net.minecraft.entity.living.mob.hostile.ShulkerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.TheEndChunkGenerator;
import net.minecraft.world.gen.structure.EndCityStructure;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(TheEndChunkGenerator.class)
public class EndChunkGeneratorMixin {
    private static final List<Biome.SpawnEntry> spawnList = Collections.singletonList(new Biome.SpawnEntry(ShulkerEntity.class, 10, 4, 4));

    @Shadow @Final private EndCityStructure endCity;
    @Shadow @Final private World world;

    @Inject(
            method = "getSpawnEntries",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shulkerSpawning(MobCategory creatureType, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnEntry>> cir) {
        if (CarpetSettings.shulkerSpawningInEndCities && creatureType == MobCategory.MONSTER && endCity.isInsideStructure(pos)) {
            cir.setReturnValue(spawnList);
        }
    }

    @Inject(
            method = "placeStructures",
            at = @At("HEAD")
    )
    private void recreateEndCityForShulkerSpawning(WorldChunk chunkIn, int x, int z, CallbackInfo ci) {
        if (CarpetSettings.shulkerSpawningInEndCities) this.endCity.place(this.world, x, z, null);
    }
}
