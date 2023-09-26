package carpet.mixin.repeaterPoweredTerracotta;

import net.minecraft.block.state.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ComparatorBlock.class)
public abstract class ComparatorBlockMixin extends AbstractRedstoneGateBlockMixin {
    @Shadow protected abstract int getDelay(BlockState state);

    @Override
    protected int getDelay(BlockState state, World world, BlockPos pos) {
        return getDelay(state);
    }
}
