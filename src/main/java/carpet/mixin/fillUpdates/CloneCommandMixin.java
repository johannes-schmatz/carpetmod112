package carpet.mixin.fillUpdates;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.server.command.CloneCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CloneCommand.class)
public class CloneCommandMixin {
    @ModifyConstant(
            method = "run",
            constant = {
                    @Constant(intValue = 2),
                    @Constant(
                            intValue = 3,
                            ordinal = 1
                    )
            }
    )
    private int changeFlags(int flags) {
        // TODO: let this get a public static final field!
        return flags | (CarpetSettings.fillUpdates ? 0 : 1024);
    }

    @Redirect(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;onBlockChanged(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Z)V"
            )
    )
    private void notifyNeighbors(World world, BlockPos pos, Block blockType, boolean updateObservers) {
        if (!CarpetSettings.fillUpdates) return;
        world.onBlockChanged(pos, blockType, updateObservers);
    }

    @Redirect(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;loadScheduledTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"
            )
    )
    private void scheduleBlockUpdate(World world, BlockPos pos, Block blockIn, int delay, int priority) {
        if (!CarpetSettings.fillUpdates) return;
        world.loadScheduledTick(pos, blockIn, delay, priority);
    }
}
