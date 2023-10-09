package carpet.commands;

import carpet.logging.logHelpers.DebugLogHelper;
import carpet.utils.extensions.CameraPlayer;

import net.minecraft.entity.living.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class CommandGMS extends CommandCarpetBase {
	@Override
	public String getName() {
		return "s";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "commands.gamemode.usage";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandCameramode", sender)) return;
		if (args.length > 0) {
			throw new IncorrectUsageException(getUsage(sender));
		} else {
			ServerPlayerEntity entityplayer = asPlayer(sender);
			setPlayerToSurvival(server, entityplayer, false);
		}
	}

	public static void setPlayerToSurvival(MinecraftServer server, ServerPlayerEntity entityplayer, boolean alwaysPutPlayerInSurvival) {
		GameMode gametype = server.getDefaultGameMode();
		if (entityplayer.interactionManager.getGameMode() != GameMode.SURVIVAL) {
			DebugLogHelper.invisDebug(() -> "s1: " + entityplayer.world.players.contains(entityplayer));
			if (((CameraPlayer) entityplayer).moveToStoredCameraData() && !alwaysPutPlayerInSurvival) {
				DebugLogHelper.invisDebug(() -> "s7: " + entityplayer.world.players.contains(entityplayer));
				return;
			}
			entityplayer.fallDistance = 0;
			DebugLogHelper.invisDebug(() -> "s5: " + entityplayer.world.players.contains(entityplayer));
			if (gametype == GameMode.SPECTATOR) {
				entityplayer.setGameMode(GameMode.SURVIVAL);
			} else {
				entityplayer.setGameMode(gametype);
			}
			if (!((CameraPlayer) entityplayer).hadNightvision()) entityplayer.removeStatusEffect(StatusEffects.NIGHT_VISION);
			DebugLogHelper.invisDebug(() -> "s6: " + entityplayer.world.players.contains(entityplayer));
		}
	}
}
