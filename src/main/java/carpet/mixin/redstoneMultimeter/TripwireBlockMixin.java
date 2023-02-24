package carpet.mixin.redstoneMultimeter;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import redstone.multimeter.block.Meterable;

import net.minecraft.block.BlockState;
import net.minecraft.block.TripwireBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(TripwireBlock.class)
public class TripwireBlockMixin implements Meterable {
	@Shadow @Final public static BooleanProperty POWERED;

	@Override
	public boolean isActive(World world, BlockPos pos, BlockState state) {
		return state.get(POWERED);
	}
}
