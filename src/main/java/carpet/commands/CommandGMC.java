package carpet.commands;

import carpet.CarpetSettings;
import carpet.logging.LoggerRegistry;
import carpet.mixin.accessors.EntityAccessor;
import carpet.utils.extensions.CameraPlayer;

import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.effect.StatusEffectInstance;
import net.minecraft.entity.living.mob.hostile.HostileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;

import java.util.List;

public class CommandGMC extends CommandCarpetBase {
	@Override
	public String getName() {
		return "c";
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
			if (!CarpetSettings.commandCameramode) {
				sendSuccess(sender, this, "Quick gamemode switching is disabled");
			}

			ServerPlayerEntity player = asPlayer(sender);

			if (player.isSpectator()) return;
			if (CarpetSettings.cameraModeSurvivalRestrictions && player.interactionManager.getGameMode() == GameMode.SURVIVAL) {
				if (!isUnrestricted(sender, player)) {
					sendSuccess(sender, this, "Restricted use to: on ground, not in water, not on fire, not flying/falling, not near hostile mobs.");
					return;
				}
			}

			StatusEffect nightVision = StatusEffect.get("night_vision");

			boolean hasNightVision = player.getEffectInstance(nightVision) != null;
			((CameraPlayer) player).storeCameraData(hasNightVision);

			player.setGameMode(GameMode.SPECTATOR);

			if (hasNightVision || LoggerRegistry.getLogger("normalCameraVision").subscribed(player)) {
				StatusEffectInstance effect = new StatusEffectInstance(nightVision, 999999, 0, false, false);
				player.addStatusEffect(effect);
			}

			((CameraPlayer) player).setGameModeCamera();
		}
	}

	private static boolean isUnrestricted(CommandSource sender, ServerPlayerEntity player) {
		List<HostileEntity> hostiles = sender.getSourceWorld()
				.getEntities(
						HostileEntity.class,
						new Box(
								player.x - 8.0D,
								player.y - 5.0D,
								player.z - 8.0D,
								player.x + 8.0D,
								player.y + 5.0D,
								player.z + 8.0D
						),
						mob -> mob.isAngryAt(player)
				);

		StatusEffectInstance fireResistance = player.getEffectInstance(StatusEffect.get("fire_resistance"));

		int fireTicks = ((EntityAccessor) player).getFireTicks();

		return player.onGround
				&& !player.isFallFlying()
				&& (
						fireTicks <= 0 ||
						(fireResistance != null && fireResistance.getDuration() >= fireTicks)
				)
				&& player.getBreath() == 300
				&& hostiles.isEmpty();
	}
}
