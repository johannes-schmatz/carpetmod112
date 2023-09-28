package carpet.mixin.accessors;

import net.minecraft.entity.living.mob.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccessor {
    @Accessor int getRiches();
    @Accessor void setRiches(int riches);
    @Accessor("career") int getCareer();
    @Accessor("career") void setCareer(int id);
    @Accessor("careerLevel") int getCareerLevel();
    @Accessor("careerLevel") void setCareerLevel(int level);
}
