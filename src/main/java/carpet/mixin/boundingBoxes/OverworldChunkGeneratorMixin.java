package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtList;
import net.minecraft.structure.*;
import net.minecraft.world.chunk.SurfaceChunkGenerator;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SurfaceChunkGenerator.class)
public class OverworldChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private TempleStructure witchHut;
    @Shadow @Final private VillageStructure village;
    @Shadow @Final private StrongholdStructure strongholdGenerator;
    @Shadow @Final private MineshaftStructure mineshaft;
    @Shadow @Final private OceanMonumentStructure oceanMonument;
    @Shadow @Final private MansionStructure mansions;

    public NbtList getBoundingBoxes(Entity entity) {
        NbtList boxes = new NbtList();
        boxes.add(CarpetClientMarkers.getBoundingBoxes(witchHut, entity, CarpetClientMarkers.TEMPLE));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(village, entity, CarpetClientMarkers.VILLAGE));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(strongholdGenerator, entity, CarpetClientMarkers.STRONGHOLD));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(mineshaft, entity, CarpetClientMarkers.MINESHAFT));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(oceanMonument, entity, CarpetClientMarkers.MONUMENT));
        boxes.add(CarpetClientMarkers.getBoundingBoxes(mansions, entity, CarpetClientMarkers.MANSION));
        return boxes;
    }
}
