package carpet.mixin.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.server.world.ChunkGenerator;
import net.minecraft.world.chunk.ChunkStorage;
import net.minecraft.world.chunk.ServerChunkProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ServerChunkProvider.class)
public interface ServerChunkProviderAccessor {
    @Accessor("chunksToUnload") Set<Long> getDroppedChunks();
    @Accessor("generator") ChunkGenerator getChunkGenerator();
    @Accessor("chunkWriter") ChunkStorage getChunkLoader();
    @Accessor("loadedChunksMap") Long2ObjectMap<Chunk> getLoadedChunksMap();
}
