package carpet.mixin.accessors;

import net.minecraft.block.state.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RedstoneWireBlock.class)
public interface RedstoneWireBlockAccessor {
    @Accessor("shouldSignal") boolean getWiresGivePower();
    @Accessor("shouldSignal") void setWiresGivePower(boolean wiresGivePower);
    @Invoker("connectsTo") boolean invokeCouldConnectTo(WorldView world, BlockPos pos, Direction side);
    @Invoker("doUpdatePower") BlockState invokeCalculateCurrentChanges(World world, BlockPos pos1, BlockPos pos2, BlockState state);
}
