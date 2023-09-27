package carpet.commands;

import net.minecraft.server.command.source.CommandSource;

public class CommandDebuglogger extends CommandLog {

	@Override
	public String getName() {
		return "logdebug";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "/logdebug (interactive menu) OR /logdebug <logName> [?option] [player] [handler ...] OR /logdebug <logName> clear [player] OR /logdebug defaults (interactive menu) OR /logdebug setDefault <logName> [?option] [handler ...] OR /logdebug removeDefault <logName>";
	}
}
