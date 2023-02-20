package carpet.mixin.accessors;

import net.minecraft.class_2763;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_2763.class)
public interface StructureManagerAccessor {
    @Accessor("field_13023") String getBaseFolder();
}
