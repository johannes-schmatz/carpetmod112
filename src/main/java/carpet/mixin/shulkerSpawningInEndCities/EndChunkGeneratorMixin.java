package carpet.mixin.shulkerSpawningInEndCities;

import carpet.CarpetSettings;

import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.ShulkerEntity;
import net.minecraft.structure.EndCityStructure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EndChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(EndChunkGenerator.class)
public class EndChunkGeneratorMixin {
    private static final List<Biome.SpawnEntry> spawnList = Collections.singletonList(new Biome.SpawnEntry(ShulkerEntity.class, 10, 4, 4));

    @Shadow @Final private EndCityStructure endCityFeature;
    @Shadow @Final private World world;

    @Inject(
            method = "getSpawnEntries",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shulkerSpawning(EntityCategory creatureType, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnEntry>> cir) {
        if (CarpetSettings.shulkerSpawningInEndCities && creatureType == EntityCategory.MONSTER && endCityFeature.method_9270(pos)) {
            cir.setReturnValue(spawnList);
        }
    }

    @Inject(
            method = "method_4702",
            at = @At("HEAD")
    )
    private void recreateEndCityForShulkerSpawning(Chunk chunkIn, int x, int z, CallbackInfo ci) {
        if (CarpetSettings.shulkerSpawningInEndCities) this.endCityFeature.method_4004(this.world, x, z, null);
    }
}
