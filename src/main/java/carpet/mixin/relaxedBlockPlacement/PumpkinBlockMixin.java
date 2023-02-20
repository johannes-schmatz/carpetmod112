package carpet.mixin.relaxedBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.PumpkinBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PumpkinBlock.class)
public class PumpkinBlockMixin {
    @Redirect(
            method = "canBePlacedAtPos",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;method_11739()Z"
            )
    )
    private boolean allowMidAir(BlockState state) {
        return CarpetSettings.relaxedBlockPlacement || state.method_11739();
    }
}
