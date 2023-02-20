package carpet.mixin.accessors;

import net.minecraft.util.collection.Weighting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Weighting.Weight.class)
public interface WeightingWeightAccessor {
    @Accessor int getWeight();
}
