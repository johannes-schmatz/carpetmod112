package carpet.commands;

import carpet.worldedit.WorldEditBridge;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.InvalidNumberException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.KillCommand;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;

public class CommandRemoveEntity extends KillCommand {
	@Override
	public String getName() {
		return "removeEntity";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (args.length == 0) {
			PlayerEntity entityplayer = asPlayer(sender);
			entityplayer.m_3468489();
			sendSuccess(sender, this, "commands.kill.successful", entityplayer.getName());
		} else {
			Entity entity = parseEntity(server, sender, args[0]);
			entity.remove();

			if (!(entity instanceof ServerPlayerEntity)) {
				ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
				WorldEditBridge.recordEntityRemoval(worldEditPlayer, sender.getSourceWorld(), entity);
			}

			sendSuccess(sender, this, "commands.kill.successful", entity.getName());
		}
	}

}
