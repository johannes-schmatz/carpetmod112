package carpet.mixin.fallingBlockResearch;

import carpet.utils.extensions.ExtendedRegionFileFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.storage.RegionFile;

import java.io.IOException;
import java.util.List;

@Mixin(RegionFile.class)
public abstract class RegionFileFormatMixin implements ExtendedRegionFileFormat {

	@Shadow protected abstract int getChunkBlockInfo(int chunkX, int chunkZ);

	/**
	 * {@code true} means empty, {@code false} means used. Positions are section offsets.
	 */
	@Shadow private List<Boolean> blockEmptyFlags;

	@Shadow protected abstract void writeChunkBlockInfo(int chunkX, int chunkZ, int length) throws IOException;

	@Shadow protected abstract void writeChunkSaveTime(int chunkX, int chunkZ, int timestamp) throws IOException;

	@Override
	public synchronized void deleteChunk(int x, int z) {
		try {
			int sectorData = this.getChunkBlockInfo(x, z);
			int offset = sectorData >> 8;
			int size = sectorData & 0xFF;

			// mark old chunk data storage as unused
			for(int i = 0; i < size; i++) {
				this.blockEmptyFlags.set(offset + i, true);
			}

			this.writeChunkBlockInfo(x, z, 0);

			this.writeChunkSaveTime(x, z, (int)(MinecraftServer.getTimeMillis() / 1000L));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
