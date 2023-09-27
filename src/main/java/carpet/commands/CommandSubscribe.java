package carpet.commands;

import net.minecraft.server.command.source.CommandSource;

public class CommandSubscribe extends CommandLog {

	@Override
	public String getName() {
		return "subscribe";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "/subscribe <subscribeName> [?option]";
	}
}
