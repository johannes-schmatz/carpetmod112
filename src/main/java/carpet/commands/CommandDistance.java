package carpet.commands;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import carpet.CarpetSettings;
import carpet.utils.DistanceCalculator;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandDistance extends CommandCarpetBase
{
    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return "Usage: distance <X1> <Y1> <Z1> <X2> <Y2> <Z2>";
    }

    @Override
    public String getCommandName()
    {
        return "distance";
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandDistance", sender)) return;
        if (args.length != 6)
        {
            throw new IncorrectUsageException(getUsageTranslationKey(sender), new Object[0]);
        }
        BlockPos blockpos = getBlockPos(sender, args, 0, false);
        BlockPos blockpos2 = getBlockPos(sender, args, 3, false);
        msg(sender, DistanceCalculator.print_distance_two_points(blockpos, blockpos2));

    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandDistance)
        {
            return Collections.<String>emptyList();
        }
        if (args.length > 0 && args.length <= 3)
        {
            return method_10707(args, 0, pos);
        }
        if (args.length > 3 && args.length <= 6)
        {
            return method_10707(args, 3, pos);
        }
        return Collections.<String>emptyList();
    }
}
