package carpet.commands;

import carpet.helpers.CapturedDrops;
import carpet.worldedit.WorldEditBridge;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandResults;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.SetBlockCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class CommandZetBlock extends SetBlockCommand {
	/**
	 * Gets the name of the command
	 */
	public String getName()
	{
		return "zetblock";
	}

	/**
	 * Return the required permission level for this command.
	 * TODO: overwritten method has same content, why overwrite?
	 */
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	/**
	 * Gets the usage string for the command.
	 * TODO: overwritten method has same content, why overwrite?
	 */
	public String getUsage(CommandSource sender)
	{
		return "commands.setblock.usage";
	}

	/**
	 * Callback for when the command is executed
	 *TODO: overwritten method has same content, why overwrite? no it has not:
	 * - there's a mixin for SetBlockCommand (which should not affect us then!)
	 * - we don't care about loaded chunks
	 */
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
	{
		if (args.length < 4)
		{
			throw new IncorrectUsageException("commands.setblock.usage");
		}
		else
		{
			sender.addResult(CommandResults.Type.AFFECTED_BLOCKS, 0);
			BlockPos blockpos = parseBlockPos(sender, args, 0, false);
			Block block = parseBlock(sender, args[3]);
			BlockState iblockstate;

			if (args.length >= 5)
			{
				iblockstate = parseBlockState(block, args[4]);
			}
			else
			{
				iblockstate = block.defaultState();
			}

			World world = sender.getSourceWorld();

			NbtCompound nbttagcompound = new NbtCompound();
			boolean flag = false;

			if (args.length >= 7 && block.hasBlockEntity())
			{
				String s = parseString(args, 6);

				try
				{
					nbttagcompound = StringNbtReader.parse(s);
					flag = true;
				}
				catch (NbtException nbtexception)
				{
					throw new CommandException("commands.setblock.tagError", new Object[] {nbtexception.getMessage()});
				}
			}

			ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
			NbtCompound worldEditTag = flag ? nbttagcompound : null;

			boolean updates = true;

			if (args.length >= 6)
			{
				if ("destroy".equals(args[5]))
				{
					WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos, Blocks.AIR.defaultState(), worldEditTag);
					CapturedDrops.setCapturingDrops(true);
					world.breakBlock(blockpos, true);
					CapturedDrops.setCapturingDrops(false);
					for (ItemEntity drop : CapturedDrops.getCapturedDrops())
						WorldEditBridge.recordEntityCreation(worldEditPlayer, world, drop);
					CapturedDrops.clearCapturedDrops();

					if (block == Blocks.AIR)
					{
						sendSuccess(sender, this, "commands.setblock.success");
						return;
					}
				}
				else if ("keep".equals(args[5]) && !world.isAir(blockpos))
				{
					throw new CommandException("commands.setblock.noChange");
				}
				else if ("noupdate".equals(args[5]))
				{
					updates = false;
				}
			}

			WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos, iblockstate, worldEditTag);

			BlockEntity tileentity1 = world.getBlockEntity(blockpos);

			if (tileentity1 != null && tileentity1 instanceof Inventory)
			{
				((Inventory)tileentity1).clear();
			}

			// SetBlockCommand has the 2 here replaced with something like
			// 2 | (CarpetSettings.fillUpdates ? 1024 : 0)
			// later on in World the 1024 flag also needs to be 0 to update the neighbour
			if (!world.setBlockState(blockpos, iblockstate, 2 | (updates ? 0 : 1024)))
			{
				throw new CommandException("commands.setblock.noChange");
			}
			else
			{
				if (flag)
				{
					BlockEntity tileentity = world.getBlockEntity(blockpos);

					if (tileentity != null)
					{
						nbttagcompound.putInt("x", blockpos.getX());
						nbttagcompound.putInt("y", blockpos.getY());
						nbttagcompound.putInt("z", blockpos.getZ());
						tileentity.readNbt(nbttagcompound);
					}
				}

				if (updates) {
					world.onBlockChanged(blockpos, iblockstate.getBlock(), false);
				}
				sender.addResult(CommandResults.Type.AFFECTED_BLOCKS, 1);
				sendSuccess(sender, this, "commands.setblock.success");
			}
		}
	}

	/**
	 * Get a list of options for when the user presses the TAB key
	 * TODO: overwritten method has same content, why overwrite?
	 */
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos)
	{
		if (args.length > 0 && args.length <= 3)
		{
			return suggestCoordinate(args, 0, targetPos);
		}
		else if (args.length == 4)
		{
			return suggestMatching(args, Block.REGISTRY.keySet());
		}
		else
		{
			return args.length == 6 ? suggestMatching(args, "replace", "destroy", "keep", "noupdate") : Collections.emptyList();
		}
	}
}