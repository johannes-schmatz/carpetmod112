package carpet.commands;

import carpet.mixin.accessors.PlayerChunkMapAccessor;
import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import carpet.mixin.accessors.ServerChunkProviderAccessor;
import carpet.mixin.accessors.WorldAccessor;
import carpet.utils.extensions.ExtendedThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import net.minecraft.client.util.NetworkUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.packet.s2c.play.ChunkUnloadS2CPacket;
import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerWorldManager;
import net.minecraft.server.world.ChunkGenerator;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStorage;
import net.minecraft.world.chunk.ServerChunkProvider;
import net.minecraft.world.chunk.ThreadedAnvilChunkStorage;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandChunk extends CommandCarpetBase
{
	/**
	 * Gets the name of the command
	 */

	public String getUsageTranslationKey(CommandSource sender)
	{
		return "Usage: chunk <load | info | unload | regen | repop | asyncrepop | delete> <X> <Z>";
	}

	public String getCommandName()
	{
		return "chunk";
	}
	/**
	 * Callback for when the command is executed
	 */
	public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
	{
		if (!command_enabled("commandChunk", sender)) return;

		if (args.length != 3) {
			throw new IncorrectUsageException(getUsageTranslationKey(sender));
		}

		World world = sender.getWorld();
		try {
			int chunkX = parseChunkPosition(args[1], sender.getBlockPos().getX());
			int chunkZ = parseChunkPosition(args[2], sender.getBlockPos().getZ());

			switch (args[0]){
				case "load":
					world.getChunk(chunkX, chunkZ);
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
		}catch (Exception e){
			throw new IncorrectUsageException(getUsageTranslationKey(sender));
		}
	}

	private boolean checkRepopLoaded(World world, int x, int z){
		return ((WorldAccessor) world).invokeIsChunkLoaded(x, z, false)
				&& ((WorldAccessor) world).invokeIsChunkLoaded(x+1, z, false)
				&& ((WorldAccessor) world).invokeIsChunkLoaded(x, z+1, false)
				&& ((WorldAccessor) world).invokeIsChunkLoaded(x+1, z+1, false);
	}

	private void regen(World world, CommandSource sender, int x, int z) {
		if(!checkRepopLoaded(world, x, z)) {
			sender.sendMessage(new LiteralText(("Area not loaded for re-population")));
		}

		ServerChunkProvider chunkProvider = (ServerChunkProvider) world.getChunkProvider();
		long i = ChunkPos.getIdFromCoords(x, z);
		Long2ObjectMap<Chunk> map = ((ServerChunkProviderAccessor) chunkProvider).getLoadedChunksMap();
		map.remove(i);
		ChunkGenerator chunkGenerator = ((ServerChunkProviderAccessor) chunkProvider).getChunkGenerator();
		Chunk chunk = chunkGenerator.generate(x, z);
		map.put(i, chunk);
		chunk.loadToWorld();
		chunk.setTerrainPopulated(true);
		chunk.populateBlockEntities(false);
		ChunkPlayerManager entry = ((ServerWorld)world).getPlayerWorldManager().method_12811(x, z);
		if (entry != null && entry.getChunk() != null) {
			((PlayerChunkMapEntryAccessor) entry).setChunk(chunk);
			((PlayerChunkMapEntryAccessor) entry).setSentToPlayers(false);

			entry.method_12801();
		}
	}

	private void repop(World world, CommandSource sender, int x, int z) {
		if(!checkRepopLoaded(world, x, z)) {
			sender.sendMessage(new LiteralText(("Area not loaded for re-population")));
		}

		ServerChunkProvider chunkProvider = (ServerChunkProvider) world.getChunkProvider();
		ChunkGenerator chunkGenerator = ((ServerChunkProviderAccessor) chunkProvider).getChunkGenerator();
		Chunk chunk = ((ServerChunkProviderAccessor) chunkProvider).invokeLoadChunk(x, z);
		chunk.setTerrainPopulated(false);
		chunk.populateIfMissing(chunkProvider, chunkGenerator);
	}

	private void asyncrepop(World world, CommandSource sender, int x, int z) {
		if(!checkRepopLoaded(world, x, z)) {
			sender.sendMessage(new LiteralText(("Area not loaded for re-population")));
		}

		NetworkUtils.downloadExcecutor.submit(() -> {
			try {
				ServerChunkProvider chunkProvider = (ServerChunkProvider) world.getChunkProvider();
				ChunkGenerator chunkGenerator = ((ServerChunkProviderAccessor) chunkProvider).getChunkGenerator();
				Chunk chunk = ((ServerChunkProviderAccessor) chunkProvider).invokeLoadChunk(x, z);
				chunk.setTerrainPopulated(false);
				chunk.populateIfMissing(chunkProvider, chunkGenerator);
				System.out.println("Chunk async repop end.");
			} catch(Throwable e) {
				e.printStackTrace();
			}
		});
	}

	protected void info(World world, CommandSource sender, int x, int z) throws NoSuchFieldException, IllegalAccessException {
		if(!((WorldAccessor) world).invokeIsChunkLoaded(x, z, false)) {
			sender.sendMessage(new LiteralText(("Chunk is not loaded")));
		}

		long i = ChunkPos.getIdFromCoords(x, z);
		ServerChunkProvider provider = (ServerChunkProvider) world.getChunkProvider();
		int mask = CommandLoadedChunks.getMask((Long2ObjectOpenHashMap<Chunk>) ((ServerChunkProviderAccessor) provider).getLoadedChunksMap());
		long key = HashCommon.mix(i) & mask;
		sender.sendMessage(new LiteralText(("Chunk ideal key is " + key)));
		if (world.isChunkInsideSpawnChunks(x, z))
			sender.sendMessage(new LiteralText(("Spawn Chunk")));
	}

	protected void unload(World world, CommandSource sender, int x, int z){
		if(!((WorldAccessor) world).invokeIsChunkLoaded(x, z, false)) {
			sender.sendMessage(new LiteralText(("Chunk is not loaded")));
			return;
		}
		Chunk chunk = world.getChunk(x, z);
		ServerChunkProvider provider = (ServerChunkProvider) world.getChunkProvider();
		provider.unload(chunk);
		sender.sendMessage(new LiteralText(("Chunk is queue to unload")));
	}

	protected void delete(World world, CommandSource sender, int x, int z) {
		{ // delete chunk from memory
			ServerChunkProvider provider = (ServerChunkProvider) world.getChunkProvider();
			long id = ChunkPos.getIdFromCoords(x, z);

			// loadedChunkMap
			((ServerChunkProviderAccessor) provider).getLoadedChunksMap().remove(id);

			// chunksToUnload
			((ServerChunkProviderAccessor) provider).getDroppedChunks().remove(id);
		}

		{ // delete chunk from PlayerWorldManager
			PlayerWorldManager playerWorldManager = ((ServerWorld) world).getPlayerWorldManager();
			ChunkPlayerManager chunkPlayerManager = playerWorldManager.method_12811(x, z);
			if (chunkPlayerManager != null) { // the ChunkPlayerManager might not exist
				((PlayerChunkMapEntryAccessor) chunkPlayerManager).setChunk(null);

				((PlayerChunkMapEntryAccessor) chunkPlayerManager).setSentToPlayers(false);

				((PlayerChunkMapAccessor) playerWorldManager).getEntriesWithoutChunks().add(chunkPlayerManager);

				// send unload packet
				ChunkUnloadS2CPacket packet = new ChunkUnloadS2CPacket(x, z);
				for (ServerPlayerEntity e : ((PlayerChunkMapEntryAccessor) chunkPlayerManager).getPlayers()) {
					e.networkHandler.sendPacket(packet);
				}

				// so that this can refresh the client state
				((PlayerChunkMapAccessor) playerWorldManager).getEntries().add(chunkPlayerManager);
			}
		}


		{ // delete chunk on disk
			ChunkStorage storage = ((ServerChunkProviderAccessor) world.getChunkProvider()).getChunkLoader();
			if (storage instanceof ThreadedAnvilChunkStorage) {
				ThreadedAnvilChunkStorage anvilChunkStorage = (ThreadedAnvilChunkStorage) storage;

				((ExtendedThreadedAnvilChunkStorage) anvilChunkStorage).deleteChunk(x, z);
			} else {
				sender.sendMessage(new LiteralText("Storage " + storage + " isn't instanceof ThreadedAnvilChunkStorage, can't delete chunks for these"));
			}
		}
	}

	public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
		int chunkX = sender.getBlockPos().getX() >> 4;
		int chunkZ = sender.getBlockPos().getZ() >> 4;

		if (args.length == 1) {
			return method_2894(args, "info", "load", "unload", "regen", "repop", "asyncrepop", "delete");
		} else if (args.length == 2) {
			return method_2894(args, Integer.toString(chunkX), "~");
		} else if (args.length == 3) {
			return method_2894(args, Integer.toString(chunkZ), "~");
		} else {
			return Collections.emptyList();
		}
	}
}