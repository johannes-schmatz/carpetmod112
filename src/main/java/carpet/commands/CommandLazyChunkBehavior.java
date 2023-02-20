package carpet.commands;

import carpet.helpers.LazyChunkBehaviorHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;


public class CommandLazyChunkBehavior extends CommandCarpetBase {


	/**
	 * Gets the name of the command
	 */

	public String getUsageTranslationKey(CommandSource sender)
	{
		return "Usage: lazychunkbehavior <add, removeAll, remove, list> <X> <Z>";
	}

	public String getCommandName()
	{
		return "lazychunkbehavior";
	}

	private void list(CommandSource sender, String[] args) throws CommandException{
		if (args.length != 1)
		{
			throw new IncorrectUsageException(getUsageTranslationKey(sender));
		}
		LazyChunkBehaviorHelper.listLazyChunks(sender);
	}
	private void removeAll(CommandSource sender, String[] args) throws CommandException{
		if (args.length != 1)
		{
			throw new IncorrectUsageException(getUsageTranslationKey(sender));
		}

		LazyChunkBehaviorHelper.removeAll();
		Text text = new LiteralText("All chunks have been removed." );
		text.getStyle().setFormatting(Formatting.RED);
		sender.sendMessage(text);
	}

	private void add(CommandSource sender, String[] args) throws CommandException{

		if (args.length != 3)
		{
			throw new IncorrectUsageException(getUsageTranslationKey(sender));
		}
		int chunkX = parseInt(args[1]);
		int chunkZ = parseInt(args[2]);
		World world = sender.getWorld();
		Chunk chunk = world.getChunk(chunkX, chunkZ);
		LazyChunkBehaviorHelper.addLazyChunk(chunk);
		Text text = new LiteralText("Chunk " + chunkX + ", " + chunkZ + " in world " + chunk.getWorld().dimension.getDimensionType() + " has been added" +
				"." );
		text.getStyle().setFormatting(Formatting.RED);
		sender.sendMessage(text);
	}

	private void remove(CommandSource sender, String[] args) throws CommandException{

		if (args.length != 3)
		{
			throw new IncorrectUsageException(getUsageTranslationKey(sender));
		}

		int chunkX = parseInt(args[1]);
		int chunkZ = parseInt(args[2]);
		World world = sender.getWorld();
		Chunk chunk = world.getChunk(chunkX, chunkZ);

		LazyChunkBehaviorHelper.removeLazyChunk(chunk);
		Text text = new LiteralText("Chunk " + chunkX + ", " + chunkZ + " in world " + chunk.getWorld().dimension.getDimensionType() + " has been " +
				"removed." );
		text.getStyle().setFormatting(Formatting.RED);
		sender.sendMessage(text);
	}

	/**
	 * Callback for when the command is executed
	 */
	public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
	{
		if (!command_enabled("CommandLazyChunkBehavior", sender)) return;

		if(args.length < 1 || args.length > 3){
			throw new IncorrectUsageException(getUsageTranslationKey(sender));

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
	public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {

		int chunkX = sender.getBlockPos().getX() >> 4;
		int chunkZ = sender.getBlockPos().getZ() >> 4;

		if (args.length == 1) {
			return method_2894(args, "list", "add", "remove","removeAll");
		}
		else if(args.length == 2 && !args[0].equals("list") && !args[0].equals("removeAll")){
			return method_2894(args, Integer.toString(chunkX));
		}
		else if (args.length == 3 && !args[0].equals("list") && !args[0].equals("removeAll")) {
			return method_2894(args, Integer.toString(chunkZ));
		} else {
			return Collections.emptyList();
		}
	}
}