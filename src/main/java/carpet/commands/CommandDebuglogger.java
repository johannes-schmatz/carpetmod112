package carpet.commands;

import net.minecraft.server.command.Command;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.InvalidNumberException;

public class CommandDebuglogger extends CommandLog {

    private final String USAGE = "/logdebug (interactive menu) OR /logdebug <logName> [?option] [player] [handler ...] OR /logdebug <logName> clear [player] OR /logdebug defaults (interactive menu) OR /logdebug setDefault <logName> [?option] [handler ...] OR /logdebug removeDefault <logName>";

    @Override
    public String getName() {
        return "logdebug";
    }

    @Override
    public String getUsage(CommandSource sender) {
        return USAGE;
    }
}
