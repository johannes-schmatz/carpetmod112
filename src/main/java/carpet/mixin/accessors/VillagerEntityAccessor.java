package carpet.mixin.accessors;

import net.minecraft.entity.living.mob.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccessor {
    @Accessor int getRiches();
    @Accessor void setRiches(int riches);
    @Accessor("f_3381958") int getCareer();
    @Accessor("f_3381958") void setCareer(int id);
    @Accessor("f_4282224") int getCareerLevel();
    @Accessor("f_4282224") void setCareerLevel(int level);
}
