package carpet.mixin.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.world.chunk.ChunkGenerator;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.storage.ChunkStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(ServerChunkCache.class)
public interface ServerChunkProviderAccessor {
    @Accessor("chunksToUnload") Set<Long> getDroppedChunks();
    @Accessor("generator") ChunkGenerator getChunkGenerator();
    @Accessor("storage") ChunkStorage getChunkLoader();
    @Accessor("chunkMap") Long2ObjectMap<WorldChunk> getLoadedChunksMap();
    @Invoker("loadChunk") WorldChunk invokeLoadChunk(int x, int z);
}
