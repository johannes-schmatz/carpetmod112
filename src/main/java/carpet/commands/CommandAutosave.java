package carpet.commands;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import carpet.CarpetSettings;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandAutosave extends CommandCarpetBase {

	@Override
	public String getName() {
		return "autosave";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "Usage: autosave info | autosave detect <range-start> <range-end> <quiet t| run <command>>";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandAutosave", sender)) return;
		
		if(args.length < 1)
		{
			throw new IncorrectUsageException(getUsage(sender));
		}
		
		int gametick = server.getTicks();
		
		int afterAutosave = gametick%900;
		
		if("info".equalsIgnoreCase(args[0])) {
			int previous = afterAutosave;
			int interval = gametick/900;
			if(gametick != 0 && previous == 0) {
				previous = 900;
				interval--;
			}
			int next = 900 - previous;
			int beforeAutosave = 900-previous;
			sendSuccess(sender, this, String.format("Autosave (interval %d) %d gameticks ago - in %d ticks", interval, previous, next));
		}
		else if("detect".equalsIgnoreCase(args[0])) {
			if(args.length < 3) {
				throw new IncorrectUsageException(getUsage(sender));
			}
			
			int start = parseInt(args[1]);
			int end = parseInt(args[2]);
			boolean quiet = false;
			String run = null;

			if(args.length == 4) {
				if("quiet".equalsIgnoreCase(args[3])) {
					quiet = true;
				}
				else {
					throw new IncorrectUsageException(getUsage(sender));
				}
			}
			else if(args.length > 4) {
				if("run".equals(args[3])) {
					run = parseString(args, 4);
					quiet = true;
				}
				else {
					throw new IncorrectUsageException(getUsage(sender));
				}
			}
			
			start = (start % 900 + 900) % 900;
			end = (end % 900 + 900) % 900;
			boolean innerInterval = (start <= end);
			boolean pass = innerInterval ?
					(start <= afterAutosave) && (afterAutosave <= end):
					(start <= afterAutosave) || (afterAutosave <= end);
			
			if(pass) {
				if(!quiet) {
					sendSuccess(sender, this, String.format("gametick %d in interval %d %d",afterAutosave, start, end));
				}
				if(run != null) {
					server.getCommandHandler().run(sender, run);
				}
			}
			else {
				throw new CommandException(String.format("gametick %d not in interval %d %d",afterAutosave, start, end));
			}
		}
		else {
			throw new IncorrectUsageException(getUsage(sender));
		}
	}

	@Override
    public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandAutosave)
        {
            return Collections.<String>emptyList();
        }
        if(args.length == 1) {
        	return suggestMatching(args, "info", "detect");
        }
        if(args.length == 4) {
        	return suggestMatching(args, "run", "quiet");
        }
		return Collections.<String>emptyList();
    }
}
