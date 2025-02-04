package carpet.commands;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import carpet.utils.Messenger;
import carpetmod.Build;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import carpet.CarpetSettings;

public class CommandCarpet extends CommandCarpetBase {
	@Override
	public String getUsage(CommandSource sender) {
		return "carpet <rule> <value>";
	}

	@Override
	public String getName() {
		return "carpet";
	}

	@Override
	public boolean canUse(MinecraftServer server, CommandSource sender) {
		return sender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	private static Text displayInteractiveSetting(String ruleName) {
		String def = CarpetSettings.getDefault(ruleName);
		String val = CarpetSettings.get(ruleName);
		List<String> args = new ArrayList<>();
		args.add("w - " + ruleName + " ");
		args.add("!/carpet " + ruleName);
		args.add("^y " + CarpetSettings.getDescription(ruleName));
		for (String option : CarpetSettings.getOptions(ruleName)) {
			String style = val.equalsIgnoreCase(def) ? "g" : (option.equalsIgnoreCase(def) ? "y" : "e");
			if (option.equalsIgnoreCase(def)) style = style + "b";
			else if (option.equalsIgnoreCase(val)) style = style + "u";
			args.add(style + " [" + option + "]");
			if (!option.equalsIgnoreCase(val)) {
				args.add("!/carpet " + ruleName + " " + option);
				args.add("^w switch to " + option);
			}
			args.add("w  ");
		}
		args.remove(args.size() - 1);
		return Messenger.m(null, args.toArray(new Object[0]));
	}

	public void list_settings(CommandSource sender, String title, String[] settings_list) {
		if (sender instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) sender;
			Messenger.m(player, String.format("wb %s:", title));
			Arrays.stream(settings_list).forEach(e -> Messenger.m(player, displayInteractiveSetting(e)));
		} else {
			sendSuccess(sender, this, String.format("%s:", title));
			Arrays.stream(settings_list).forEach(e -> sendSuccess(sender, this, String.format(" - %s", e)));
		}
	}

