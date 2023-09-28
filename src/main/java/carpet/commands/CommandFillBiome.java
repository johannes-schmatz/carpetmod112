package carpet.commands;

import java.util.Collections;
import java.util.List;

import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;

import net.minecraft.network.packet.s2c.play.WorldChunkS2CPacket;
import net.minecraft.server.ChunkHolder;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;

public class CommandFillBiome extends CommandCarpetBase {
	@Override
	public String getName() {
		return "fillbiome";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "/fillbiome <from: x z> <to: x z> <biome>";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandFillBiome", sender)) return;

		if (args.length < 5) throw new IncorrectUsageException(getUsage(sender));

		int x1 = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getX(), args[0], false).getCoordinate());
		int z1 = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getZ(), args[1], false).getCoordinate());
		int x2 = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getX(), args[2], false).getCoordinate());
		int z2 = (int) Math.round(parseTeleportCoordinate(sender.getSourceBlockPos().getZ(), args[3], false).getCoordinate());

		int minX = Math.min(x1, x2);
		int maxX = Math.max(x1, x2);
		int minZ = Math.min(z1, z2);
		int maxZ = Math.max(z1, z2);

		Biome biome;
		try {
			biome = Biome.byId(Integer.parseInt(args[4]));
		} catch (NumberFormatException e) {
			biome = Biome.REGISTRY.get(new Identifier(args[4]));
		}
		if (biome == null) {
			throw new CommandException("Unknown biome " + args[4]);
		}
		byte biomeId = (byte) (Biome.getId(biome) & 255);

		ServerWorld world = (ServerWorld) sender.getSourceWorld();
		if (!world.isAreaLoaded(new BlockPos(minX, 0, minZ), new BlockPos(maxX, 0, maxZ))) {
			throw new CommandException("commands.fill.outOfWorld");
		}

		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				WorldChunk chunk = world.getChunk(pos.set(x, 0, z));
				chunk.getBiomes()[(x & 15) | (z & 15) << 4] = biomeId;
				chunk.markDirty();
			}
		}

		int minChunkX = Math.floorDiv(minX, 16);
		int maxChunkX = Math.floorDiv(maxX, 16);
		int minChunkZ = Math.floorDiv(minZ, 16);
		int maxChunkZ = Math.floorDiv(maxZ, 16);
		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				ChunkHolder entry = world.getChunkMap().getChunk(chunkX, chunkZ);
				if (entry != null) {
					WorldChunk chunk = entry.getChunk();
					if (chunk != null) {
						WorldChunkS2CPacket packet = new WorldChunkS2CPacket(chunk, 65535);
						for (ServerPlayerEntity player : ((PlayerChunkMapEntryAccessor) entry).getPlayers())
							player.networkHandler.sendPacket(packet);
					}
				}
			}
		}

		sendSuccess(sender, this, ((maxX - minX + 1) * (maxZ - minZ + 1)) + " biome blocks changed");
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos) {
		if (args.length == 0) {
			return Collections.emptyList();
		} else if (args.length == 1 || args.length == 3) {
			if (targetPos == null) return suggestMatching(args, "~");
			else return suggestMatching(args, String.valueOf(targetPos.getX()));
		} else if (args.length == 2 || args.length == 4) {
			if (targetPos == null) return suggestMatching(args, "~");
			else return suggestMatching(args, String.valueOf(targetPos.getZ()));
		} else if (args.length == 5) {
			return suggestMatching(args, Biome.REGISTRY.keySet());
		} else {
			return Collections.emptyList();
		}
	}
}
