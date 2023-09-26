package carpet.mixin.accessors;

import net.minecraft.scoreboard.criterion.StatCriterion;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StatCriterion.class)
public interface ScoreCriteriaStatAccessor {
    @Accessor("stat") Stat getStat();
}
