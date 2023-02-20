package carpet.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandLoadChunk extends CommandCarpetBase {
    @Override
    public String getUsageTranslationKey(CommandSource sender) {
        return "Usage: loadchunk <X> <Z>";
    }

    @Override
    public String getCommandName() {
        return "loadchunk";
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if (!command_enabled("commandLoadChunk", sender)) return;

        if (args.length != 2)
        {
            throw new IncorrectUsageException(getUsageTranslationKey(sender));
        }
        int chunkX = parseInt(args[0]);
        int chunkZ = parseInt(args[1]);
        World world = sender.getWorld();
        world.getChunk(chunkX, chunkZ);
        sender.sendMessage(new LiteralText("Chunk" + chunkX + ", " + chunkZ + " loaded"));
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
