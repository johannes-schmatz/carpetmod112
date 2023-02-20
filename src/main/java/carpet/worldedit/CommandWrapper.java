package carpet.worldedit;

import java.util.Arrays;
import java.util.List;

import com.sk89q.worldedit.util.command.CommandMapping;

import carpet.commands.CommandCarpetBase;
import net.minecraft.command.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;

class CommandWrapper extends CommandCarpetBase {
    private CommandMapping command;

    protected CommandWrapper(CommandMapping command) {
        this.command = command;
    }

    @Override
    public String getCommandName() {
        return command.getPrimaryAlias();
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList(command.getAllAliases());
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource var1, String[] var2) {}

    @Override
    public String getUsageTranslationKey(CommandSource sender) {
        return "/" + command.getPrimaryAlias() + " " + command.getDescription().getUsage();
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public boolean method_3278(MinecraftServer server, CommandSource sender) {
        return command_enabled("worldEdit", sender); // Will send an extra vanilla permission message but that's the best we can do
    }

    @Override
    public int compareTo(Command o) {
        return super.compareTo(o);
    }
}
