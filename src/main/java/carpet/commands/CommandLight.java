package carpet.commands;

import net.minecraft.server.command.Command;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class CommandLight extends CommandCarpetBase {
	public static final String USAGE = "/light [x1 y1 z1] [x2 y2 z2] <sky/block> <value>";

	@Override
	public String getName() {
		return "light";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return USAGE;
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandLight", sender)) return;

		int x1, y1, z1, x2, y2, z2, lightLevel;
		String type;
		if (args.length > 7) {
			if (args.length > 8) throw new IncorrectUsageException(USAGE);
			x1 = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getX(), args[0], false).getCoordinate());
			y1 = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getY(), args[1], false).getCoordinate());
			z1 = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getZ(), args[2], false).getCoordinate());

			x2 = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getX(), args[3], false).getCoordinate());
			y2 = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getY(), args[4], false).getCoordinate());
			z2 = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getZ(), args[5], false).getCoordinate());

			type = args[6];
			try {
				lightLevel = Integer.parseInt(args[7]);
			} catch (Exception e) {
				throw new IncorrectUsageException(USAGE);
			}

			if (x1 > x2) {
				int temp = x1;
				x1 = x2;
				x2 = temp;
			}
			if (y1 > y2) {
				int temp = y1;
				y1 = y2;
				y2 = temp;
			}
			if (z1 > z2) {
				int temp = z1;
				z1 = z2;
				z2 = temp;
			}

			LightType t;

			if (type.equals("sky")) {
				t = LightType.SKY;
			} else if (type.equals("block")) {
				t = LightType.BLOCK;
			} else {
				throw new IncorrectUsageException(USAGE);
			}
			fillLightInArea(sender.getSourceWorld(), t, x1, y1, z1, x2, y2, z2, lightLevel);

			sendSuccess(sender,
					this,
					String.format("Changing light level of %s to %d from:[%d %d %d] to:[%d %d %d]", type, lightLevel, x1, y1, z1, x2, y2, z2)
			);
		} else {
			throw new IncorrectUsageException(USAGE);
		}
	}

	private void fillLightInArea(World world, LightType type, int x1, int y1, int z1, int x2, int y2, int z2, int lightLevel) {
		for (int z = z1; z <= z2; z++) {
			for (int y = y1; y <= y2; y++) {
				for (int x = x1; x <= x2; x++) {
					BlockPos pos = new BlockPos(x, y, z);
					world.setLight(type, pos, lightLevel);
				}
			}
		}
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos) {
		if (args.length == 0) {
			return Collections.emptyList();
		} else if (args.length == 1) {
			return suggestMatching(args, String.valueOf(targetPos.getX()));
		} else if (args.length == 2) {
			return suggestMatching(args, String.valueOf(targetPos.getY()));
		} else if (args.length == 3) {
			return suggestMatching(args, String.valueOf(targetPos.getZ()));
		} else if (args.length == 4) {
			return suggestMatching(args, String.valueOf(targetPos.getX()));
		} else if (args.length == 5) {
			return suggestMatching(args, String.valueOf(targetPos.getY()));
		} else if (args.length == 6) {
			return suggestMatching(args, String.valueOf(targetPos.getZ()));
		} else if (args.length == 7) {
			return suggestMatching(args, "sky", "block");
		} else if (args.length == 8) {
			return suggestMatching(args, "0", "15");
		} else {
			return Collections.emptyList();
		}
	}
}
