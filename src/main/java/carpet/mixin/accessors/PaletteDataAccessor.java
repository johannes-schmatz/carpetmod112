package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.chunk.palette.PaletteData;

@Mixin(PaletteData.class)
public interface PaletteDataAccessor {
	@Accessor int getBitsPerBlock();
}
