package carpet.helpers;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class TeleportHelper {

	public static void changeDimensions(ServerPlayerEntity player, ServerPlayerEntity target){
		// Adapted from spectator teleport code (NetHandlerPlayServer::handleSpectate)
		double x = target.x;
		double y = target.y;
		double z = target.z;
		MinecraftServer server = player.getMinecraftServer();
		assert server != null;

		ServerWorld worldFrom = (ServerWorld) player.world;
		ServerWorld worldTo = (ServerWorld) target.world;
		int dimension = worldTo.dimension.getDimensionType().getId();
		player.dimension = dimension;

		player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(dimension, worldFrom.getGlobalDifficulty(), worldFrom.getLevelProperties().getGeneratorType(),
				player.interactionManager.getGameMode()));
		server.getPlayerManager().method_12831(player);
		worldFrom.method_3700(player);
		player.removed = false;
		player.refreshPositionAndAngles(x, y, z, (float) target.yaw, (float) target.pitch);

		worldFrom.checkChunk(player, false);
		worldTo.spawnEntity(player);
		worldTo.checkChunk(player, false);

		player.setWorld(worldTo);
		server.getPlayerManager().method_1986(player, worldFrom);

		player.refreshPositionAfterTeleport(x, y, z);
		player.interactionManager.setWorld(worldTo);
		server.getPlayerManager().sendWorldInfo(player, worldTo);
		server.getPlayerManager().method_2009(player);
	}
}