package carpet.commands;

import carpet.mixin.accessors.PlayerChunkMapAccessor;
import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import carpet.mixin.accessors.ServerChunkProviderAccessor;
import carpet.mixin.accessors.WorldAccessor;
import carpet.utils.extensions.ExtendedThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import net.minecraft.network.packet.s2c.play.ForgetWorldChunkS2CPacket;
import net.minecraft.server.ChunkHolder;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.text.LiteralText;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkGenerator;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.storage.AnvilChunkStorage;
import net.minecraft.world.chunk.storage.ChunkStorage;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandChunk extends CommandCarpetBase {
	public String getUsage(CommandSource sender) {
		return "Usage: chunk <load | info | unload | regen | repop | asyncrepop | delete> <X> <Z>";
	}

	public String getName() {
		return "chunk";
	}

	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandChunk", sender)) return;

		if (args.length != 3) {
			throw new IncorrectUsageException(getUsage(sender));
		}

		World world = sender.getSourceWorld();
		try {
			int chunkX = parseChunkPosition(args[1], sender.getSourceBlockPos().getX());
			int chunkZ = parseChunkPosition(args[2], sender.getSourceBlockPos().getZ());

			switch (args[0]) {
				case "load":
					world.getChunkAt(chunkX, chunkZ);
					sender.sendMessage(new LiteralText("Chunk " + chunkX + ", " + chunkZ + " loaded"));
					return;
				case "unload":
					unload(world, sender, chunkX, chunkZ);
					return;
				case "regen":
					regen(world, sender, chunkX, chunkZ);
					return;
				case "repop":
					repop(world, sender, chunkX, chunkZ);
					return;
				case "asyncrepop":
					asyncrepop(world, sender, chunkX, chunkZ);
					return;
				case "delete":
					delete(world, sender, chunkX, chunkZ);
					return;
				case "info":
				default:
					info(world, sender, chunkX, chunkZ);

			}
		} catch (Exception e) {
			throw new IncorrectUsageException(getUsage(sender));
		}
	}

	private boolean checkRepopLoaded(World world, int x, int z) {
		return ((WorldAccessor) world).invokeIsChunkLoaded(x, z, false) && ((WorldAccessor) world).invokeIsChunkLoaded(x + 1, z, false) &&
				((WorldAccessor) world).invokeIsChunkLoaded(x, z + 1, false) && ((WorldAccessor) world).invokeIsChunkLoaded(x + 1, z + 1, false);
	}

	private void regen(World world, CommandSource sender, int x, int z) {
		if (!checkRepopLoaded(world, x, z)) {
			sender.sendMessage(new LiteralText(("Area not loaded for re-population")));
		}

		ServerChunkCache chunkProvider = (ServerChunkCache) world.getChunkSource();
		long i = ChunkPos.toLong(x, z);
		Long2ObjectMap<WorldChunk> map = ((ServerChunkProviderAccessor) chunkProvider).getLoadedChunksMap();
		map.remove(i);
		ChunkGenerator chunkGenerator = ((ServerChunkProviderAccessor) chunkProvider).getChunkGenerator();
		WorldChunk chunk = chunkGenerator.getChunk(x, z);
		map.put(i, chunk);
		chunk.load();
		chunk.setTerrainPopulated(true);
		chunk.tick(false);
		ChunkHolder entry = ((ServerWorld) world).getChunkMap().getChunk(x, z);
		if (entry != null && entry.getChunk() != null) {
			((PlayerChunkMapEntryAccessor) entry).setChunk(chunk);
			((PlayerChunkMapEntryAccessor) entry).setSentToPlayers(false);

			entry.populate();
		}
	}

	private void repop(World world, CommandSource sender, int x, int z) {
		if (!checkRepopLoaded(world, x, z)) {
			sender.sendMessage(new LiteralText(("Area not loaded for re-population")));
		}

		ServerChunkCache chunkProvider = (ServerChunkCache) world.getChunkSource();
		ChunkGenerator chunkGenerator = ((ServerChunkProviderAccessor) chunkProvider).getChunkGenerator();
		WorldChunk chunk = ((ServerChunkProviderAccessor) chunkProvider).invokeLoadChunk(x, z);
		chunk.setTerrainPopulated(false);
		chunk.populate(chunkProvider, chunkGenerator);
	}

	private void asyncrepop(World world, CommandSource sender, int x, int z) {
		if (!checkRepopLoaded(world, x, z)) {
			sender.sendMessage(new LiteralText(("Area not loaded for re-population")));
		}

		HttpUtil.DOWNLOAD_THREAD_FACTORY.submit(() -> {
			try {
				ServerChunkCache chunkProvider = (ServerChunkCache) world.getChunkSource();
				ChunkGenerator chunkGenerator = ((ServerChunkProviderAccessor) chunkProvider).getChunkGenerator();
				WorldChunk chunk = ((ServerChunkProviderAccessor) chunkProvider).invokeLoadChunk(x, z);
				chunk.setTerrainPopulated(false);
				chunk.populate(chunkProvider, chunkGenerator);
				System.out.println("Chunk async repop end.");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

	protected void info(World world, CommandSource sender, int x, int z) throws NoSuchFieldException, IllegalAccessException {
		if (!((WorldAccessor) world).invokeIsChunkLoaded(x, z, false)) {
			sender.sendMessage(new LiteralText(("Chunk is not loaded")));
		}

		long i = ChunkPos.toLong(x, z);
		ServerChunkCache provider = (ServerChunkCache) world.getChunkSource();
		int mask = CommandLoadedChunks.getMask((Long2ObjectOpenHashMap<WorldChunk>) ((ServerChunkProviderAccessor) provider).getLoadedChunksMap());
		long key = HashCommon.mix(i) & mask;
		sender.sendMessage(new LiteralText(("Chunk ideal key is " + key)));
		if (world.isSpawnChunk(x, z)) sender.sendMessage(new LiteralText(("Spawn Chunk")));
	}

	protected void unload(World world, CommandSource sender, int x, int z) {
		if (!((WorldAccessor) world).invokeIsChunkLoaded(x, z, false)) {
			sender.sendMessage(new LiteralText(("Chunk is not loaded")));
			return;
		}
		WorldChunk chunk = world.getChunkAt(x, z);
		ServerChunkCache provider = (ServerChunkCache) world.getChunkSource();
		provider.scheduleUnload(chunk);
		sender.sendMessage(new LiteralText(("Chunk is queue to unload")));
	}

	protected void delete(World world, CommandSource sender, int x, int z) {
		{ // delete chunk from memory
			ServerChunkCache provider = (ServerChunkCache) world.getChunkSource();
			long id = ChunkPos.toLong(x, z);

			// loadedChunkMap
			((ServerChunkProviderAccessor) provider).getLoadedChunksMap().remove(id);

			// chunksToUnload
			((ServerChunkProviderAccessor) provider).getDroppedChunks().remove(id);
		}

		{ // delete chunk from PlayerWorldManager
			ChunkMap playerWorldManager = ((ServerWorld) world).getChunkMap();
			ChunkHolder chunkPlayerManager = playerWorldManager.getChunk(x, z);
			if (chunkPlayerManager != null) { // the ChunkPlayerManager might not exist
				((PlayerChunkMapEntryAccessor) chunkPlayerManager).setChunk(null);

				((PlayerChunkMapEntryAccessor) chunkPlayerManager).setSentToPlayers(false);

				((PlayerChunkMapAccessor) playerWorldManager).getEntriesWithoutChunks().add(chunkPlayerManager);

				// send unload packet
				ForgetWorldChunkS2CPacket packet = new ForgetWorldChunkS2CPacket(x, z);
				for (ServerPlayerEntity e : ((PlayerChunkMapEntryAccessor) chunkPlayerManager).getPlayers()) {
					e.networkHandler.sendPacket(packet);
				}

				// so that this can refresh the client state
				((PlayerChunkMapAccessor) playerWorldManager).getEntries().add(chunkPlayerManager);
			}
		}


		{ // delete chunk on disk
			ChunkStorage storage = ((ServerChunkProviderAccessor) world.getChunkSource()).getChunkLoader();
			if (storage instanceof AnvilChunkStorage) {
				AnvilChunkStorage anvilChunkStorage = (AnvilChunkStorage) storage;

				((ExtendedThreadedAnvilChunkStorage) anvilChunkStorage).deleteChunk(x, z);
			} else {
				sender.sendMessage(new LiteralText("Storage " + storage + " isn't instanceof ThreadedAnvilChunkStorage, can't delete chunks for these"));
			}
		}
	}

	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
		int chunkX = sender.getSourceBlockPos().getX() >> 4;
		int chunkZ = sender.getSourceBlockPos().getZ() >> 4;

		if (args.length == 1) {
			return suggestMatching(args, "info", "load", "unload", "regen", "repop", "asyncrepop", "delete");
		} else if (args.length == 2) {
			return suggestMatching(args, Integer.toString(chunkX), "~");
		} else if (args.length == 3) {
			return suggestMatching(args, Integer.toString(chunkZ), "~");
		} else {
			return Collections.emptyList();
		}
	}
}