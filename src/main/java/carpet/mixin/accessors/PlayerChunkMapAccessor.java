package carpet.mixin.accessors;

import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.server.PlayerWorldManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PlayerWorldManager.class)
public interface PlayerChunkMapAccessor {
    @Accessor("field_13870") List<ChunkPlayerManager> getEntries();
    @Accessor("field_13871") List<ChunkPlayerManager> getEntriesWithoutChunks();
}
