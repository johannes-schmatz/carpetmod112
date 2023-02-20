package carpet.mixin.accessors;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.chunk.ThreadedAnvilChunkStorage;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface ThreadedAnvilChunkStorageAccessor {
    @Accessor("saveLocation") File getChunkSaveLocation();
    @Invoker("putChunk") void invokeWriteChunkToNBT(Chunk chunk, World world, NbtCompound tag);
}
