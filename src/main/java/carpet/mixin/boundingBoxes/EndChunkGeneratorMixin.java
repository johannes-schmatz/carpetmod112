package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.gen.chunk.TheEndChunkGenerator;
import net.minecraft.world.gen.structure.EndCityStructure;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TheEndChunkGenerator.class)
public class EndChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private EndCityStructure endCity;

    @Override
    public NbtList getBoundingBoxes(Entity entity) {
        NbtList boxes = new NbtList();
        boxes.add(CarpetClientMarkers.getBoundingBoxes(endCity, entity, CarpetClientMarkers.END_CITY));
        return boxes;
    }
}
