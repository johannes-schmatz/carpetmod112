package carpet.mixin.accessors;

import net.minecraft.server.ChunkHolder;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.world.chunk.WorldChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChunkHolder.class)
public interface PlayerChunkMapEntryAccessor {
    @Accessor("players") List<ServerPlayerEntity> getPlayers();
    @Accessor("chunk") void setChunk(WorldChunk chunk);
    @Accessor("populated") void setSentToPlayers(boolean sent);
    @Accessor("blocksChanged") void setChanges(int changes);
}
