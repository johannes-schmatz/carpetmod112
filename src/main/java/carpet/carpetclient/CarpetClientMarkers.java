package carpet.carpetclient;

import java.util.ArrayList;

import carpet.mixin.accessors.StructureFeatureAccessor;
import carpet.utils.extensions.BoundingBoxProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureFeature;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.gen.GeneratorConfig;

public class CarpetClientMarkers {

    public static final int OUTER_BOUNDING_BOX = 0;
    public static final int END_CITY = 1;
    public static final int FORTRESS = 2;
    public static final int TEMPLE = 3;
    public static final int VILLAGE = 4;
    public static final int STRONGHOLD = 5;
    public static final int MINESHAFT = 6;
    public static final int MONUMENT = 7;
    public static final int MANSION = 8;

    private static ArrayList<ServerPlayerEntity> playersVillageMarkers = new ArrayList<>();

    public static void updateClientVillageMarkers(World worldObj) {
        if (playersVillageMarkers.size() == 0) {
            return;
        }
        NbtList nbttaglist = new NbtList();
        NbtCompound tagCompound = new NbtCompound();

        for (Village village : worldObj.getVillageState().method_2843()) {
            NbtCompound nbttagcompound = new NbtCompound();
            village.toNbt(nbttagcompound);
            nbttaglist.add(nbttagcompound);
        }

        tagCompound.put("Villages", nbttaglist);

        for (ServerPlayerEntity sender : playersVillageMarkers) {
            CarpetClientMessageHandler.sendNBTVillageData(sender, tagCompound);
        }
    }

    public static void updateClientBoundingBoxMarkers(ServerPlayerEntity sender, PacketByteBuf data) {
        MinecraftServer ms = sender.world.getServer();
        ServerWorld ws = ms.getWorld(sender.dimension);
        NbtList list = ((BoundingBoxProvider) ws.getChunkProvider()).getBoundingBoxes(sender);
        NbtCompound nbttagcompound = new NbtCompound();

        nbttagcompound.put("Boxes", list);
        nbttagcompound.putInt("Dimention", sender.dimension);
        nbttagcompound.putLong("Seed", sender.world.getSeed());

        CarpetClientMessageHandler.sendNBTBoundingboxData(sender, nbttagcompound);
    }

    public static void registerVillagerMarkers(ServerPlayerEntity sender, PacketByteBuf data) {
        boolean addPlayer = data.readBoolean();
        if (addPlayer) {
            playersVillageMarkers.add(sender);
            updateClientVillageMarkers(sender.world);
        } else {
            playersVillageMarkers.remove(sender);
        }
    }

    public static void unregisterPlayerVillageMarkers(ServerPlayerEntity player) {
        playersVillageMarkers.remove(player);
    }

    // Retrieval method to get the bounding boxes CARPET-XCOM
    public static NbtList getBoundingBoxes(StructureFeature structure, Entity entity, int type) {
        NbtList list = new NbtList();
        for (GeneratorConfig structurestart : ((StructureFeatureAccessor) structure).getStructureMap().values()) {
            if (MathHelper.sqrt(new ChunkPos(structurestart.getChunkX(), structurestart.getChunkZ()).squaredDistanceToCenter(entity)) > 700) {
                continue;
            }
            NbtCompound outerBox = new NbtCompound();
            outerBox.putInt("type", OUTER_BOUNDING_BOX);
            outerBox.put("bb", structurestart.getBoundingBox().toNbt());
            list.add(outerBox);
            for (StructurePiece child : structurestart.method_11855()) {
                NbtCompound innerBox = new NbtCompound();
                innerBox.putInt("type", type);
                innerBox.put("bb", child.getBoundingBox().toNbt());
                list.add(innerBox);
            }
        }
        return list;
    }
}
