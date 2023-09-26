package carpet.mixin.fallingBlockResearch;

import carpet.utils.extensions.ExtendedFileIoThread;
import carpet.utils.extensions.ExtendedRegionFileFormat;
import carpet.utils.extensions.ExtendedThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.storage.AnvilChunkStorage;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionIo;
import net.minecraft.world.chunk.storage.io.FileIoThread;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Mixin(AnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin implements ExtendedThreadedAnvilChunkStorage {
	@Shadow @Final private File dir;
	private final Set<ChunkPos> chunksToDelete = Collections.synchronizedSet(new HashSet<>());

	@Override
	public void deleteChunk(int x, int z) {
		ChunkPos pos = new ChunkPos(x, z);

		chunksToDelete.add(pos);

		((ExtendedFileIoThread) FileIoThread.getInstance()).registerDeletionCallback(this);
	}

	@Override
	public void deleteScheduled() { // runs on other thread
		if (!chunksToDelete.isEmpty()) {
			for (ChunkPos pos : chunksToDelete) {
				RegionFile format = RegionIo.getRegionFile(this.dir, pos.x, pos.z);

				((ExtendedRegionFileFormat) format).deleteChunk(pos.x & 31, pos.z & 31);
			}
			chunksToDelete.clear();
		}
	}
}
