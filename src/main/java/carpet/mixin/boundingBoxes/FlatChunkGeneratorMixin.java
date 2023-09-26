package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.structure.StructureFeature;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(FlatChunkGenerator.class)
public class FlatChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private Map<String, StructureFeature> structures;

    @Override
    public NbtList getBoundingBoxes(Entity entity) {
        NbtList boxes = new NbtList();
        for (Map.Entry<String, StructureFeature> e : structures.entrySet()) {
            boxes.add(CarpetClientMarkers.getBoundingBoxes(e.getValue(), entity, 1));
        }
        return boxes;
    }
}
