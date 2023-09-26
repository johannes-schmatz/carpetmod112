package carpet.commands;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.InvalidNumberException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class CommandPing extends CommandCarpetBase
{

    @Override
    public String getName()
    {
        return "ping";
    }

    @Override
    public String getUsage(CommandSource sender)
    {
        return "/ping";
    }

    @Override
    public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandPing", sender))
            return;

        if (sender instanceof ServerPlayerEntity)
        {
            int ping = ((ServerPlayerEntity) sender).ping;
            sender.sendMessage(new LiteralText("Your ping is: " + ping + " ms"));
        }
        else
        {
            throw new CommandException("Only a player can have a ping!");
        }
    }

}
