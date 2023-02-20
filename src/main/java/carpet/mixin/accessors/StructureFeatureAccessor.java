package carpet.mixin.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import net.minecraft.structure.StructureFeature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.GeneratorConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureFeature.class)
public interface StructureFeatureAccessor {
    @Accessor("field_13012") Long2ObjectMap<GeneratorConfig> getStructureMap();
    @Invoker("getGeneratorConfigAtPos") GeneratorConfig invokeGetStructureAt(BlockPos pos);
}
