package carpet.commands;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import carpet.CarpetSettings;
import carpet.utils.BlockInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandBlockInfo extends CommandCarpetBase
{
    /**
     * Gets the name of the command
     */

    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return "Usage: blockinfo <X> <Y> <Z>";
    }

    @Override
    public String getCommandName()
    {
        return "blockinfo";
    }

    /**
     * Callback for when the command is executed
     */
    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandBlockInfo", sender)) return;

        if (args.length != 3)
        {
            throw new IncorrectUsageException(getUsageTranslationKey(sender));
        }
        BlockPos blockpos = getBlockPos(sender, args, 0, false);
        World world = sender.getWorld();
        msg(sender, BlockInfo.blockInfo(blockpos, world));
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandBlockInfo)
        {
            return Collections.<String>emptyList();
        }
        if (args.length > 0 && args.length <= 3)
        {
            return method_10707(args, 0, pos);
        }
        return Collections.<String>emptyList();
    }
}
