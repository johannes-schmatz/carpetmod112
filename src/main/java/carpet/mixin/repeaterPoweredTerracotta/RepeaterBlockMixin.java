package carpet.mixin.repeaterPoweredTerracotta;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.state.property.IntegerProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RepeaterBlock.class)
public class RepeaterBlockMixin extends AbstractRedstoneGateBlockMixin {
    @Shadow @Final public static IntegerProperty DELAY;

    @Override
    protected int getDelay(BlockState state, World world, BlockPos pos) {
        int delay = 2;
        // Added repeater with adjustable delay on terracota CARPET-XCOM
        if (CarpetSettings.repeaterPoweredTerracotta) {
            BlockState stateBelow = world.getBlockState(pos.down());
            Block blockBelow = stateBelow.getBlock();
            if (blockBelow == Blocks.STAINED_HARDENED_CLAY) {
                delay = blockBelow.getDropItemMetadata(stateBelow);
                if (delay == 0) delay = 100;
            }
        }

        return state.get(DELAY) * delay;
    }
}