	/**
	 * Callback for when the command is executed
	 */
	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (CarpetSettings.locked) {
			list_settings(sender, Build.NAME + " admin locked with the following settings", CarpetSettings.findNonDefault());
			return;
		}
		String tag = null;
		try {
			if (args.length == 0) {
				list_settings(sender, "Current " + Build.NAME + " Settings", CarpetSettings.findNonDefault());
				sendSuccess(sender, this, Build.NAME + " version: " + Build.VERSION);
				if (sender instanceof PlayerEntity) {
					List<Object> tags = new ArrayList<>();
					tags.add("w Browse Categories:\n");
					for (CarpetSettings.RuleCategory ctgy : CarpetSettings.RuleCategory.values()) {
						String t = ctgy.name().toLowerCase(Locale.ENGLISH);
						tags.add("c [" + t + "]");
						tags.add("^g list all " + t + " settings");
						tags.add("!/carpet list " + t);
						tags.add("w  ");
					}
					tags.remove(tags.size() - 1);
					Messenger.m(sender, tags.toArray(new Object[0]));
				}
				return;
			}
			if (args.length == 1 && "list".equalsIgnoreCase(args[0])) {
				list_settings(sender, "All " + Build.NAME + " Settings", CarpetSettings.findAll(null));
				sendSuccess(sender, this, Build.NAME + " version: " + Build.VERSION);
				return;
			}
			if ("defaults".equalsIgnoreCase(args[0])) // empty tag
			{
				list_settings(sender, "Current " + Build.NAME + " Startup Settings from carpet.conf", CarpetSettings.findStartupOverrides(server));
				sendSuccess(sender, this, Build.NAME + " version: " + Build.VERSION);
				return;
			}
			if ("use".equalsIgnoreCase(args[0])) {// empty tag
				switch (args[1].toLowerCase()) {
					case "default":
						CarpetSettings.resetToUserDefaults(server);
						return;
					case "vanilla":
						CarpetSettings.resetToVanilla();
						return;
					case "survival":
						CarpetSettings.resetToSurvival();
						return;
					case "creative":
						CarpetSettings.resetToCreative();
						return;
					case "bugfixes":
						CarpetSettings.resetToBugFixes();
						return;
					default:
						throw new IncorrectUsageException(getUsage(sender));
				}
			}

			if (args.length >= 2 && "list".equalsIgnoreCase(args[0])) {
				tag = args[1].toLowerCase();
				args = Arrays.copyOfRange(args, 2, args.length);
				//no return; continue
			}
			if (args.length == 0) {
				list_settings(sender, String.format(Build.NAME + " Settings matching \"%s\"", tag), CarpetSettings.findAll(tag));
				return;
			}
			if ("setDefault".equalsIgnoreCase(args[0])) {
				if (args.length != 3) {
					throw new IncorrectUsageException("/carpet setDefault <setting> <value>");
				}
				boolean success = CarpetSettings.addOrSetPermarule(server, args[1], args[2]);
				if (success) {
					sendSuccess(sender, this, args[1] + " will default to: " + args[2]);
				} else {
					throw new IncorrectUsageException("Unknown setting");
				}
				return;
			}
			if ("removeDefault".equalsIgnoreCase(args[0])) {
				if (args.length != 2) {
					throw new IncorrectUsageException("/carpet removeDefault <setting>");
				}
				boolean success = CarpetSettings.removePermarule(server, args[1]);
				if (success) {
					sendSuccess(sender, this, args[1] + " will not be set upon restart");
				} else {
					throw new IncorrectUsageException("Unknown setting");
				}
				return;
			}
			if (!CarpetSettings.hasRule(args[0])) {
				throw new IncorrectUsageException("Unknown setting: " + args[0]);
			}
			if (args.length == 2) {
				boolean success = CarpetSettings.set(args[0], args[1]);
				if (!success) {
					throw new IncorrectUsageException(getUsage(sender));
				}
				msg(sender, Messenger.m(null,
						"w " + args[0] + ": " + CarpetSettings.get(args[0]) + ", ",
						"c [change permanently?]",
						"^w Click to keep the settings in carpet.conf to save across restarts",
						"?/carpet setDefault " + args[0] + " " + CarpetSettings.get(args[0])
				));
				return;
			}
			if (sender instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) sender;
				Messenger.s(player, "");
				Messenger.m(player, "wb " + args[0], "!/carpet " + args[0], "^g refresh");
				Messenger.s(player, CarpetSettings.getDescription(args[0]));

				Arrays.stream(CarpetSettings.getExtraInfo(args[0])).forEach(s -> Messenger.s(player, " " + s, "g"));

				List<Text> tags = new ArrayList<>();
				tags.add(Messenger.m(null, "w Tags: "));
				for (CarpetSettings.RuleCategory ctgy : CarpetSettings.RuleCategory.values()) {
					String t = ctgy.name().toLowerCase(Locale.ENGLISH);
					tags.add(Messenger.m(null, "c [" + t + "]", "^g list all " + t + " settings", "!/carpet list " + t));
					tags.add(Messenger.s(null, ", "));
				}
				tags.remove(tags.size() - 1);
				Messenger.m(player, tags.toArray(new Object[0]));
//
				Messenger.m(player, "w Current value: ", String.format("%s %s (%s value)",
						CarpetSettings.get(args[0]).equalsIgnoreCase("true") ? "lb" : "nb",
						CarpetSettings.get(args[0]),
						CarpetSettings.get(args[0]).equalsIgnoreCase(CarpetSettings.getDefault(args[0])) ? "default" : "modified"
				));
				List<Text> options = new ArrayList<>();
				options.add(Messenger.m(null, "w Options: ", "y [ "));
				for (String o : CarpetSettings.getOptions(args[0])) {
					options.add(Messenger.m(null, String.format("%s%s %s",
							(o.equals(CarpetSettings.getDefault(args[0]))) ? "u" : "",
							(o.equals(CarpetSettings.get(args[0]))) ? "bl" : "y",
							o
					), "^g switch to " + o, String.format("?/carpet %s %s", args[0], o)));
					options.add(Messenger.s(null, " "));
				}
				options.remove(options.size() - 1);
				options.add(Messenger.m(null, "y  ]"));
				Messenger.m(player, options.toArray(new Object[0]));
			} else {
				sendSuccess(sender, this, args[0] + " is set to: " + CarpetSettings.get(args[0]));
			}
			// Updates the carpet client with the changed rule.
		} catch (CommandException e) {
			if (e instanceof IncorrectUsageException) {
				throw e;
			}
			throw new IncorrectUsageException(getUsage(sender));
		}
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
		if (CarpetSettings.locked) {
			return Collections.emptyList();
		}
		if (args.length == 2 && "list".equalsIgnoreCase(args[0])) {
			return suggestMatching(args,
					Arrays.stream(CarpetSettings.RuleCategory.values())
							.map(v -> v.name().toLowerCase(Locale.ENGLISH))
							.toArray(String[]::new)
			);
		}
		if (args.length == 2 && "use".equalsIgnoreCase(args[0])) {
			return suggestMatching(args, "creative", "survival", "default", "vanilla", "bugfixes");
		}
		String tag = null;
		if (args.length > 2 && "list".equalsIgnoreCase(args[0])) {
			tag = args[1].toLowerCase();
			args = Arrays.copyOfRange(args, 2, args.length);
		}
		if (args.length == 1) {
			List<String> lst = new ArrayList<>();
			if ((tag != null) || (!args[0].isEmpty())) {
                Collections.addAll(lst, CarpetSettings.findAll(tag));
			}
			lst.add("setDefault");
			lst.add("removeDefault");
			if (tag == null) {
				lst.add("defaults");
				lst.add("use");
				lst.add("list");
			}
			return suggestMatching(args, lst);
		}
		if (args.length == 2) {
			if ("setDefault".equalsIgnoreCase(args[0]) || "removeDefault".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, CarpetSettings.findAll(tag));
			}
			return suggestMatching(args, CarpetSettings.getOptions(args[0]));
		}
		if (args.length == 3 && "setDefault".equalsIgnoreCase(args[0])) {
			return suggestMatching(args, CarpetSettings.getOptions(args[1]));
		}
		return Collections.emptyList();
	}
}