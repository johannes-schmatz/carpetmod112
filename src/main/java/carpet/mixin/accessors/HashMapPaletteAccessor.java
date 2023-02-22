package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.chunk.palette.HashMapPalette;

@Mixin(HashMapPalette.class)
public interface HashMapPaletteAccessor {
	@Accessor int getBitsPerBlock();
}
