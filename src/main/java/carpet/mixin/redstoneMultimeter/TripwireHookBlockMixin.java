package carpet.mixin.redstoneMultimeter;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import redstone.multimeter.block.Meterable;
import redstone.multimeter.block.PowerSource;

import net.minecraft.block.BlockState;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(TripwireHookBlock.class)
public class TripwireHookBlockMixin implements Meterable, PowerSource {

	@Shadow @Final public static BooleanProperty POWERED;

	@Override
	public boolean isActive(World world, BlockPos pos, BlockState state) {
		return state.get(POWERED);
	}

	@Override
	public int getPowerLevel(World world, BlockPos pos, BlockState state) {
		return state.get(POWERED) ? MAX_POWER : MIN_POWER;
	}
}
