package carpet.mixin.accessors;

import carpet.utils.extensions.AccessibleGoalSelectorEntry;
import net.minecraft.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// TODO: make this an access widener
@Mixin(targets = "net.minecraft.entity.ai.goal.GoalSelector$Entry")
public class GoalSelectorEntryAccessor implements AccessibleGoalSelectorEntry {
    @Shadow @Final public Goal goal;
    @Shadow @Final public int priority;
    @Shadow public boolean f_3327432;

    @Override
    public Goal getAction() {
        return goal;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isUsing() {
        return f_3327432;
    }
}
