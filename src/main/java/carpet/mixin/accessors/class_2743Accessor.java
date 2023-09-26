package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.BitStorage;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

@Mixin(PalettedContainer.class)
public interface class_2743Accessor {
	@Accessor("bits") int getBitsPerBlock();
	@Accessor("palette") Palette getPalette();
	@Accessor("storage") BitStorage getPaletteData();
}
