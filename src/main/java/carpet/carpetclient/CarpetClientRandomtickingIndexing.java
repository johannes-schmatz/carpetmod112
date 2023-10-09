package carpet.carpetclient;

import carpet.CarpetSettings;

import net.minecraft.server.ChunkMap;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CarpetClientRandomtickingIndexing {

    private static final boolean[] updates = {false, false, false};
    private static boolean enableUpdates = false;
    private static final List<ServerPlayerEntity> players = new ArrayList<>();

    public static void enableUpdate(ServerPlayerEntity player) {
        if (!enableUpdates) return;
        int dimension = player.world.dimension.getType().getId() + 1;
        updates[dimension] = CarpetSettings.randomtickingChunkUpdates;
    }

    public static boolean sendUpdates(World world) {
        int dimension = world.dimension.getType().getId() + 1;
        return updates[dimension];
    }

    public static void register(ServerPlayerEntity sender, PacketByteBuf data) {
        boolean register = data.readBoolean();
        if (register) {
            registerPlayer(sender);
        } else {
            unregisterPlayer(sender);
        }
    }

    private static void registerPlayer(ServerPlayerEntity sender) {
        players.add(sender);
        enableUpdates = true;
        int dimention = sender.world.dimension.getType().getId() + 1;
        updates[dimention] = CarpetSettings.randomtickingChunkUpdates;
    }

    public static void unregisterPlayer(ServerPlayerEntity player) {
        players.remove(player);
        if (players.isEmpty()) enableUpdates = false;
    }

    public static void sendRandomtickingChunkOrder(World world, ChunkMap playerChunkMap) {
        NbtCompound compound = new NbtCompound();
        NbtList list = new NbtList();
        for (Iterator<WorldChunk> iterator = playerChunkMap.getTickingChunks(); iterator.hasNext(); ) {
            WorldChunk c = iterator.next();
            NbtCompound chunkData = new NbtCompound();
            chunkData.putInt("x", c.chunkX);
            chunkData.putInt("z", c.chunkZ);
            list.add(chunkData);
        }
        compound.put("list", list);
        for (ServerPlayerEntity p : players) {
            CarpetClientMessageHandler.sendNBTRandomTickData(p, compound);
        }

        int dimention = world.dimension.getType().getId() + 1;
        updates[dimention] = false;
    }

}
