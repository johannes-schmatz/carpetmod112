package carpet.commands;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import carpet.utils.Messenger;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;

public class CommandCounter extends CommandCarpetBase {
	@Override
	public String getUsage(CommandSource sender) {
		return "Usage: counter <color> <reset/realtime>";
	}

	@Override
	public String getName() {
		return "counter";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (CarpetSettings.hopperCounters == CarpetSettings.HopperCounters.off && !CarpetSettings.cactusCounter) {
			msg(sender, Messenger.m(null, "Need cactusCounter or hopperCounters to be enabled to use this command."));
			return;
		}
		if (args.length == 0) {
			msg(sender, HopperCounter.formatAll(server, false));
			return;
		}
		switch (args[0].toLowerCase(Locale.ROOT)) {
			case "realtime":
				msg(sender, HopperCounter.formatAll(server, true));
				return;
			case "reset":
				HopperCounter.resetAll(server);
				sendSuccess(sender, this, "All counters restarted.");
				return;
		}
		HopperCounter counter = HopperCounter.getCounter(args[0]);
		if (counter == null) throw new IncorrectUsageException("Invalid color");
		if (args.length == 1) {
			msg(sender, counter.format(server, false, false));
			return;
		}
		switch (args[1].toLowerCase(Locale.ROOT)) {
			case "realtime":
				msg(sender, counter.format(server, true, false));
				return;
			case "reset":
				counter.reset(server);
				sendSuccess(sender, this, String.format("%s counters restarted.", args[0]));
				return;
		}
		throw new IncorrectUsageException(getUsage(sender));

	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
		if (CarpetSettings.hopperCounters == CarpetSettings.HopperCounters.off && !CarpetSettings.cactusCounter) {
			msg(sender, Messenger.m(null, "Need cactusCounter or hopperCounters to be enabled to use this command."));
			return Collections.emptyList();
		}
		if (args.length == 1) {
			List<String> lst = new ArrayList<>();
			lst.add("reset");
			for (DyeColor clr : DyeColor.values()) {
				lst.add(clr.name().toLowerCase(Locale.ROOT));
			}
			lst.add("cactus");
			lst.add("all");
			lst.add("realtime");
			String[] stockArr = new String[lst.size()];
			stockArr = lst.toArray(stockArr);
			return suggestMatching(args, stockArr);
		}
		if (args.length == 2) {
			return suggestMatching(args, "reset", "realtime");
		}
		return Collections.emptyList();
	}
}
