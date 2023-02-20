package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.item.Item;
import net.minecraft.stat.CraftingStat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Stats.class)
public class StatsMixin {
    @Redirect(
            method = "loadCraftingStats",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/stat/CraftingStat"
            )
    )
    private static CraftingStat createCraftStat(String id1, String id2, Text text, Item item) {
        CraftingStat baseStat = new CraftingStat(id1, id2, text, item);
        StatHelper.addCraftStats(baseStat);
        return baseStat;
    }

    @Redirect(
            method = "loadBlockStats",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/stat/CraftingStat"
            )
    )
    private static CraftingStat createMiningStat(String id1, String id2, Text text, Item item) {
        CraftingStat baseStat = new CraftingStat(id1, id2, text, item);
        StatHelper.addMineStats(baseStat);
        return baseStat;
    }

    @Redirect(
            method = "loadUseStats",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/stat/CraftingStat"
            )
    )
    private static CraftingStat createUseStat(String id1, String id2, Text text, Item item) {
        CraftingStat baseStat = new CraftingStat(id1, id2, text, item);
        StatHelper.addUseStats(baseStat);
        return baseStat;
    }

    @Redirect(
            method = "method_12851",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/stat/CraftingStat",
                    ordinal = 0
            )
    )
    private static CraftingStat createPickedUpStat(String id1, String id2, Text text, Item item) {
        CraftingStat baseStat = new CraftingStat(id1, id2, text, item);
        StatHelper.addPickedUpStats(baseStat);
        return baseStat;
    }

    @Redirect(
            method = "method_12851",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/stat/CraftingStat",
                    ordinal = 1
            )
    )
    private static CraftingStat createDroppedStat(String id1, String id2, Text text, Item item) {
        CraftingStat baseStat = new CraftingStat(id1, id2, text, item);
        StatHelper.addDroppedStats(baseStat);
        return baseStat;
    }
}
