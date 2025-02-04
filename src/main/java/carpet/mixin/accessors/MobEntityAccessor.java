package carpet.mixin.accessors;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.living.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobEntity.class)
public interface MobEntityAccessor {
    // TODO: unsure, could also be targetSelector
    @Accessor("goalSelector") GoalSelector getGoalSelector();
    @Invoker("canDespawn") boolean invokeCanImmediatelyDespawn();
}
