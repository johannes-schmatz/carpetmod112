package carpet.commands;

import carpet.mixin.accessors.WorldAccessor;
import carpet.utils.extensions.RepopulatableChunk;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandRepopulate extends CommandCarpetBase {

    @Override
	public String getName() {
		return "repopulate";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "/repopulate <chunk x> <chunk z>";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandRepopulate", sender)) return;

		if (args.length != 2) {
			throw new IncorrectUsageException(getUsage(sender));
		}
		int chunkX = parseInt(args[0]);
		int chunkZ = parseInt(args[1]);
		boolean isloaded = ((WorldAccessor) sender.getSourceWorld()).invokeIsChunkLoaded(chunkX, chunkZ, false);
		WorldChunk chunk = sender.getSourceWorld().getChunkAt(chunkX, chunkZ);
		((RepopulatableChunk) chunk).setUnpopulated();
		if (isloaded) {
			sender.sendMessage(new LiteralText("Marked currently loaded chunk " + chunkX + " " + chunkZ + " for repopulation!"));
		} else {
			sender.sendMessage(new LiteralText("Marked chunk " + chunkX + " " + chunkZ + " for repopulation!"));
		}
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
		int chunkX = sender.getSourceBlockPos().getX() >> 4;
		int chunkZ = sender.getSourceBlockPos().getZ() >> 4;

		if (args.length == 1) {
			return suggestMatching(args, Integer.toString(chunkX));
		} else if (args.length == 2) {
			return suggestMatching(args, Integer.toString(chunkZ));
		} else {
			return Collections.emptyList();
		}
	}

}
