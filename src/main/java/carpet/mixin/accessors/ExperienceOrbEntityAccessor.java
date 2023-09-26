package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.XpOrbEntity;

@Mixin(XpOrbEntity.class)
public interface ExperienceOrbEntityAccessor {
    @Accessor("xp") void setAmount(int value);
}
