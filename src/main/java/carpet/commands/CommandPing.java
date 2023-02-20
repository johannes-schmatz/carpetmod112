package carpet.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class CommandPing extends CommandCarpetBase
{

    @Override
    public String getCommandName()
    {
        return "ping";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return "/ping";
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
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
