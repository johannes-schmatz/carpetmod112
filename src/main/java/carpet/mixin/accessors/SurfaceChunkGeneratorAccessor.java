package carpet.mixin.accessors;

import net.minecraft.structure.MansionStructure;
import net.minecraft.world.chunk.SurfaceChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SurfaceChunkGenerator.class)
public interface SurfaceChunkGeneratorAccessor {
    @Accessor("mansions")
    MansionStructure getWoodlandMansionFeature();
}
