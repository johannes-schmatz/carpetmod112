package carpet.commands;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import carpet.CarpetSettings;
import carpet.utils.EntityInfo;
import carpet.utils.extensions.ActionPackOwner;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class CommandEntityInfo extends CommandCarpetBase
{
    @Override
    public String getCommandName()
    {
        return "entityinfo";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return "Usage: entityinfo <entity_selector>";
    }


    public void print_multi_message(List<String> messages, CommandSource sender, String grep)
    {
        List<String> actual = new ArrayList<>();
        if (grep != null)
        {
            Pattern p = Pattern.compile(grep);
            actual.add(messages.get(0));
            boolean empty = true;
            for (int i = 1; i<messages.size(); i++)
            {
                String line = messages.get(i);
                Matcher m = p.matcher(line);
                if (m.find())
                {
                    empty = false;
                    actual.add(line);
                }
            }
            if (empty)
            {
                return;
            }
        }
        else
        {
            actual = messages;
        }
        run(sender, this, "");
        for (String lline: actual)
        {
            run(sender, this, lline);
        }
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandEntityInfo", sender)) return;
        if (args.length == 0 || "grep".equalsIgnoreCase(args[0]))
        {
            String grep = null;
            if (args.length == 2)
            {
                grep = args[1];
            }
            PlayerEntity entityplayer = getAsPlayer(sender);
            List<String> report = EntityInfo.entityInfo(entityplayer, sender.getWorld());
            print_multi_message(report, sender, grep);
        }
        else
        {
            Entity entity = method_10711(server, sender, args[0]);
            //LOG.error("SENDER dimension "+ sender.method_29608().provider.getDimensionType().getId());
            List<String> report = EntityInfo.entityInfo(entity, sender.getWorld());
            String grep = null;
            if (args.length >= 3 && "grep".equalsIgnoreCase(args[1]))
            {
                grep = args[2];
            }
            print_multi_message(report, sender, grep);
        }
    }

    @Override
    public boolean isUsernameAtIndex(String[] args, int index)
    {
        return index == 0;
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (!CarpetSettings.commandEntityInfo)
        {
            run(sender, this, "Command is disabled in carpet settings");
        }
        List<String> list = method_2894(args, server.getPlayerNames());
        BlockHitResult result = ((ActionPackOwner) sender).getActionPack().mouseOver();
        if (result != null && result.type == BlockHitResult.Type.ENTITY) {
            list.add(result.entity.getUuid().toString());
        }
        return args.length == 1 ? list : Collections.emptyList();
    }
}
