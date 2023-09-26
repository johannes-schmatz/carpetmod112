package carpet.commands;

import carpet.helpers.LazyChunkBehaviorHelper;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;


public class CommandLazyChunkBehavior extends CommandCarpetBase {


	/**
	 * Gets the name of the command
	 */

	public String getUsage(CommandSource sender)
	{
		return "Usage: lazychunkbehavior <add, removeAll, remove, list> <X> <Z>";
	}

	public String getName()
	{
		return "lazychunkbehavior";
	}

	private void list(CommandSource sender, String[] args) throws CommandException{
		if (args.length != 1)
		{
			throw new IncorrectUsageException(getUsage(sender));
		}
		LazyChunkBehaviorHelper.listLazyChunks(sender);
	}
	private void removeAll(CommandSource sender, String[] args) throws CommandException{
		if (args.length != 1)
		{
			throw new IncorrectUsageException(getUsage(sender));
		}

		LazyChunkBehaviorHelper.removeAll();
		Text text = new LiteralText("All chunks have been removed." );
		text.getStyle().setColor(Formatting.RED);
		sender.sendMessage(text);
	}

	private void add(CommandSource sender, String[] args) throws CommandException{

		if (args.length != 3)
		{
			throw new IncorrectUsageException(getUsage(sender));
		}
		int chunkX = parseInt(args[1]);
		int chunkZ = parseInt(args[2]);
		World world = sender.getSourceWorld();
		WorldChunk chunk = world.getChunkAt(chunkX, chunkZ);
		LazyChunkBehaviorHelper.addLazyChunk(chunk);
		Text text = new LiteralText("Chunk " + chunkX + ", " + chunkZ + " in world " + chunk.getWorld().dimension.getType() + " has been added" +
				"." );
		text.getStyle().setColor(Formatting.RED);
		sender.sendMessage(text);
	}

	private void remove(CommandSource sender, String[] args) throws CommandException{

		if (args.length != 3)
		{
			throw new IncorrectUsageException(getUsage(sender));
		}

		int chunkX = parseInt(args[1]);
		int chunkZ = parseInt(args[2]);
		World world = sender.getSourceWorld();
		WorldChunk chunk = world.getChunkAt(chunkX, chunkZ);

		LazyChunkBehaviorHelper.removeLazyChunk(chunk);
		Text text = new LiteralText("Chunk " + chunkX + ", " + chunkZ + " in world " + chunk.getWorld().dimension.getType() + " has been " +
				"removed." );
		text.getStyle().setColor(Formatting.RED);
		sender.sendMessage(text);
	}

	/**
	 * Callback for when the command is executed
	 */
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
	{
		if (!command_enabled("CommandLazyChunkBehavior", sender)) return;

		if(args.length < 1 || args.length > 3){
			throw new IncorrectUsageException(getUsage(sender));

		}
		switch (args[0]){
			case "list":
				list(sender,args);
				return;
			case "add":
				add(sender,args);
				return;
			case "remove":
				remove(sender,args);
				return;
			case "removeAll":
				removeAll(sender,args);
		}
	}
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {

		int chunkX = sender.getSourceBlockPos().getX() >> 4;
		int chunkZ = sender.getSourceBlockPos().getZ() >> 4;

		if (args.length == 1) {
			return suggestMatching(args, "list", "add", "remove","removeAll");
		}
		else if(args.length == 2 && !args[0].equals("list") && !args[0].equals("removeAll")){
			return suggestMatching(args, Integer.toString(chunkX));
		}
		else if (args.length == 3 && !args[0].equals("list") && !args[0].equals("removeAll")) {
			return suggestMatching(args, Integer.toString(chunkZ));
		} else {
			return Collections.emptyList();
		}
	}
}