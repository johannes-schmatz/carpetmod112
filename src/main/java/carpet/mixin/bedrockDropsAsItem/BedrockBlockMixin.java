package carpet.mixin.bedrockDropsAsItem;

import carpet.CarpetSettings;
import net.minecraft.block.BedrockBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BedrockBlock.class)
public class BedrockBlockMixin {
    @Inject(
            method = "getDropItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dropAsItem(BlockState state, Random rand, int fortune, CallbackInfoReturnable<Item> cir) {
        if (CarpetSettings.bedrockDropsAsItem) cir.setReturnValue(Item.byBlock((Block) (Object) this));
    }
}
