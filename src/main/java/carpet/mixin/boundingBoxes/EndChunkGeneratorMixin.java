package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtList;
import net.minecraft.structure.EndCityStructure;
import net.minecraft.world.chunk.EndChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EndChunkGenerator.class)
public class EndChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private EndCityStructure endCityFeature;

    @Override
    public NbtList getBoundingBoxes(Entity entity) {
        NbtList nbttaglist = new NbtList();
        nbttaglist.add(CarpetClientMarkers.getBoundingBoxes(endCityFeature, entity, CarpetClientMarkers.END_CITY));
        return nbttaglist;
    }
}
