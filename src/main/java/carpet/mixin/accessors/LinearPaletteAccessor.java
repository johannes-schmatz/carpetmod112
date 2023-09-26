package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.chunk.LinearPalette;

@Mixin(LinearPalette.class)
public interface LinearPaletteAccessor {
	@Accessor("bits") int getBitsPerBlock();
	@Accessor("size") int getSize();
}
