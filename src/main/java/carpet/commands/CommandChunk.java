package carpet.commands;

import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import carpet.mixin.accessors.ServerChunkProviderAccessor;
import carpet.mixin.accessors.WorldAccessor;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import net.minecraft.client.util.NetworkUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkGenerator;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ServerChunkProvider;

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
		return "Usage: chunk <load | info | unload | regen | repop | asyncrepop> <X> <Z>";
	}

	public String getCommandName()
	{
		return "chunk";
	}

	protected World world; // TODO: convert to local variable + parameters
	/**
	 * Callback for when the command is executed
	 */
	public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
	{
		if (!command_enabled("commandChunk", sender)) return;

		if (args.length != 3) {
			throw new IncorrectUsageException(getUsageTranslationKey(sender));
		}

		world = sender.getWorld();
		try {
			int chunkX = parseChunkPosition(args[1], sender.getBlockPos().getX());
			int chunkZ = parseChunkPosition(args[2], sender.getBlockPos().getZ());

			switch (args[0]){
				case "load":
					world.getChunk(chunkX, chunkZ);
					sender.sendMessage(new LiteralText("Chunk " + chunkX + ", " + chunkZ + " loaded"));
					return;
				case "unload":
					unload(sender, chunkX, chunkZ);
					return;
				case "regen":
					regen(sender, chunkX, chunkZ);
					return;
				case "repop":
					repop(sender, chunkX, chunkZ);
					return;
				case "asyncrepop":
					asyncrepop(sender, chunkX, chunkZ);
					return;
				case "info":
				default:
					info(sender, chunkX, chunkZ);

			}
		}catch (Exception e){
			throw new IncorrectUsageException(getUsageTranslationKey(sender));
		}
	}

	private boolean checkRepopLoaded(int x, int z){
		return ((WorldAccessor) world).invokeIsChunkLoaded(x, z, false)
				&& ((WorldAccessor) world).invokeIsChunkLoaded(x+1, z, false)
				&& ((WorldAccessor) world).invokeIsChunkLoaded(x, z+1, false)
				&& ((WorldAccessor) world).invokeIsChunkLoaded(x+1, z+1, false);
	}

	private void regen(CommandSource sender, int x, int z) {
		if(!checkRepopLoaded(x, z)) {
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

	private void repop(CommandSource sender, int x, int z) {
		if(!checkRepopLoaded(x, z)) {
			sender.sendMessage(new LiteralText(("Area not loaded for re-population")));
		}

		ServerChunkProvider chunkProvider = (ServerChunkProvider) world.getChunkProvider();
		ChunkGenerator chunkGenerator = ((ServerChunkProviderAccessor) chunkProvider).getChunkGenerator();
		Chunk chunk = ((ServerChunkProviderAccessor) chunkProvider).invokeLoadChunk(x, z);
		chunk.setTerrainPopulated(false);
		chunk.populateIfMissing(chunkProvider, chunkGenerator);
	}

	private void asyncrepop(CommandSource sender, int x, int z) {
		if(!checkRepopLoaded(x, z)) {
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

	protected void info(CommandSource sender, int x, int z) throws NoSuchFieldException, IllegalAccessException {
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

	protected void unload(CommandSource sender, int x, int z){
		if(!((WorldAccessor) world).invokeIsChunkLoaded(x, z, false)) {
			sender.sendMessage(new LiteralText(("Chunk is not loaded")));
			return;
		}
		Chunk chunk = world.getChunk(x, z);
		ServerChunkProvider provider = (ServerChunkProvider) world.getChunkProvider();
		provider.unload(chunk);
		sender.sendMessage(new LiteralText(("Chunk is queue to unload")));
	}

	public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
		int chunkX = sender.getBlockPos().getX() >> 4;
		int chunkZ = sender.getBlockPos().getZ() >> 4;

		if (args.length == 1) {
			return method_2894(args, "info", "load", "unload", "regen", "repop", "asyncrepop");
		} else if (args.length == 2) {
			return method_2894(args, Integer.toString(chunkX), "~");
		} else if (args.length == 3) {
			return method_2894(args, Integer.toString(chunkZ), "~");
		} else {
			return Collections.emptyList();
		}
	}
}