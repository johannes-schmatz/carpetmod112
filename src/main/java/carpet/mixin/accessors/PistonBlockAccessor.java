package carpet.mixin.accessors;

import net.minecraft.block.PistonBaseBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PistonBaseBlock.class)
public interface PistonBlockAccessor {
    @Invoker("shouldExtend") boolean invokeShouldExtend(World world, BlockPos pos, Direction facing);
}
