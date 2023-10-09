package carpet.mixin.boundingBoxes;

import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.world.chunk.ChunkGenerator;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkCache.class)
public class ServerChunkProviderMixin implements BoundingBoxProvider {
    @Shadow @Final private ChunkGenerator generator;

    @Override
    public NbtList getBoundingBoxes(Entity entity) {
        if (generator instanceof BoundingBoxProvider) return ((BoundingBoxProvider) generator).getBoundingBoxes(entity);
        return new NbtList();
    }
}
