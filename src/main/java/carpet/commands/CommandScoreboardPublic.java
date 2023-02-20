package carpet.commands;

import carpet.CarpetSettings;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CommandScoreboardPublic extends ScoreboardCommand {
    @Override
    public String getCommandName() {
        return "scoreboardPublic";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender) {
        return "/scoreboardPublic objectives <list|add|remove|setdisplay> [objective]";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public boolean method_3278(MinecraftServer server, CommandSource sender) {
        return true;
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if (!CarpetSettings.commandPublicScoreboard) return;

        if (args.length < 1) {
            throw new IncorrectUsageException("commands.scoreboard.usage");
        } else {
            if ("objectives".equalsIgnoreCase(args[0])) {
                if (args.length == 1) {
                    throw new IncorrectUsageException("/scoreboardPublic objectives <list|add|remove|setdisplay> [objective]");
                }

                if ("list".equalsIgnoreCase(args[1])) {
                    this.method_5310(sender, server);
                } else if ("add".equalsIgnoreCase(args[1])) {
                    if (args.length < 4) {
                        throw new IncorrectUsageException("/scoreboardPublic objectives add [objective]");
                    }

                    this.method_5307(sender, args, 2, server);
                } else if ("remove".equalsIgnoreCase(args[1])) {
                    if (args.length != 3) {
                        throw new IncorrectUsageException("/scoreboardPublic objectives remove [objective]");
                    }

                    this.method_5312(sender, args[2], server);
                } else {
                    if (!"setdisplay".equalsIgnoreCase(args[1])) {
                        throw new IncorrectUsageException("/scoreboardPublic objectives setdisplay [objective]");
                    }

                    if (args.length != 3 && args.length != 4) {
                        throw new IncorrectUsageException("/scoreboardPublic objectives setdisplay <slot> [objective]");
                    }

                    this.method_5318(sender, args, 2, server);
                }
            }
        }
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
        if (!CarpetSettings.commandPublicScoreboard) return Collections.emptyList();

        if (args.length == 1) {
            return method_2894(args, "objectives");
        } else {
            if ("objectives".equalsIgnoreCase(args[0])) {
                if (args.length == 2) {
                    return method_2894(args, "list", "add", "remove", "setdisplay");
                }

                if ("add".equalsIgnoreCase(args[1])) {
                    if (args.length == 4) {
                        Set<String> set = ScoreboardCriterion.OBJECTIVES.keySet();
                        return method_10708(args, set);
                    }
                } else if ("remove".equalsIgnoreCase(args[1])) {
                    if (args.length == 3) {
                        return method_10708(args, this.method_12135(false, server));
                    }
                } else if ("setdisplay".equalsIgnoreCase(args[1])) {
                    if (args.length == 3) {
                        return method_2894(args, Scoreboard.getDisplaySlotNames());
                    }

                    if (args.length == 4) {
                        return method_10708(args, this.method_12135(false, server));
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}
