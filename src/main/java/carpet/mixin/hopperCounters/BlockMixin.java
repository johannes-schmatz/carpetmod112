package carpet.mixin.hopperCounters;

import net.minecraft.block.Block;
import net.minecraft.block.WoolBlock;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Block.class)
public class BlockMixin {
    @Redirect(
            method = "setup()V",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/block/WoolBlock",
                    ordinal = 0
            )
    )
    private static WoolBlock customWoolBlock(Material material) {
        return new carpet.patches.WoolBlock();
    }
}
