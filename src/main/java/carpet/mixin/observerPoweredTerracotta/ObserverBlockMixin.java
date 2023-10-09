package carpet.mixin.observerPoweredTerracotta;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.ObserverBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ObserverBlock.class)
public class ObserverBlockMixin extends FacingBlock {
    protected ObserverBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @ModifyConstant(
            method = "update",
            constant = @Constant(intValue = 2)
    )
    private int adjustDelay(int delay, BlockState observerState, World world, BlockPos observerWorld) {
        if (CarpetSettings.observerPoweredTerracotta){
            Direction direction = observerState.get(FACING);

            BlockPos pos = observerWorld.offset(direction.getOpposite());
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block == Blocks.STAINED_HARDENED_CLAY){
                delay = block.getDropItemMetadata(state);
                if (delay == 0) delay = 100;
            }
        }
        return delay;
    }
}
