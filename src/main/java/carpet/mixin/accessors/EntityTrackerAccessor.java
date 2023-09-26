package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.server.entity.EntityTracker;
import net.minecraft.server.entity.EntityTrackerEntry;

@Mixin(EntityTracker.class)
public interface EntityTrackerAccessor {
    @Accessor("entries") Set<EntityTrackerEntry> getEntries();
}
