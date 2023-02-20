package carpet.commands;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.KillCommand;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandRemoveEntity extends KillCommand {
    @Override
    public String getCommandName()
    {
        return "removeEntity";
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            PlayerEntity entityplayer = getAsPlayer(sender);
            entityplayer.kill();
            run(sender, this, "commands.kill.successful", entityplayer.getName());
        }
        else
        {
            Entity entity = method_10711(server, sender, args[0]);
            entity.remove();

            if (!(entity instanceof ServerPlayerEntity))
            {
                ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
                WorldEditBridge.recordEntityRemoval(worldEditPlayer, sender.getWorld(), entity);
            }

            run(sender, this, "commands.kill.successful", entity.getName());
        }
    }

}
