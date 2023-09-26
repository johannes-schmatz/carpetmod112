package carpet.mixin.boundingBoxes;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.gen.chunk.NetherChunkGenerator;
import net.minecraft.world.gen.structure.FortressStructure;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NetherChunkGenerator.class)
public class NetherChunkGeneratorMixin implements BoundingBoxProvider {
    @Shadow @Final private FortressStructure fortress;

    @Override
    public NbtList getBoundingBoxes(Entity entity) {
        NbtList boxes = new NbtList();
        boxes.add(CarpetClientMarkers.getBoundingBoxes(this.fortress, entity, CarpetClientMarkers.FORTRESS));
        return boxes;
    }
}
