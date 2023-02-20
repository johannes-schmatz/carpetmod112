package carpet.carpetclient;

import carpet.CarpetSettings;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.PlayerWorldManager;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CarpetClientRandomtickingIndexing {

    private static boolean[] updates = {false, false, false};
    private static boolean enableUpdates = false;
    private static List<ServerPlayerEntity> players = new ArrayList<>();

    public static void enableUpdate(ServerPlayerEntity player) {
        if (!enableUpdates) return;
        int dimention = player.world.dimension.getDimensionType().getId() + 1;
        updates[dimention] = CarpetSettings.randomtickingChunkUpdates;
    }

    public static boolean sendUpdates(World world) {
        int dimention = world.dimension.getDimensionType().getId() + 1;
        return updates[dimention];
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
        int dimention = sender.world.dimension.getDimensionType().getId() + 1;
        updates[dimention] = CarpetSettings.randomtickingChunkUpdates;
    }

    public static void unregisterPlayer(ServerPlayerEntity player) {
        players.remove(player);
        if (players.size() == 0) enableUpdates = false;
    }

    public static void sendRandomtickingChunkOrder(World world, PlayerWorldManager playerChunkMap) {
        NbtCompound compound = new NbtCompound();
        NbtList nbttaglist = new NbtList();
        for (Iterator<Chunk> iterator = playerChunkMap.method_12810(); iterator.hasNext(); ) {
            Chunk c = iterator.next();
            NbtCompound chunkData = new NbtCompound();
            chunkData.putInt("x", c.chunkX);
            chunkData.putInt("z", c.chunkZ);
            nbttaglist.add(chunkData);
        }
        compound.put("list", nbttaglist);
        for (ServerPlayerEntity p : players) {
            CarpetClientMessageHandler.sendNBTRandomTickData(p, compound);
        }

        int dimention = world.dimension.getDimensionType().getId() + 1;
        updates[dimention] = false;
    }

}
