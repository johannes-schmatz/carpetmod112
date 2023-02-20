package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtList;
import net.minecraft.structure.StructureFeature;
import net.minecraft.world.chunk.FlatChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(FlatChunkGenerator.class)
public class FlatChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private Map<String, StructureFeature> field_15187;

    @Override
    public NbtList getBoundingBoxes(Entity entity) {
        NbtList boxes = new NbtList();
        for (Map.Entry<String, StructureFeature> e : field_15187.entrySet()) {
            boxes.add(CarpetClientMarkers.getBoundingBoxes(e.getValue(), entity, 1));
        }
        return boxes;
    }
}
