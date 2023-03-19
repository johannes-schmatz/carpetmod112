package carpet.mixin.fallingBlockResearch;

import carpet.utils.extensions.ExtendedRegionFileFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.RegionFileFormat;

import java.io.IOException;
import java.util.List;

@Mixin(RegionFileFormat.class)
public abstract class RegionFileFormatMixin implements ExtendedRegionFileFormat {

	@Shadow protected abstract int getSectorData(int chunkX, int chunkZ);

	/**
	 * {@code true} means empty, {@code false} means used. Positions are section offsets.
	 */
	@Shadow private List<Boolean> field_9956;

	@Shadow protected abstract void writeSectorData(int chunkX, int chunkZ, int length) throws IOException;

	@Shadow protected abstract void writeSaveTime(int chunkX, int chunkZ, int timestamp) throws IOException;

	@Override
	public synchronized void deleteChunk(int x, int z) {
		try {
			int sectorData = this.getSectorData(x, z);
			int offset = sectorData >> 8;
			int size = sectorData & 0xFF;

			// mark old chunk data storage as unused
			for(int i = 0; i < size; i++) {
				this.field_9956.set(offset + i, true);
			}

			this.writeSectorData(x, z, 0);

			this.writeSaveTime(x, z, (int)(MinecraftServer.getTimeMillis() / 1000L));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
