package carpet.helpers;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.Objects;

public class TeleportHelper {

	/**
	 * Adapted from spectator teleport code {@link net.minecraft.server.network.handler.ServerPlayNetworkHandler#handlePlayerSpectate}
	 * @param player
	 * @param target
	 */
	public static void changeDimensions(ServerPlayerEntity player, ServerPlayerEntity target) {
		MinecraftServer server = player.getServer();
		Objects.requireNonNull(server);

		ServerWorld worldFrom = player.getServerWorld();
		ServerWorld worldTo = target.getServerWorld();

		int dimension = worldTo.dimension.getType().getId();
		player.dimensionId = dimension;

		player.networkHandler.sendPacket(
				new PlayerRespawnS2CPacket(
						dimension,
						worldFrom.getDifficulty(),
						worldFrom.getData().getGeneratorType(),
					player.interactionManager.getGameMode()
				)
		);
		server.getPlayerManager().updatePermissions(player);
		worldFrom.removeEntityNow(player);
		player.removed = false;
		player.refreshPositionAndAngles(target.x, target.y, target.z, target.yaw, target.pitch);

		worldFrom.tickEntity(player, false);
		worldTo.addEntity(player);
		worldTo.tickEntity(player, false);

		player.setWorld(worldTo);
		server.getPlayerManager().onChangedDimension(player, worldFrom);

		player.teleport(target.x, target.y, target.z);
		player.interactionManager.setWorld(worldTo);

		server.getPlayerManager().sendWorldInfo(player, worldTo);
		server.getPlayerManager().sendPlayerInfo(player);
	}
}