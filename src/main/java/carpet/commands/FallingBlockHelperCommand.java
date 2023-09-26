package carpet.commands;

import carpet.helpers.FallingBlockResearchHelper;

import net.minecraft.server.command.Command;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.InvalidNumberException;
import net.minecraft.server.MinecraftServer;

public class FallingBlockHelperCommand extends CommandCarpetBase {
	@Override
	public String getName() {
		return "fb";
	}

	@Override
	public String getUsage(CommandSource source) {
		return "fb";
	}

	@Override
	public void run(MinecraftServer minecraftServer, CommandSource arg, String[] args) throws CommandException {
		FallingBlockResearchHelper.log(asPlayer(arg));
	}
}
