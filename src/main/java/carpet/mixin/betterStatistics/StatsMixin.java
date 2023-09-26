package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.item.Item;
import net.minecraft.stat.ItemStat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Stats.class)
public class StatsMixin {
    @Redirect(
            method = "initItemsCraftedStats",
            at = @At(
                    value = "NEW",
                    target = "(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/text/Text;Lnet/minecraft/item/Item;)Lnet/minecraft/stat/ItemStat;"
            )
    )
    private static ItemStat createCraftStat(String id1, String id2, Text text, Item item) {
        ItemStat baseStat = new ItemStat(id1, id2, text, item);
        StatHelper.addCraftStats(baseStat);
        return baseStat;
    }

    @Redirect(
            method = "initBlocksMinedStats",
            at = @At(
                    value = "NEW",
                    target = "(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/text/Text;Lnet/minecraft/item/Item;)Lnet/minecraft/stat/ItemStat;"
            )
    )
    private static ItemStat createMiningStat(String id1, String id2, Text text, Item item) {
        ItemStat baseStat = new ItemStat(id1, id2, text, item);
        StatHelper.addMineStats(baseStat);
        return baseStat;
    }

    @Redirect(
            method = "initItemsUsedStats",
            at = @At(
                    value = "NEW",
                    target = "(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/text/Text;Lnet/minecraft/item/Item;)Lnet/minecraft/stat/ItemStat;"
            )
    )
    private static ItemStat createUseStat(String id1, String id2, Text text, Item item) {
        ItemStat baseStat = new ItemStat(id1, id2, text, item);
        StatHelper.addUseStats(baseStat);
        return baseStat;
    }

    @Redirect(
            method = "initItemStats",
            at = @At(
                    value = "NEW",
                    target = "(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/text/Text;Lnet/minecraft/item/Item;)Lnet/minecraft/stat/ItemStat;",
                    ordinal = 0
            )
    )
    private static ItemStat createPickedUpStat(String id1, String id2, Text text, Item item) {
        ItemStat baseStat = new ItemStat(id1, id2, text, item);
        StatHelper.addPickedUpStats(baseStat);
        return baseStat;
    }

    @Redirect(
            method = "initItemStats",
            at = @At(
                    value = "NEW",
                    target = "(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/text/Text;Lnet/minecraft/item/Item;)Lnet/minecraft/stat/ItemStat;",
                    ordinal = 1
            )
    )
    private static ItemStat createDroppedStat(String id1, String id2, Text text, Item item) {
        ItemStat baseStat = new ItemStat(id1, id2, text, item);
        StatHelper.addDroppedStats(baseStat);
        return baseStat;
    }
}
