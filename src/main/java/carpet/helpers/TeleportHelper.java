package carpet.helpers;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class TeleportHelper {

	public static void changeDimensions(ServerPlayerEntity player, ServerPlayerEntity target){
		// Adapted from spectator teleport code (NetHandlerPlayServer::handleSpectate)
		double x = target.x;
		double y = target.y;
		double z = target.z;
		MinecraftServer server = player.getServer();
		assert server != null;

		ServerWorld worldFrom = (ServerWorld) player.world;
		ServerWorld worldTo = (ServerWorld) target.world;
		int dimension = worldTo.dimension.getType().getId();
		player.dimensionId = dimension;

		player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(dimension, worldFrom.getDifficulty(), worldFrom.getData().getGeneratorType(),
				player.interactionManager.getGameMode()));
		server.getPlayerManager().updatePermissions(player);
		worldFrom.removeEntityNow(player);
		player.removed = false;
		player.refreshPositionAndAngles(x, y, z, (float) target.yaw, (float) target.pitch);

		worldFrom.tickEntity(player, false);
		worldTo.addEntity(player);
		worldTo.tickEntity(player, false);

		player.setWorld(worldTo);
		server.getPlayerManager().onChangedDimension(player, worldFrom);

		player.teleport(x, y, z);
		player.interactionManager.setWorld(worldTo);
		server.getPlayerManager().sendWorldInfo(player, worldTo);
		server.getPlayerManager().sendPlayerInfo(player);
	}
}