package carpet.commands;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import carpet.CarpetSettings;
import carpet.utils.ChunkLoading;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CommandUnload extends CommandCarpetBase
{
    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return "Usage: unload <brief|verbose|order> <X1> <Y1> <Z1> [<x2> <y2> <z2>]";
    }

    @Override
    public String getCommandName()
    {
        return "unload";
    }


    public void print_multi_message(List<String> messages, CommandSource sender)
    {
        for (String line: messages)
        {
            run(sender, this, line);
        }
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandUnload", sender)) return;
        if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 4 && args.length != 7)
        {
            throw new IncorrectUsageException(getUsageTranslationKey(sender));
        }
        BlockPos pos = sender.getBlockPos();
        BlockPos pos2 = null;
        boolean verbose = false;
        boolean order = false;
        boolean custom_dim = false;
        int custom_dim_id = 0;
        if (args.length >0)
        {
            verbose = "verbose".equalsIgnoreCase(args[0]);
        }
        if (args.length >0)
        {
            order = "order".equalsIgnoreCase(args[0]);
        }

        if (args.length >= 4)
        {
            pos = getBlockPos(sender, args, 1, false);
        }
        if (args.length >= 7)
        {
            pos2 = getBlockPos(sender, args, 4, false);
        }
        if (args.length > 0)
        {
            if ("overworld".equalsIgnoreCase(args[0]))
            {
                custom_dim = true;
                custom_dim_id = 0;
            }
            if ("nether".equalsIgnoreCase(args[0]))
            {
                custom_dim = true;
                custom_dim_id = -1;
            }
            if ("end".equalsIgnoreCase(args[0]))
            {
                custom_dim = true;
                custom_dim_id = 1;
            }
            if (custom_dim && args.length > 1)
            {
                if ("verbose".equalsIgnoreCase(args[1]))
                {
                    verbose = true;
                }
            }
        }

        if (order)
        {
            List<String> orders = ChunkLoading.check_unload_order((ServerWorld)sender.getWorld(), pos, pos2);
            print_multi_message(orders, sender);
            return;
        }
        ServerWorld world = (ServerWorld) (custom_dim?server.getWorld(custom_dim_id):sender.getWorld() );
        run(sender, this, "Chunk unloading report for "+world.dimension.getDimensionType());
        List<String> report = ChunkLoading.test_save_chunks(world, pos, verbose);
        print_multi_message(report, sender);
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandUnload)
        {
            return Collections.emptyList();
        }
        if (args.length == 1)
        {
            return method_2894(args, "verbose", "brief", "order", "nether", "overworld", "end");
        }
        if (args.length == 2 && ( "nether".equalsIgnoreCase(args[0]) || "overworld".equalsIgnoreCase(args[0]) || "end".equalsIgnoreCase(args[0]) ))
        {
            return method_2894(args, "verbose");
        }
        if (args.length > 1 && args.length <= 4)
        {
            return method_10707(args, 1, pos);
        }
        if (args.length > 4 && args.length <= 7)
        {
            return method_10707(args, 4, pos);
        }
        return Collections.emptyList();
    }
}
