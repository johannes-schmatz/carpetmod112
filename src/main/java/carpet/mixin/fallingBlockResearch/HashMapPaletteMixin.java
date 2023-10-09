package carpet.mixin.fallingBlockResearch;

import carpet.utils.extensions.ExtendedPalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.state.BlockState;
import net.minecraft.util.CrudeIncrementalIntIdentityHashMap;
import net.minecraft.world.chunk.HashMapPalette;

@Mixin(HashMapPalette.class)
public class HashMapPaletteMixin implements ExtendedPalette {
	@Shadow @Final private int bits;

	@Shadow @Final private CrudeIncrementalIntIdentityHashMap<BlockState> values;

	@Override
	public int getBitsPerBlock() {
		return bits;
	}

	@Override
	public int getSize() {
		return values.size();
	}
}
