package carpet.mixin.entityDuplicationFix;

import carpet.CarpetSettings;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.storage.AnvilChunkStorage;
import net.minecraft.world.chunk.storage.ChunkStorage;

@Mixin(AnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin implements ChunkStorage {
    @Shadow @Final private final Map<ChunkPos, NbtCompound> chunkSaveQueue = new HashMap<>();
    @Shadow private boolean saving;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private File dir;

    @Shadow protected abstract void saveChunk(ChunkPos pos, NbtCompound compound) throws IOException;

    private final Map<ChunkPos, NbtCompound> chunksInWrite = new HashMap<>();
    /** Insert new chunk into pending queue, replacing any older one at the same position */
    private synchronized void queueChunkToRemove(ChunkPos pos, NbtCompound data) {
        chunkSaveQueue.put(pos, data);
    }

    /**
     * Fetch another chunk to save to disk and atomically move it into
     * the queue of chunk(s) being written.
     */
    private synchronized Map.Entry<ChunkPos, NbtCompound> fetchChunkToWrite() {
        if (chunkSaveQueue.isEmpty()) return null;
        Iterator<Map.Entry<ChunkPos, NbtCompound>> iter =
                chunkSaveQueue.entrySet().iterator();
        Map.Entry<ChunkPos, NbtCompound> entry = iter.next();
        iter.remove();
        chunksInWrite.put(entry.getKey(), entry.getValue());
        return entry;
    }

    /**
     * Once the write for a chunk is completely committed to disk,
     * this method discards it
     */
    private synchronized void retireChunkToWrite(ChunkPos pos, NbtCompound data) {
        chunksInWrite.remove(pos);
    }

    /** Check these data structures for a chunk being reloaded */
    private synchronized NbtCompound reloadChunkFromRemoveQueues(ChunkPos pos) {
        NbtCompound data = chunkSaveQueue.get(pos);
        if (data != null) return data;
        return (CarpetSettings.entityDuplicationFix)?chunksInWrite.get(pos):data;
    }

    // Check if chunk exists at all in any pending save state
    //synchronized private boolean chunkExistInRemoveQueues(ChunkPos pos)
    //{
    //    return chunksToRemove.containsKey(pos) || chunksInWrite.containsKey(pos);
    //}

    /* --- end of new code for MC-119971 --- */

    @Redirect(
            method = {
                    "loadChunk(Lnet/minecraft/world/World;II)Lnet/minecraft/world/chunk/WorldChunk;",
                    "doesChunkExist"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            )
    )
    private Object get(Map<ChunkPos, NbtCompound> map, Object key) {
        return reloadChunkFromRemoveQueues((ChunkPos) key);
    }

    @Redirect(
            method = "queueChunkSave",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private boolean isInWrite(Set<ChunkPos> set, Object o) {
        if (CarpetSettings.entityDuplicationFix) return false;
        //noinspection SuspiciousMethodCalls
        return chunksInWrite.containsKey(o);
    }

    @Redirect(
            method = "queueChunkSave",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            )
    )
    private Object queueRemove(Map<ChunkPos, NbtCompound> map, Object key, Object value) {
        queueChunkToRemove((ChunkPos) key, (NbtCompound) value);
        return null;
    }

    /**
     * @author skyrising
     * @reason carpet
     */
    @Overwrite
    public boolean run() {
        Map.Entry<ChunkPos, NbtCompound> entry = fetchChunkToWrite();
        if (entry == null) {
            if (this.saving) {
                LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", this.dir.getName());
            }

            return false;
        }

        ChunkPos pos = entry.getKey();
        NbtCompound tag = entry.getValue();
        try {
            this.saveChunk(pos, tag);
        } catch (Exception exception) {
            LOGGER.error("Failed to save chunk", exception);
        }

        retireChunkToWrite(pos, tag);
        return true;
    }
}
