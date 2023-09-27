package carpet.commands;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import carpet.utils.CarpetProfiler;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientMessageHandler;
import carpet.helpers.TickSpeed;


public class CommandTick extends CommandCarpetBase {
	@Override
	public String getName() {
		return "tick";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "Usage: tick rate <tickrate in tps> | warp [time in ticks to skip]";
	}

	@Override
	public void run(final MinecraftServer server, final CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandTick", sender)) return;
		if (args.length == 0) {
			throw new IncorrectUsageException(getUsage(sender));
		}
		if ("rate".equalsIgnoreCase(args[0])) {
			if (args.length == 2) {
				float tickrate = (float) parseDouble(args[1], 0.01D);
				TickSpeed.tickrate(tickrate);
			}
			CarpetClientMessageHandler.sendTickRateChanges();
			sendSuccess(sender, this, String.format("tick rate is %.1f", TickSpeed.tickrate));
			return;
		} else if ("warp".equalsIgnoreCase(args[0])) {
			long advance = args.length >= 2 ? parseLong(args[1], 0, Long.MAX_VALUE) : TickSpeed.time_bias > 0 ? 0 : Long.MAX_VALUE;
			PlayerEntity player = null;
			if (sender instanceof PlayerEntity) {
				player = (PlayerEntity) sender;
			}

			String s = null;
			CommandSource icommandsender = null;
			if (args.length > 3) {
				s = parseString(args, 2);
				icommandsender = sender;
			}

			String message = TickSpeed.tickrate_advance(player, advance, s, icommandsender);
			if (!message.isEmpty()) {
				sendSuccess(sender, this, message);
			}
			return;
		} else if ("freeze".equalsIgnoreCase(args[0])) {
			TickSpeed.is_paused = !TickSpeed.is_paused;
			if (TickSpeed.is_paused) {
				sendSuccess(sender, this, "Game is paused");
			} else {
				sendSuccess(sender, this, "Game runs normally");
			}
			return;
		} else if ("step".equalsIgnoreCase(args[0])) {
			int advance = 1;
			if (args.length > 1) {
				advance = parseInt(args[1], 1, 72000);
			}
			TickSpeed.add_ticks_to_run_in_pause(advance);
			return;
		} else if ("superHot".equalsIgnoreCase(args[0])) {
			if (args.length > 1) {
				if ("stop".equalsIgnoreCase(args[1]) && !TickSpeed.is_superHot) {
					return;
				}
				if ("start".equalsIgnoreCase(args[1]) && TickSpeed.is_superHot) {
					return;
				}
			}
			TickSpeed.is_superHot = !TickSpeed.is_superHot;
			if (TickSpeed.is_superHot) {
				sendSuccess(sender, this, "Superhot enabled");
			} else {
				sendSuccess(sender, this, "Superhot disabled");
			}
			return;
		} else if ("health".equalsIgnoreCase(args[0])) {
			int step = 100;
			if (args.length > 1) {
				step = parseInt(args[1], 20, 72000);
			}
			CarpetProfiler.prepare_tick_report(step);
			return;
		} else if ("entities".equalsIgnoreCase(args[0])) {
			int step = 100;
			if (args.length > 1) {
				step = parseInt(args[1], 20, 72000);
			}
			CarpetProfiler.prepare_entity_report(step);
			return;
		}
		throw new IncorrectUsageException(getUsage(sender));
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
		if (!CarpetSettings.commandTick) {
			return Collections.emptyList();
		}
		if (args.length == 1) {
			return suggestMatching(args, "rate", "warp", "freeze", "step", "superHot", "health", "entities");
		}
		if (args.length == 2 && "superHot".equalsIgnoreCase(args[0])) {
			return suggestMatching(args, "stop", "start");
		}
		if (args.length == 2 && "rate".equalsIgnoreCase(args[0])) {
			return suggestMatching(args, "20");
		}
		if (args.length == 2 && "warp".equalsIgnoreCase(args[0])) {
			return suggestMatching(args, "1000", "24000", "72000");
		}
		if (args.length == 2 && "health".equalsIgnoreCase(args[0])) {
			return suggestMatching(args, "100", "200", "1000");
		}
		if (args.length == 2 && "entities".equalsIgnoreCase(args[0])) {
			return suggestMatching(args, "100", "200", "1000");
		}
		return Collections.emptyList();
	}
}
