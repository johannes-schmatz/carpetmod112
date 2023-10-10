package carpet.commands;

import org.jetbrains.annotations.Nullable;

import carpet.CarpetSettings;
import carpet.utils.CarpetProfiler;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class CommandProfile extends CommandCarpetBase {
	@Override
	public String getName() {
		return "profile";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "Usage: /profile <entities>";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandProfile", sender)) return;

		if (args.length > 0 && "entities".equalsIgnoreCase(args[0])) {
			CarpetProfiler.prepare_entity_report(100);
		} else {
			CarpetProfiler.prepare_tick_report(100);
		}

	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
		if (!CarpetSettings.commandProfile) {
			return Collections.emptyList();
		}
		if (args.length == 1) {
			return suggestMatching(args, "entities");
		}
		return Collections.emptyList();
	}
}
