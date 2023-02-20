package narcolepticfrog.rsmm;

import carpet.CarpetServer;
import carpet.commands.CommandCarpetBase;
import narcolepticfrog.rsmm.server.RSMMServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import java.util.Collections;
import java.util.List;

public class MeterCommand extends CommandCarpetBase
{
    @Override
    public String getCommandName() {
        return "meter";
    }

    private static final String USAGE = "/meter name [idx] name OR /meter color [idx] <RRGGBB> OR /meter removeAll Or" +
            " /meter group <groupName> OR /meter listGroups";

    @Override
    public String getUsageTranslationKey(CommandSource sender) {
        return USAGE;
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("redstoneMultimeter", sender)) return;
        if (args.length < 1) {
            throw new IncorrectUsageException(USAGE);
        }

        if (!(sender instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity)sender;
        RSMMServer rsmmServer = CarpetServer.getInstance().rsmmServer;

        if (args[0].equals("name")) {
            if (rsmmServer.getNumMeters(player) <= 0) {
                throw new CommandException("There are no meters to rename!");
            }
            if (args.length == 2) {
                rsmmServer.renameLastMeter(player, args[1]);
                notifySender(sender, "Renamed meter to " + args[1]);
            } else if (args.length == 3) {
                int ix = parseClampedInt(args[1], 0, rsmmServer.getNumMeters(player) - 1);
                rsmmServer.renameMeter(player, ix, args[2]);
                notifySender(sender, "Renamed meter " + ix + " to " + args[2]);
            } else {
                throw new IncorrectUsageException(USAGE);
            }
        } else if (args[0].equals("color")) {
            if (rsmmServer.getNumMeters(player) <= 0) {
                throw new CommandException("There are no meters to recolor!");
            }
            if (args.length == 2) {
                rsmmServer.recolorLastMeter(player, ColorUtils.parseColor(args[1]));
                notifySender(sender, "Recolored meter to " + args[1]);
            } else if (args.length == 3) {
                int ix = parseClampedInt(args[1], 0, rsmmServer.getNumMeters(player) - 1);
                rsmmServer.recolorMeter(player, ix, ColorUtils.parseColor(args[2]));
                notifySender(sender, "Recolored meter " + ix + " to " + args[2]);
            } else {
                throw new IncorrectUsageException(USAGE);
            }
        } else if (args[0].equals("removeAll")) {

            if (args.length != 1) {
                throw new IncorrectUsageException(USAGE);
            }
            rsmmServer.removeAllMeters(player);
            notifySender(sender, "Removed all meters.");

        } else if (args[0].equals("group")) {

            if (args.length != 2) {
                throw new IncorrectUsageException(USAGE);
            }
            rsmmServer.changePlayerSubscription(player, args[1]);
            notifySender(sender, "Subscribed to meter group " + args[1]);

        } else if (args[0].equals("listGroups")) {

            StringBuilder response = new StringBuilder();
            response.append("Meter Groups:");
            for (String name : rsmmServer.getGroupNames()) {
                response.append("\n  " + name);
            }
            notifySender(sender, response.toString());

        } else {
            throw new IncorrectUsageException(USAGE);
        }
    }

    public void notifySender(CommandSource sender, String message) {
        LiteralText messageText = new LiteralText(message);
        messageText.getStyle().setFormatting(Formatting.GRAY);
        sender.sendMessage(messageText);
    }

    @Override
    public List<String> method_10738(MinecraftServer server,
                                          CommandSource sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return method_2894(args, "name", "color", "removeAll", "group", "listGroups");
        } else if (args.length == 2 && args[0].equals("group")) {
            return method_10708(args, CarpetServer.getInstance().rsmmServer.getGroupNames());
        } else {
            return Collections.<String>emptyList();
        }
    }
}
