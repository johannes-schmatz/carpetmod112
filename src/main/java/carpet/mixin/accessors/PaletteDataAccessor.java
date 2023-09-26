package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.BitStorage;

@Mixin(BitStorage.class)
public interface PaletteDataAccessor {
	@Accessor("bits") int getBitsPerBlock();
}
