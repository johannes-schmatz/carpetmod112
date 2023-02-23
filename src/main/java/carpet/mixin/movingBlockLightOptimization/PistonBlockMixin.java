package carpet.mixin.movingBlockLightOptimization;

import carpet.CarpetSettings;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.level.LevelGeneratorType;

import carpet.mixin.accessors.WorldAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PistonBlock.class)
public class PistonBlockMixin extends FacingBlock {
    protected PistonBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @Redirect(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
                    ordinal = 1
            )
    )
    private boolean setBlockState1(World world, BlockPos pos, BlockState newState, int flags) {
        return CarpetSettings.movingBlockLightOptimization || world.setBlockState(pos, newState, flags);
    }

    @Redirect(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
                    ordinal = 2
            )
    )
    private boolean setBlockState2(World world, BlockPos pos, BlockState newState, int flags) {
        return CarpetSettings.movingBlockLightOptimization || world.setBlockState(pos, newState, flags);
    }

    // Added the properties of opacity and light to the moving block as to minimize light updates. CARPET-XCOM
    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
                    ordinal = 2
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void movingBlockLightOptimization(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                                              PistonHandler helper, List<BlockPos> positions, List<BlockState> states, List<BlockPos> list2, int k, BlockState[] aiblockstate, Direction enumfacing, int index, BlockPos currentPos, BlockState currentState) {
        if (!CarpetSettings.movingBlockLightOptimization) return;
        BlockPos posOld = positions.get(index); // basically undo the offset
        boolean remove = true;
        for (int backwardCheck = index - 1; backwardCheck >= 0; --backwardCheck){
            BlockPos blockposCheck = positions.get(backwardCheck);
            if(blockposCheck.offset(enumfacing).equals(posOld)){
                remove = false;
                break;
            }
        }
        BlockState movingBlock = Blocks.PISTON_EXTENSION.getDefaultState().with(FACING, direction);

        int opacity = currentState.getOpacity();
        int light = currentState.getLuminance();

        System.out.println("oop " + worldIn.getBlockState(posOld) + " at " + posOld);

        setBlockState(worldIn, currentPos, movingBlock, opacity, light);

        if (remove){
            worldIn.setBlockState(posOld, Blocks.AIR.getDefaultState(), 2);
        }
        worldIn.method_13693(currentPos, movingBlock.getBlock());
    }

    /**
     * A copy of {@link net.minecraft.world.World#setBlockState(BlockPos, BlockState, int)}, that has {@code flags = 20} and allows setting the luminance
     * of the new given BlockState.
     * @param world the world
     * @param pos the position
     * @param state the state to set
     * @param opacity the opacity of the block to set
     * @param luminance the luminance of the block to set
     */
    private void setBlockState(World world, BlockPos pos, BlockState state, int opacity, int luminance) {
        //world.setBlockState(pos, state, 20);

        // changed in a way to allow us to control the light values of the block we set
        // TODO: consider this to be a new method for something like ExtendedWorld or a helper class
        if (
                !((WorldAccessor) world).invokeIsOutsideWorld(pos)
                        && !(!world.isClient
                        && world.getLevelProperties().getGeneratorType() == LevelGeneratorType.DEBUG)
        ) {
            Chunk chunk = world.getChunk(pos);
            BlockState oldState = chunk.getBlockState(pos, state);
            if (oldState != null) {
                if (opacity != oldState.getOpacity() || luminance != oldState.getLuminance()) {
                    /*System.out.println("pos: " + pos);
                    System.out.println("old block: " + oldState);
                    System.out.println("new block: " + state);
                    System.out.println("old: " + oldState.getOpacity() + ", " + oldState.getLuminance());
                    System.out.println("new: " + opacity + ", " + luminance);*/
                    world.profiler.push("checkLight");
                    world.method_8568(pos);
                    world.profiler.pop();
                }
            }
            // removed the other checks because flags = 20
        }
    }
}
