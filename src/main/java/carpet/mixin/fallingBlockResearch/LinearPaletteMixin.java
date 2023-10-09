package carpet.mixin.fallingBlockResearch;

import carpet.utils.extensions.ExtendedPalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.chunk.LinearPalette;

@Mixin(LinearPalette.class)
public class LinearPaletteMixin implements ExtendedPalette {
	@Shadow @Final private int bits;

	@Shadow private int size;

	@Override
	public int getBitsPerBlock() {
		return bits;
	}

	@Override
	public int getSize() {
		return size;
	}
}
