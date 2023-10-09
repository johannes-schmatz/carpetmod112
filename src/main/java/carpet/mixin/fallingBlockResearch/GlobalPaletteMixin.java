package carpet.mixin.fallingBlockResearch;

import carpet.utils.extensions.ExtendedPalette;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.GlobalPalette;

@Mixin(GlobalPalette.class)
public class GlobalPaletteMixin implements ExtendedPalette {
	@Override
	public int getBitsPerBlock() {
		return 0;
	}

	@Override
	public int getSize() {
		return Block.STATE_REGISTRY.size();
	}
}
