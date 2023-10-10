package carpet.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import carpet.logging.LoggerOptions;
import org.apache.commons.lang3.ArrayUtils;

import carpet.CarpetSettings;
import carpet.logging.LogHandler;
import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;

import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandLog extends CommandCarpetBase {
    @Override
	public String getName() {
		return "log";
	}

	@Override
	public String getUsage(CommandSource sender) {
        return "/log (interactive menu) OR "
			+ "/log <logName> [?option] [player] [handler ...] OR "
			+ "/log <logName> clear [player] OR "
			+ "/log defaults (interactive menu) OR "
			+ "/log setDefault <logName> [?option] [handler ...] OR "
			+ "/log removeDefault <logName>";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandLog", sender)) return;

		if (args.length == 0) {
			interactive(sender);
		} else {
			switch (args[0]) {
				case "reset":
					reset(server, sender, args);
					break;
				case "defaults":
					defaults(sender);
					break;
				case "setDefault":
					setDefault(server, sender, args);
					break;
				case "removeDefault":
					removeDefault(server, sender, args);
					break;
				default:
					defaultCase(server, sender, args);
					break;
			}
		}
	}

	private void interactive(CommandSource sender) {
		PlayerEntity player = null;
		if (sender instanceof PlayerEntity) {
			player = (PlayerEntity) sender;
		}
		if (player == null) {
			return;
		}
		Map<String, LoggerOptions> subs = LoggerRegistry.getPlayerSubscriptions(player.getName());
		if (subs == null) {
			subs = new HashMap<>();
		}
		List<String> loggerNames = LoggerRegistry.getLoggerNames(classType());
		Collections.sort(loggerNames);
		Messenger.m(player, "w _____________________");
		Messenger.m(player, "w Available logging options:");
		for (String loggerName : loggerNames) {
			List<Object> comp = new ArrayList<>();
			comp.add("w  - " + loggerName + ": ");
			String color = subs.containsKey(loggerName) ? "w" : "g";

			Logger logger = LoggerRegistry.getLogger(loggerName);

			List<String> options = logger.getOptions();
			if (options.isEmpty()) {
				if (subs.containsKey(loggerName)) {
					comp.add("l Subscribed ");
				} else {
					comp.add(color + " [Subscribe] ");
					comp.add("^w toggle subscription to " + loggerName);
					comp.add("!/log " + loggerName);
				}
			} else {
				for (String option : options) {
					if (subs.containsKey(loggerName) && subs.get(loggerName).option.equalsIgnoreCase(option)) {
						comp.add("l [" + option + "] ");
					} else {
						comp.add(color + " [" + option + "] ");
						comp.add("^w toggle subscription to " + loggerName + " " + option);
						comp.add("!/log " + loggerName + " " + option);
					}

				}
			}
			if (subs.containsKey(loggerName)) {
				comp.add("nb [X]");
				comp.add("^w Click to toggle subscription");
				comp.add("!/log " + loggerName);
			}
			Messenger.mL(player, comp);
		}
	}

	private void reset(MinecraftServer server, CommandSource sender, String[] args) throws IncorrectUsageException {
		// toggle to default
		PlayerEntity player = null;
		if (sender instanceof PlayerEntity) {
			player = (PlayerEntity) sender;
		}
		if (args.length > 1) {
			player = server.getPlayerManager().get(args[1]);
		}

		if (player == null) {
			throw new IncorrectUsageException("No player specified");
		}

		LoggerRegistry.resetSubscriptions(server, player.getName());
		sendSuccess(sender, this, "Unsubscribed from all logs and restored default subscriptions");
	}

	private void defaults(CommandSource sender) {
		PlayerEntity player;
		if (sender instanceof PlayerEntity) {
			player = (PlayerEntity) sender;
		} else {
			return;
		}

		Map<String, LoggerOptions> subs = LoggerRegistry.getDefaultSubscriptions();

		List<String> loggerNames = LoggerRegistry.getLoggerNames(classType());
		Collections.sort(loggerNames);

		Messenger.m(player, "w _____________________");
		Messenger.m(player, "w Available logging options:");
		for (String loggerName : loggerNames) {
			List<Object> comp = new ArrayList<>();
			String color = subs.containsKey(loggerName) ? "w" : "g";
			comp.add("w  - " + loggerName + ": ");
			Logger logger = LoggerRegistry.getLogger(loggerName);

			List<String> options = logger.getOptions();
			if (options.isEmpty()) {
				if (subs.containsKey(loggerName)) {
					comp.add("l Subscribed ");
				} else {
					comp.add(color + " [Subscribe] ");
					comp.add("^w set default subscription to " + loggerName);
					comp.add("!/log setDefault " + loggerName);
				}
			} else {
				for (String option : options) {
					if (subs.containsKey(loggerName) && subs.get(loggerName).option.equalsIgnoreCase(option)) {
						comp.add("l [" + option + "] ");
					} else {
						comp.add(color + " [" + option + "] ");
						comp.add("^w set default subscription to " + loggerName + " " + option);
						comp.add("!/log setDefault " + loggerName + " " + option);
					}
				}
			}
			if (subs.containsKey(loggerName)) {
				comp.add("nb [X]");
				comp.add("^w Click to remove default subscription");
				comp.add("!/log removeDefault " + loggerName);
			}
			Messenger.mL(player, comp);
		}
	}
	private static void setDefault(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		PlayerEntity player;
		if (sender instanceof PlayerEntity) {
			player = (PlayerEntity) sender;
		} else {
			player = null;
		}


		if (args.length >= 2) {
			Logger logger = LoggerRegistry.getLogger(args[1]);
			if (logger != null) {

				String option;
				if (args.length >= 3) {
					option = logger.getAcceptedOption(args[2]);
				} else {
					option = logger.getDefault();
				}

				LogHandler handler = null;
				if (args.length >= 4) {
					handler = LogHandler.createHandler(args[3], ArrayUtils.subarray(args, 4, args.length));
				}

				LoggerRegistry.setDefault(server, args[1], option, handler);

				Messenger.m(player, "gi Added " + logger.getLogName() + " to default subscriptions.");
			} else {
				throw new IncorrectUsageException("No logger named " + args[1] + ".");
			}
		} else {
			throw new IncorrectUsageException("No logger specified.");
		}
	}
	private static void removeDefault(MinecraftServer server, CommandSource sender, String[] args) throws IncorrectUsageException {
		PlayerEntity player;
		if (sender instanceof PlayerEntity) {
			player = (PlayerEntity) sender;
		} else {
			player = null;
		}

		if (args.length > 1) {
			Logger logger = LoggerRegistry.getLogger(args[1]);
			if (logger != null) {
				LoggerRegistry.removeDefault(server, args[1]);
				Messenger.m(player, "gi Removed " + logger.getLogName() + " from default subscriptions.");
			} else {
				throw new IncorrectUsageException("No logger named " + args[1] + ".");
			}
		} else {
			throw new IncorrectUsageException("No logger specified.");
		}
	}

	private static void defaultCase(MinecraftServer server, CommandSource sender, String[] args) throws IncorrectUsageException {
		Logger logger = LoggerRegistry.getLogger(args[0]);
		if (logger != null) {
			String option = null;
			if (args.length >= 2) {
				option = logger.getAcceptedOption(args[1]);
			}

			PlayerEntity player;
			if (sender instanceof PlayerEntity) {
				player = (PlayerEntity) sender;
			} else {
				player = null;
			}

			if (args.length >= 3) {
				player = server.getPlayerManager().get(args[2]);
			}
			if (player == null) {
				throw new IncorrectUsageException("No player specified");
			}

			LogHandler handler = null;
			if (args.length >= 4) {
				handler = LogHandler.createHandler(args[3], ArrayUtils.subarray(args, 4, args.length));
			}

			boolean subscribed = true;
			if (args.length >= 2 && "clear".equalsIgnoreCase(args[1])) {
				LoggerRegistry.unsubscribePlayer(server, player.getName(), logger.getLogName());
				subscribed = false;
			} else if (option == null) {
				subscribed = LoggerRegistry.togglePlayerSubscription(server, player.getName(), logger.getLogName(), handler);
			} else {
				LoggerRegistry.subscribePlayer(server, player.getName(), logger.getLogName(), option, handler);
			}

			if (subscribed) {
				Messenger.m(player, "gi Subscribed to " + logger.getLogName() + ".");
			} else {
				Messenger.m(player, "gi Unsubscribed from " + logger.getLogName() + ".");
			}
		} else {
			throw new IncorrectUsageException("No logger named " + args[0] + ".");
		}
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos) {
		if (!CarpetSettings.commandLog) {
			return Collections.emptyList();
		}
		if (args.length == 1) {
			Set<String> options = new HashSet<>(LoggerRegistry.getLoggerNames(classType()));
			options.add("clear");
			options.add("defaults");
			options.add("setDefault");
			options.add("removeDefault");
			return suggestMatching(args, options);
		} else if (args.length == 2) {
			if ("clear".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, server.getPlayerNames());
			} else if ("setDefault".equalsIgnoreCase(args[0]) || "removeDefault".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, LoggerRegistry.getLoggerNames(classType()));
			}

			Logger logger = LoggerRegistry.getLogger(args[0]);
			if (logger != null) {
				List<String> options = new ArrayList<>();
				options.add("clear");

				List<String> opts = logger.getOptions();
				if (opts.isEmpty()) {
					options.addAll(opts);
				} else {
					options.add("on");
				}

				return suggestMatching(args, options);
			}
		} else if (args.length == 3) {
			if ("setDefault".equalsIgnoreCase(args[0])) {
				Logger logger = LoggerRegistry.getLogger(args[1]);
				if (logger != null) {
					return suggestMatching(args, logger.getOptions());
				}
			}

			return suggestMatching(args, server.getPlayerNames());
		} else if (args.length == 4) {
			return suggestMatching(args, LogHandler.getHandlerNames());
		}

		return Collections.emptyList();
	}

	private int classType() {
		if (this instanceof CommandDebuglogger) return 2;
		if (this instanceof CommandSubscribe) return 3;
		return 1;
	}
}
