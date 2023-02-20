package carpet.commands;

import carpet.mixin.accessors.WorldAccessor;
import carpet.utils.extensions.RepopulatableChunk;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandRepopulate extends CommandCarpetBase {
    public static final String USAGE = "/repopulate <chunk x> <chunk z>";

    @Override
    public String getCommandName()
    {
        return "repopulate";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return USAGE;
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandRepopulate", sender))
            return;

        if (args.length != 2) {
            throw new IncorrectUsageException(USAGE);
        }
        int chunkX = parseInt(args[0]);
        int chunkZ = parseInt(args[1]);
        boolean isloaded = ((WorldAccessor) sender.getWorld()).invokeIsChunkLoaded(chunkX, chunkZ, false);
        Chunk chunk = sender.getWorld().getChunk(chunkX, chunkZ);
        ((RepopulatableChunk) chunk).setUnpopulated();
        if (isloaded){
            sender.sendMessage(new LiteralText("Marked currently loaded chunk " + chunkX + " " + chunkZ + " for repopulation!"));
        } else {
            sender.sendMessage(new LiteralText("Marked chunk " + chunkX + " " + chunkZ + " for repopulation!"));
        }
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
        int chunkX = sender.getBlockPos().getX() >> 4;
        int chunkZ = sender.getBlockPos().getZ() >> 4;

        if (args.length == 1) {
            return method_2894(args, Integer.toString(chunkX));
        } else if (args.length == 2) {
            return method_2894(args, Integer.toString(chunkZ));
        } else {
            return Collections.emptyList();
        }
    }

}
