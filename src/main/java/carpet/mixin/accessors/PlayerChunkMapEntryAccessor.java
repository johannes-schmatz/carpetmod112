package carpet.mixin.accessors;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChunkPlayerManager.class)
public interface PlayerChunkMapEntryAccessor {
    @Accessor("players") List<ServerPlayerEntity> getPlayers();
    @Accessor("chunk") void setChunk(Chunk chunk);
    @Accessor("field_13865") void setSentToPlayers(boolean sent);
    @Accessor("field_8888") void setChanges(int changes);
}
