package carpet.mixin.repeaterPoweredTerracotta;

import net.minecraft.block.DiodeBlock;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DiodeBlock.class)
public abstract class AbstractRedstoneGateBlockMixin {
    protected int getTickDelay(BlockState state, World world, BlockPos pos) {
        return this.getDelay(state, world, pos);
    }

    protected abstract int getDelay(BlockState state, World world, BlockPos pos);

    @Redirect(
            method = "checkOutputState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/DiodeBlock;getDelay(Lnet/minecraft/block/state/BlockState;)I"
            )
    )
    private int getDelay(DiodeBlock diode, BlockState state, World world, BlockPos pos) {
        return getDelay(state, world, pos);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/DiodeBlock;getTickingDelay(Lnet/minecraft/block/state/BlockState;)I"
            )
    )
    private int getTickDelay(DiodeBlock diode, BlockState state, World world, BlockPos pos) {
        return getTickDelay(state, world, pos);
    }
}
