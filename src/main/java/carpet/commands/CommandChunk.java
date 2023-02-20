package carpet.commands;

import carpet.mixin.accessors.ServerChunkProviderAccessor;
import carpet.mixin.accessors.WorldAccessor;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
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
		return "Usage: chunk <X> <Z> <load | info | unload>";
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

		int chunkX = parseChunkPosition(args[0], sender.getBlockPos().getX());
		int chunkZ = parseChunkPosition(args[1], sender.getBlockPos().getZ());

		world = sender.getWorld();
		try {
			switch (args[2]){
				case "load":
					world.getChunk(chunkX, chunkZ);
					sender.sendMessage(new LiteralText("Chunk " + chunkX + ", " + chunkZ + " loaded"));
					return;
				case "unload":
					unload(sender, chunkX, chunkZ);
					return;
				case "info":
				default:
					info(sender, chunkX, chunkZ);

			}
		}catch (Exception e){
			throw new IncorrectUsageException(getUsageTranslationKey(sender));
		}
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
			return method_2894(args, Integer.toString(chunkX), "~");
		} else if (args.length == 2) {
			return method_2894(args, Integer.toString(chunkZ), "~");
		} else if (args.length == 3) {
			return method_2894(args, "info", "load", "unload");
		} else {
			return Collections.emptyList();
		}
	}
}