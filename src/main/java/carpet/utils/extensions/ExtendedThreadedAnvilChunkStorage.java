package carpet.utils.extensions;

public interface ExtendedThreadedAnvilChunkStorage {
	void deleteChunk(int x, int z);
	void deleteScheduled();
}
