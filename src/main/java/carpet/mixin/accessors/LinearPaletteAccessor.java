package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.chunk.palette.LinearPalette;

@Mixin(LinearPalette.class)
public interface LinearPaletteAccessor {
	@Accessor int getBitsPerBlock();
	@Accessor int getSize();
}
