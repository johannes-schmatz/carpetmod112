package carpet.mixin.accessors;

import net.minecraft.item.Item;
import net.minecraft.stat.CraftingStat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingStat.class)
public interface StatCraftingAccessor {
    @Accessor("field_9030") Item getItem();
}
