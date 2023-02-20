package carpet.commands;

import net.minecraft.command.CommandSource;

public class CommandSubscribe extends CommandLog {

    private final String USAGE = "/subscribe <subscribeName> [?option]";

    @Override
    public String getCommandName() {
        return "subscribe";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender) {
        return USAGE;
    }
}
