package carpet.mixin.accessors;

import net.minecraft.world.gen.structure.template.StructureManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureManager.class)
public interface StructureManagerAccessor {
    @Accessor("dir") String getBaseFolder();
}
