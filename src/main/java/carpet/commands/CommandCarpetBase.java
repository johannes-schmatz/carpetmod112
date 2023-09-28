package carpet.commands;

import carpet.CarpetSettings;
import carpet.utils.Messenger;

import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.InvalidNumberException;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.List;

public abstract class CommandCarpetBase extends AbstractCommand {
	@Override
	public boolean canUse(MinecraftServer server, CommandSource sender) {
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	public void msg(CommandSource sender, List<Text> texts) {
		msg(sender, texts.toArray(new Text[0]));
	}

	public void msg(CommandSource sender, Text... texts) {
		if (sender instanceof PlayerEntity) {
			for (Text t : texts) sender.sendMessage(t);
		} else {
			for (Text t : texts) sendSuccess(sender, this, t.buildString());
		}
	}

	public boolean command_enabled(String command_name, CommandSource sender) {
		if (!CarpetSettings.get(command_name).equalsIgnoreCase("true")) {
			msg(sender, Messenger.m(null, "w Command is disabled in carpet settings"));
			if (!(sender instanceof PlayerEntity)) return false;
			if (CarpetSettings.locked) {
				Messenger.m((PlayerEntity) sender, "gi Ask your admin to enable it server config");
			} else {
				Messenger.m(
						(PlayerEntity) sender,
						"gi copy&pasta \"",
						"gib /carpet " + command_name + " true",
						"/carpet " + command_name + " true",
						"gi \"to enable it"
				);
			}
			return false;
		}
		return true;
	}

	protected int parseChunkPosition(String arg, int base) throws InvalidNumberException {
		return arg.equals("~") ? base >> 4 : parseInt(arg);
	}
}
