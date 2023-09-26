package carpet.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.state.BlockState;
import net.minecraft.util.CrudeIncrementalIntIdentityHashMap;
import net.minecraft.world.chunk.HashMapPalette;

@Mixin(HashMapPalette.class)
public interface HashMapPaletteAccessor {
	@Accessor("bits") int getBitsPerBlock();
	@Accessor("values") CrudeIncrementalIntIdentityHashMap<BlockState> getMap();
}
