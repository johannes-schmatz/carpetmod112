package carpet.mixin.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.StructureFeature;
import net.minecraft.world.gen.structure.StructureStart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureFeature.class)
public interface StructureFeatureAccessor {
    @Accessor("structures") Long2ObjectMap<StructureStart> getStructureMap();
    @Invoker("getStructure") StructureStart invokeGetStructureAt(BlockPos pos);
}
