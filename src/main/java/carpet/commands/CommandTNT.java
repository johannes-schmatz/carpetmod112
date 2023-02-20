package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.OptimizedTNT;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
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
    public String getCommandName() {
        return "tnt";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender) {
        return USAGE;
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        int x;
        int y;
        int z;
         if(args[0].equals("setSeed")){
             try {
                 rand.setSeed(Long.parseLong(args[1]) ^ 0x5DEECE66DL);
                 run(sender, this, "RNG TNT angle seed set to " + args[1] + (CarpetSettings.TNTAdjustableRandomAngle ? "" : " Enable TNTAdjustableRandomAngle" +
                         " rule or seed wont work."));
             } catch (Exception e) {
             }
        } else if(args[0].equals("clear")){
             tntScanPos = null;
             run(sender, this, "TNT scanning block cleared.");
         } else if (args.length > 2) {
            if (args.length > 3) throw new IncorrectUsageException(USAGE);
            x = (int) Math.round(getCoordinate(sender.getBlockPos().getX(), args[0], false).getAmount());
            y = (int) Math.round(getCoordinate(sender.getBlockPos().getY(), args[1], false).getAmount());
            z = (int) Math.round(getCoordinate(sender.getBlockPos().getZ(), args[2], false).getAmount());
            tntScanPos = new BlockPos(x, y, z);
            OptimizedTNT.setBlastChanceLocation(tntScanPos);
            run(sender, this,
                    String.format("TNT scanning block at: %d %d %d", x, y, z));
        } else {
            throw new IncorrectUsageException(USAGE);
        }
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1)
        {
            return method_2894(args, String.valueOf(targetPos.getX()), "clear");
        }
        else if (args.length == 2)
        {
            return method_2894(args, String.valueOf(targetPos.getY()));
        }
        else if (args.length == 3)
        {
            return method_2894(args, String.valueOf(targetPos.getZ()));
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
