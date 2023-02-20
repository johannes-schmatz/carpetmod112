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
            method = "method_3279",
            constant = {
                    @Constant(intValue = 2),
                    @Constant(intValue = 3)
            }
    )
    private int changeFlags(int flags) {
        return flags | (CarpetSettings.fillUpdates ? 0 : 1024);
    }

    @Redirect(
            method = "method_3279",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;method_8531(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Z)V"
            )
    )
    private void notifyNeighbors(World world, BlockPos pos, Block blockType, boolean updateObservers) {
        if (!CarpetSettings.fillUpdates) return;
        world.method_8531(pos, blockType, updateObservers);
    }

    @Redirect(
            method = "method_3279",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;scheduleTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"
            )
    )
    private void scheduleBlockUpdate(World world, BlockPos pos, Block blockIn, int delay, int priority) {
        if (!CarpetSettings.fillUpdates) return;
        world.scheduleTick(pos, blockIn, delay, priority);
    }
}
