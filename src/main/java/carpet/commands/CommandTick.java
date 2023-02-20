package carpet.commands;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import carpet.utils.CarpetProfiler;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientMessageHandler;
import carpet.helpers.TickSpeed;


public class CommandTick extends CommandCarpetBase
{
    @Override
    public String getCommandName()
    {
        return "tick";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return "Usage: tick rate <tickrate in tps> | warp [time in ticks to skip]";
    }

    @Override
    public void method_3279(final MinecraftServer server, final CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandTick", sender)) return;
        if (args.length == 0)
        {
            throw new IncorrectUsageException(getUsageTranslationKey(sender));
        }
        if ("rate".equalsIgnoreCase(args[0]))
        {
            if (args.length == 2)
            {
                float tickrate = (float) parseClampedDouble(args[1], 0.01D);
                TickSpeed.tickrate(tickrate);
            }
            CarpetClientMessageHandler.sendTickRateChanges();
            run(sender, this, String.format("tick rate is %.1f", TickSpeed.tickrate));
            return;
        }
        else if ("warp".equalsIgnoreCase(args[0]))
        {
            long advance = args.length >= 2 ? parseClampedLong(args[1], 0, Long.MAX_VALUE) : TickSpeed.time_bias > 0 ? 0 : Long.MAX_VALUE;
            PlayerEntity player = null;
            if (sender instanceof PlayerEntity)
            {
                player = (PlayerEntity)sender;
            }

            String s = null;
            CommandSource icommandsender = null;
            if (args.length > 3)
            {
                s = method_10706(args, 2);
                icommandsender = sender;
            }

            String message = TickSpeed.tickrate_advance(player, advance, s, icommandsender);
            if (!message.isEmpty())
            {
                run(sender, this, message);
            }
            return;
        }
        else if ("freeze".equalsIgnoreCase(args[0]))
        {
            TickSpeed.is_paused = !TickSpeed.is_paused;
            if (TickSpeed.is_paused)
            {
                run(sender, this, "Game is paused");
            }
            else
            {
                run(sender, this, "Game runs normally");
            }
            return;
        }
        else if ("step".equalsIgnoreCase(args[0]))
        {
            int advance = 1;
            if (args.length > 1)
            {
                advance = parseClampedInt(args[1], 1, 72000);
            }
            TickSpeed.add_ticks_to_run_in_pause(advance);
            return;
        }
        else if ("superHot".equalsIgnoreCase(args[0]))
        {
            if (args.length > 1)
            {
                if ("stop".equalsIgnoreCase(args[1]) && !TickSpeed.is_superHot)
                {
                    return;
                }
                if ("start".equalsIgnoreCase(args[1]) && TickSpeed.is_superHot)
                {
                    return;
                }
            }
            TickSpeed.is_superHot = !TickSpeed.is_superHot;
            if (TickSpeed.is_superHot)
            {
                run(sender, this, "Superhot enabled");
            }
            else
            {
                run(sender, this, "Superhot disabled");
            }
            return;
        }
        else if ("health".equalsIgnoreCase(args[0]))
        {
            int step = 100;
            if (args.length > 1)
            {
                step = parseClampedInt(args[1], 20, 72000);
            }
            CarpetProfiler.prepare_tick_report(step);
            return;
        }
        else if ("entities".equalsIgnoreCase(args[0]))
        {
            int step = 100;
            if (args.length > 1)
            {
                step = parseClampedInt(args[1], 20, 72000);
            }
            CarpetProfiler.prepare_entity_report(step);
            return;
        }
        throw new IncorrectUsageException(getUsageTranslationKey(sender));
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandTick)
        {
            return Collections.emptyList();
        }
        if (args.length == 1)
        {
            return method_2894(args, "rate","warp", "freeze", "step", "superHot", "health", "entities");
        }
        if (args.length == 2 && "superHot".equalsIgnoreCase(args[0]))
        {
            return method_2894(args, "stop","start");
        }
        if (args.length == 2 && "rate".equalsIgnoreCase(args[0]))
        {
            return method_2894(args, "20");
        }
        if (args.length == 2 && "warp".equalsIgnoreCase(args[0]))
        {
            return method_2894(args, "1000","24000","72000");
        }
        if (args.length == 2 && "health".equalsIgnoreCase(args[0]))
        {
            return method_2894(args, "100","200","1000");
        }
        if (args.length == 2 && "entities".equalsIgnoreCase(args[0]))
        {
            return method_2894(args, "100","200","1000");
        }
        return Collections.emptyList();
    }
}
