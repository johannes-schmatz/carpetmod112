package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.living.mob.SlimeEntity;

@Mixin(SlimeEntity.class)
public interface SlimeEntityAccessor {
    @Invoker int invokeGetDamageAmount();
}
