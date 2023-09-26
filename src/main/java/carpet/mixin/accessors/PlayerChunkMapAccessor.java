package carpet.mixin.accessors;

import net.minecraft.server.ChunkHolder;
import net.minecraft.server.ChunkMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChunkMap.class)
public interface PlayerChunkMapAccessor {
    @Accessor("populating") List<ChunkHolder> getEntries();
    @Accessor("loading") List<ChunkHolder> getEntriesWithoutChunks();
}
