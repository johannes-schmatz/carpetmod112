package carpet.mixin.accessors;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.storage.AnvilChunkStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(AnvilChunkStorage.class)
public interface ThreadedAnvilChunkStorageAccessor {
    @Accessor("dir") File getChunkSaveLocation();
    @Invoker("writeChunkToNbt") void invokeWriteChunkToNBT(WorldChunk chunk, World world, NbtCompound tag);
}
