package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.class_2743;
import net.minecraft.world.chunk.palette.Palette;
import net.minecraft.world.chunk.palette.PaletteData;

@Mixin(class_2743.class)
public interface class_2743Accessor {
	@Accessor int getBitsPerBlock();
	@Accessor Palette getPalette();
	@Accessor PaletteData getPaletteData();
}
