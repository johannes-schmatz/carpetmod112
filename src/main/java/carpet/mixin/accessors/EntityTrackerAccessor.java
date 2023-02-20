package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.TrackedEntityInstance;

@Mixin(EntityTracker.class)
public interface EntityTrackerAccessor {
    @Accessor("trackedEntities") Set<TrackedEntityInstance> getEntries();
}
