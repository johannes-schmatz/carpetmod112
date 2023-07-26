package carpet.commands;

import carpet.helpers.FallingBlockResearchHelper;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;

public class FallingBlockHelperCommand extends CommandCarpetBase {
	@Override
	public String getCommandName() {
		return "fb";
	}

	@Override
	public String getUsageTranslationKey(CommandSource source) {
		return "fb";
	}

	@Override
	public void method_3279(MinecraftServer minecraftServer, CommandSource arg, String[] args) throws CommandException {
		FallingBlockResearchHelper.log(getAsPlayer(arg));
	}
}
