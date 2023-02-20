package carpet.commands;

import net.minecraft.command.CommandSource;

public class CommandDebuglogger extends CommandLog {

    private final String USAGE = "/logdebug (interactive menu) OR /logdebug <logName> [?option] [player] [handler ...] OR /logdebug <logName> clear [player] OR /logdebug defaults (interactive menu) OR /logdebug setDefault <logName> [?option] [handler ...] OR /logdebug removeDefault <logName>";

    @Override
    public String getCommandName() {
        return "logdebug";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender) {
        return USAGE;
    }
}
