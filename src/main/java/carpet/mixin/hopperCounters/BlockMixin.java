package carpet.mixin.hopperCounters;

import net.minecraft.block.Block;
import net.minecraft.block.ColoredBlock;
import net.minecraft.block.material.Material;

import carpet.patches.WoolBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Block.class)
public class BlockMixin {
    @Redirect(
            method = "init",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/block/ColoredBlock",
                    ordinal = 0
            )
    )
    private static ColoredBlock customWoolBlock(Material material) {
        return new WoolBlock();
    }
}
