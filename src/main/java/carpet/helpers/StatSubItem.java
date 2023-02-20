package carpet.helpers;

import carpet.mixin.accessors.StatCraftingAccessor;

import net.minecraft.stat.CraftingStat;
import net.minecraft.text.Text;

public class StatSubItem extends CraftingStat {
    private final CraftingStat base;

    public StatSubItem(CraftingStat base, int meta, Text translation) {
        super(base.name, "." + meta, translation, ((StatCraftingAccessor) base).getItem());
        this.base = base;
    }

    public CraftingStat getBase() {
        return this.base;
    }
}
