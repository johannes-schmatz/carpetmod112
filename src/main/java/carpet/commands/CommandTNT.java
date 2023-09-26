package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.OptimizedTNT;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CommandTNT extends CommandCarpetBase{
    public static Random rand = new Random();
    public static BlockPos tntScanPos = null;
    public static final String USAGE = "/tnt [x y z]/clear";

    @Override
    public String getName() {
        return "tnt";
    }

    @Override
    public String getUsage(CommandSource sender) {
        return USAGE;
    }

    @Override
    public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        int x;
        int y;
        int z;
         if(args[0].equals("setSeed")){
             try {
                 rand.setSeed(Long.parseLong(args[1]) ^ 0x5DEECE66DL);
                 sendSuccess(sender, this, "RNG TNT angle seed set to " + args[1] + (CarpetSettings.TNTAdjustableRandomAngle ? "" : " Enable TNTAdjustableRandomAngle" +
                         " rule or seed wont work."));
             } catch (Exception e) {
             }
        } else if(args[0].equals("clear")){
             tntScanPos = null;
             sendSuccess(sender, this, "TNT scanning block cleared.");
         } else if (args.length > 2) {
            if (args.length > 3) throw new IncorrectUsageException(USAGE);
            x = (int) Math.round(parseCoordinate(sender.getSourceBlockPos().getX(), args[0], false).getRelative());
            y = (int) Math.round(parseCoordinate(sender.getSourceBlockPos().getY(), args[1], false).getRelative());
            z = (int) Math.round(parseCoordinate(sender.getSourceBlockPos().getZ(), args[2], false).getRelative());
            tntScanPos = new BlockPos(x, y, z);
            OptimizedTNT.setBlastChanceLocation(tntScanPos);
             sendSuccess(sender, this,
                    String.format("TNT scanning block at: %d %d %d", x, y, z));
        } else {
            throw new IncorrectUsageException(USAGE);
        }
    }

    @Override
    public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1)
        {
            return suggestMatching(args, String.valueOf(targetPos.getX()), "clear");
        }
        else if (args.length == 2)
        {
            return suggestMatching(args, String.valueOf(targetPos.getY()));
        }
        else if (args.length == 3)
        {
            return suggestMatching(args, String.valueOf(targetPos.getZ()));
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
