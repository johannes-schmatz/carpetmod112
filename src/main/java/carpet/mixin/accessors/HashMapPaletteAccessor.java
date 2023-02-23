package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.BlockState;
import net.minecraft.class_2929;
import net.minecraft.world.chunk.palette.HashMapPalette;

@Mixin(HashMapPalette.class)
public interface HashMapPaletteAccessor {
	@Accessor int getBitsPerBlock();
	@Accessor("field_12908") class_2929<BlockState> getMap();
}
