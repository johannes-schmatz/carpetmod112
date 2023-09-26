package carpet.commands;

import net.minecraft.server.command.Command;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.InvalidNumberException;

public class CommandSubscribe extends CommandLog {

    private final String USAGE = "/subscribe <subscribeName> [?option]";

    @Override
    public String getName() {
        return "subscribe";
    }

    @Override
    public String getUsage(CommandSource sender) {
        return USAGE;
    }
}
