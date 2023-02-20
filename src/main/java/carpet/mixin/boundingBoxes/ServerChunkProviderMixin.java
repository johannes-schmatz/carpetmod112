package carpet.mixin.boundingBoxes;

import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ChunkGenerator;
import net.minecraft.world.chunk.ServerChunkProvider;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkProvider.class)
public class ServerChunkProviderMixin implements BoundingBoxProvider {
    @Shadow @Final private ChunkGenerator generator;

    public NbtList getBoundingBoxes(Entity entity) {
        if (generator instanceof BoundingBoxProvider) return ((BoundingBoxProvider) generator).getBoundingBoxes(entity);
        return new NbtList();
    }
}
