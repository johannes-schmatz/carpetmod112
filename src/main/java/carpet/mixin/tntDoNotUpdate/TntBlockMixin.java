package carpet.mixin.tntDoNotUpdate;

import carpet.CarpetSettings;
import net.minecraft.block.TntBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TntBlock.class)
public class TntBlockMixin {
    @Redirect(
            method = "onAdded",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;hasNeighborSignal(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean activateOnPlaced(World world, BlockPos pos) {
        // Carpet setting to remove updates when tnt is placed CARPET-XCOM
        return !CarpetSettings.TNTDoNotUpdate && world.hasNeighborSignal(pos);
    }
}
