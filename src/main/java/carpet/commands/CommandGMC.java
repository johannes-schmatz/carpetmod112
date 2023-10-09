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
			ServerPlayerEntity entityplayer = asPlayer(sender);
			if (entityplayer.isSpectator()) return;
			if (CarpetSettings.cameraModeSurvivalRestrictions && entityplayer.interactionManager.getGameMode() == GameMode.SURVIVAL) {
				List<HostileEntity> hostiles = sender.getSourceWorld().getEntities(HostileEntity.class, new Box(entityplayer.x - 8.0D,
						entityplayer.y - 5.0D,
						entityplayer.z - 8.0D,
						entityplayer.x + 8.0D,
						entityplayer.y + 5.0D,
						entityplayer.z + 8.0D
				), mob -> mob.isAngryAt(entityplayer));
				StatusEffectInstance fireresist = entityplayer.getEffectInstance(StatusEffect.get("fire_resistance"));
				if (!entityplayer.onGround || entityplayer.isFallFlying() || (((EntityAccessor) entityplayer).getFireTicks() > 0 &&
						(fireresist == null || fireresist.getDuration() < ((EntityAccessor) entityplayer).getFireTicks())) || entityplayer.getBreath() != 300 ||
						!hostiles.isEmpty()) {
					sendSuccess(sender, this, "Restricted use to: on ground, not in water, not on fire, not flying/falling, not near hostile mobs.");
					return;
				}
			}
			StatusEffect nightvision = StatusEffect.get("night_vision");
			boolean hasNightvision = entityplayer.getEffectInstance(nightvision) != null;
			((CameraPlayer) entityplayer).storeCameraData(hasNightvision);
			GameMode gametype = GameMode.SPECTATOR;
			entityplayer.setGameMode(gametype);
			if (!hasNightvision && !LoggerRegistry.getLogger("normalCameraVision").subscribed(entityplayer)) {
				StatusEffectInstance potioneffect = new StatusEffectInstance(nightvision, 999999, 0, false, false);
				entityplayer.addStatusEffect(potioneffect);
			}
			((CameraPlayer) entityplayer).setGamemodeCamera();
		}
	}
}
