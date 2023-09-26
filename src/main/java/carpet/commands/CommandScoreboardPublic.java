package carpet.commands;

import carpet.CarpetSettings;

import net.minecraft.scoreboard.criterion.ScoreboardCriterion;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CommandScoreboardPublic extends ScoreboardCommand {
    @Override
    public String getName() {
        return "scoreboardPublic";
    }

    @Override
    public String getUsage(CommandSource sender) {
        return "/scoreboardPublic objectives <list|add|remove|setdisplay> [objective]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canUse(MinecraftServer server, CommandSource sender) {
        return true;
    }

    @Override
    public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if (!CarpetSettings.commandPublicScoreboard) return;

        if (args.length < 1) {
            throw new IncorrectUsageException("commands.scoreboard.usage");
        } else {
            if ("objectives".equalsIgnoreCase(args[0])) {
                if (args.length == 1) {
                    throw new IncorrectUsageException("/scoreboardPublic objectives <list|add|remove|setdisplay> [objective]");
                }

                if ("list".equalsIgnoreCase(args[1])) {
                    this.listObjectives(sender, server);
                } else if ("add".equalsIgnoreCase(args[1])) {
                    if (args.length < 4) {
                        throw new IncorrectUsageException("/scoreboardPublic objectives add [objective]");
                    }

                    this.addObjective(sender, args, 2, server);
                } else if ("remove".equalsIgnoreCase(args[1])) {
                    if (args.length != 3) {
                        throw new IncorrectUsageException("/scoreboardPublic objectives remove [objective]");
                    }

                    this.removeObjective(sender, args[2], server);
                } else {
                    if (!"setdisplay".equalsIgnoreCase(args[1])) {
                        throw new IncorrectUsageException("/scoreboardPublic objectives setdisplay [objective]");
                    }

                    if (args.length != 3 && args.length != 4) {
                        throw new IncorrectUsageException("/scoreboardPublic objectives setdisplay <slot> [objective]");
                    }

                    this.setDisplayObjective(sender, args, 2, server);
                }
            }
        }
    }

    @Override
    public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
        if (!CarpetSettings.commandPublicScoreboard) return Collections.emptyList();

        if (args.length == 1) {
            return suggestMatching(args, "objectives");
        } else {
            if ("objectives".equalsIgnoreCase(args[0])) {
                if (args.length == 2) {
                    return suggestMatching(args, "list", "add", "remove", "setdisplay");
                }

                if ("add".equalsIgnoreCase(args[1])) {
                    if (args.length == 4) {
                        Set<String> set = ScoreboardCriterion.BY_NAME.keySet();
                        return suggestMatching(args, set);
                    }
                } else if ("remove".equalsIgnoreCase(args[1])) {
                    if (args.length == 3) {
                        return suggestMatching(args, this.getObjectives(false, server));
                    }
                } else if ("setdisplay".equalsIgnoreCase(args[1])) {
                    if (args.length == 3) {
                        return suggestMatching(args, Scoreboard.getDisplayLocations());
                    }

                    if (args.length == 4) {
                        return suggestMatching(args, this.getObjectives(false, server));
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}
