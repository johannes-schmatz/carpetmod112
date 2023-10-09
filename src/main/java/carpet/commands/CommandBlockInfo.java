package carpet.commands;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import carpet.CarpetSettings;
import carpet.utils.BlockInfo;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandBlockInfo extends CommandCarpetBase {
	@Override
	public String getUsage(CommandSource sender) {
		return "Usage: blockinfo <X> <Y> <Z>";
	}

	@Override
	public String getName() {
		return "blockinfo";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandBlockInfo", sender)) return;

		if (args.length != 3) {
			throw new IncorrectUsageException(getUsage(sender));
		}
		BlockPos blockpos = parseBlockPos(sender, args, 0, false);
		World world = sender.getSourceWorld();
		msg(sender, BlockInfo.blockInfo(blockpos, world));
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
		if (!CarpetSettings.commandBlockInfo) {
			return Collections.emptyList();
		}
		if (args.length > 0 && args.length <= 3) {
			return suggestCoordinate(args, 0, pos);
		}
		return Collections.emptyList();
	}
}
