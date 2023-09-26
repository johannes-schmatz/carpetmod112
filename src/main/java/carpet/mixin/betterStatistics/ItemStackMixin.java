package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Shadow private int metadata;

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/stat/Stats;itemUsed(Lnet/minecraft/item/Item;)Lnet/minecraft/stat/Stat;"
            )
    )
    private Stat addUseMeta1(Item item) {
        return StatHelper.getObjectUseStats(item, metadata);
    }

    @Redirect(
            method = "attackEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/stat/Stats;itemUsed(Lnet/minecraft/item/Item;)Lnet/minecraft/stat/Stat;"
            )
    )
    private Stat addUseMeta2(Item item) {
        return StatHelper.getObjectUseStats(item, metadata);
    }

    @Redirect(
            method = "mineBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/stat/Stats;itemUsed(Lnet/minecraft/item/Item;)Lnet/minecraft/stat/Stat;"
            )
    )
    private Stat addUseMeta3(Item item) {
        return StatHelper.getObjectUseStats(item, metadata);
    }

    @Redirect(
            method = "onResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/stat/Stats;itemCrafted(Lnet/minecraft/item/Item;)Lnet/minecraft/stat/Stat;"
            )
    )
    private Stat addCraftMeta(Item item) {
        return StatHelper.getCraftStats(item, metadata);
    }
}
