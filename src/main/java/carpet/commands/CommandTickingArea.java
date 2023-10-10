package carpet.commands;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import carpet.CarpetSettings;
import carpet.utils.TickingArea;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.InvalidNumberException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.server.command.exception.IncorrectUsageException;

public class CommandTickingArea extends CommandCarpetBase {
	private static final String USAGE = "/tickingarea <add|remove|remove_all|list> ...";
	private static final String USAGE_ADD = "/tickingarea add [square|circle|spawnChunks] ...";
	private static final String USAGE_ADD_SQUARE = "/tickingarea add [square] <fromChunk: x z> <toChunk: x z> [name]";
	private static final String USAGE_ADD_CIRCLE = "/tickingarea add circle <centerChunk: x z> <radius> [name]";
	private static final String USAGE_REMOVE = "/tickingarea remove <name|chunkPos: x z>";

	@Override
	public String getName() {
		return "tickingarea";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return USAGE;
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("tickingAreas", sender)) return;

		if (args.length < 1) throw new IncorrectUsageException(getUsage(sender));

		switch (args[0]) {
			case "add":
				addTickingArea(sender, args);
				break;
			case "remove":
				removeTickingArea(sender, args);
				break;
			case "remove_all":
				removeAllTickingAreas(sender, args);
				break;
			case "list":
				listTickingAreas(sender, args);
				break;
			default:
				throw new IncorrectUsageException(getUsage(sender));
		}
	}

	private static ChunkPos parseChunkPos(CommandSource sender, String[] args, int index) throws InvalidNumberException {
		int x = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getX() >> 4, args[index], false).getCoordinate());
		int z = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getZ() >> 4, args[index + 1], false).getCoordinate());
		return new ChunkPos(x, z);
	}

	private void addTickingArea(CommandSource sender, String[] args) throws CommandException {
		if (args.length < 2) throw new IncorrectUsageException(USAGE_ADD);

		int index = 2;
		TickingArea area;

		if ("circle".equals(args[1])) {
			if (args.length < 5) throw new IncorrectUsageException(USAGE_ADD_CIRCLE);
			ChunkPos center = parseChunkPos(sender, args, index);
			index += 2;
			double radius = parseDouble(args[index++], 0);
			area = new TickingArea.Circle(center, radius);
		} else if ("spawnChunks".equals(args[1])) {
			area = new TickingArea.SpawnChunks();
		} else {
			if (!"square".equals(args[1])) index = 1;
			if (args.length < index + 4) throw new IncorrectUsageException(USAGE_ADD_SQUARE);
			ChunkPos from = parseChunkPos(sender, args, index);
			index += 2;
			ChunkPos to = parseChunkPos(sender, args, index);
			index += 2;
			ChunkPos min = new ChunkPos(Math.min(from.x, to.x), Math.min(from.z, to.z));
			ChunkPos max = new ChunkPos(Math.max(from.x, to.x), Math.max(from.z, to.z));
			area = new TickingArea.Square(min, max);
		}

		if (args.length > index) {
			area.setName(parseString(args, index));
		}

		TickingArea.addTickingArea(sender.getSourceWorld(), area);

		for (ChunkPos chunk : area.listIncludedChunks(sender.getSourceWorld())) {
			// Load chunk
			sender.getSourceWorld().getChunkAt(chunk.x, chunk.z);
		}

		sendSuccess(sender, this, "Added ticking area");
	}

	private void removeTickingArea(CommandSource sender, String[] args) throws CommandException {
		if (args.length < 2) throw new IncorrectUsageException(USAGE_REMOVE);

		boolean byName = false;
		boolean removed = false;
		if (args.length < 3) {
			byName = true;
		} else {
			try {
				ChunkPos pos = parseChunkPos(sender, args, 1);
				removed = TickingArea.removeTickingAreas(sender.getSourceWorld(), pos.x, pos.z);
			} catch (CommandException e) {
				byName = true;
			}
		}
		if (byName) {
			removed = TickingArea.removeTickingAreas(sender.getSourceWorld(), parseString(args, 1));
		}

		if (removed) sendSuccess(sender, this, "Removed ticking area");
		else throw new CommandException("Couldn't remove ticking area");
	}

	private void removeAllTickingAreas(CommandSource sender, String[] args) throws CommandException {
		TickingArea.removeAllTickingAreas(sender.getSourceWorld());
		sendSuccess(sender, this, "Removed all ticking areas");
	}

	private void listTickingAreas(CommandSource sender, String[] args) throws CommandException {
		if (args.length > 1 && "all-dimensions".equals(args[1])) {
			for (World world : sender.getServer().worlds) {
				listAreas(sender, world);
			}
		} else {
			listAreas(sender, sender.getSourceWorld());
		}
	}

	private void listAreas(CommandSource sender, World world) {
		if (world.dimension.isOverworld() && !CarpetSettings.disableSpawnChunks) sender.sendMessage(new LiteralText("Spawn chunks are enabled"));

		sender.sendMessage(new LiteralText("Ticking areas in " + world.dimension.getType().getKey() + ":"));

		for (TickingArea area : TickingArea.getTickingAreas(world)) {
			StringBuilder builder = new StringBuilder("- ");
			if (area.getName() != null) {
				builder.append(area.getName()).append(": ");
			}

			builder.append(area.format());

			sender.sendMessage(new LiteralText(builder.toString()));
		}
	}

	private static List<String> tabCompleteChunkPos(CommandSource sender, BlockPos targetPos, String[] args, int index) {
		if (targetPos == null) {
			return Lists.newArrayList("~");
		} else {
			if (index == args.length) {
				int x = sender.getSourceBlockPos().getX() / 16;
				return Lists.newArrayList(String.valueOf(x));
			} else {
				int z = sender.getSourceBlockPos().getZ() / 16;
				return Lists.newArrayList(String.valueOf(z));
			}
		}
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos) {
		if (args.length == 0) {
			return Collections.emptyList();
		} else if (args.length == 1) {
			return suggestMatching(args, "add", "remove", "remove_all", "list");
		} else if ("add".equals(args[0])) {
			if (args.length == 2) {
				List<String> completions = tabCompleteChunkPos(sender, targetPos, args, 2);
				Collections.addAll(completions, "square", "circle", "spawnChunks");
				return suggestMatching(args, completions);
			}
			int index = "square".equals(args[1]) || "circle".equals(args[1]) ? 3 : 2;
			if (args.length >= index && args.length < index + 2) {
				return tabCompleteChunkPos(sender, targetPos, args, index);
			} else if (args.length >= index + 2 && args.length < index + 4) {
				return tabCompleteChunkPos(sender, targetPos, args, index + 2);
			} else {
				return Collections.emptyList();
			}
		} else if ("remove".equals(args[0])) {
			if (args.length == 2) {
				List<String> completions = tabCompleteChunkPos(sender, targetPos, args, 2);
				TickingArea.getTickingAreas(sender.getSourceWorld())
						.stream()
						.filter(area -> area.getName() != null)
						.forEach(area -> completions.add(area.getName()));
				return suggestMatching(args, completions);
			} else if (args.length == 3) {
				return tabCompleteChunkPos(sender, targetPos, args, 2);
			} else {
				return Collections.emptyList();
			}
		} else if ("list".equals(args[0])) {
			if (args.length == 2) {
				return suggestMatching(args, "all-dimensions");
			} else {
				return Collections.emptyList();
			}
		} else {
			return Collections.emptyList();
		}
	}

}
