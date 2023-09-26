package carpet.mixin.boundingBoxes;

import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(DebugChunkGenerator.class)
public class DebugChunkGeneratorMixin implements BoundingBoxProvider {
    @Override
    public NbtList getBoundingBoxes(Entity entity) {
        return new NbtList();
    }
}
