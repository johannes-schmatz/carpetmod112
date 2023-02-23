package carpet.commands;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ScheduledTick;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import carpet.worldedit.WorldEditBridge;
import org.jetbrains.annotations.Nullable;

public class CommandColon extends AbstractCommand
{
	/**
	 * Gets the name of the command
	 */
	public String getCommandName()
	{
		return "colon";
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getPermissionLevel()
	{
		return 2;
	}

	/**
	 * Gets the usage string for the command.
	 */
	public String getUsageTranslationKey(CommandSource sender)
	{
		return "commands.clone.usage";
	}

	/**
	 * Callback for when the command is executed
	 */
	public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
	{
		if (args.length < 9)
		{
			throw new IncorrectUsageException("commands.clone.usage");
		}
		else
		{
			sender.setStat(CommandStats.Type.AFFECTED_BLOCKS, 0);
			BlockPos blockpos = getBlockPos(sender, args, 0, false);
			BlockPos blockpos1 = getBlockPos(sender, args, 3, false);
			BlockPos blockpos2 = getBlockPos(sender, args, 6, false);
			BlockBox structureboundingbox = new BlockBox(blockpos, blockpos1);
			BlockBox structureboundingbox1 = new BlockBox(blockpos2, blockpos2.add(structureboundingbox.getDimensions()));

			boolean flag = false;
			Block block = null;
			Predicate<BlockState> predicate = null;

			if ((args.length < 11 || !"force".equals(args[10]) && !"move".equals(args[10]) && !"force_noupdate".equals(args[10]) && !"move_noupdate".equals(args[10])) && structureboundingbox.intersects(structureboundingbox1))
			{
				throw new CommandException("commands.clone.noOverlap");
			}
			else
			{
				if (args.length >= 11 && ("move".equals(args[10]) || "move_noupdate".equals(args[10])))
				{
					flag = true;
				}

				boolean update = true;
				if (args.length >= 11 && ("noupdate".equals(args[10]) || "force_noupdate".equals(args[10]) || "move_noupdate".equals(args[10])))
				{
					update = false;
				}

				if (structureboundingbox.minY >= 0 && structureboundingbox.maxY < 256 && structureboundingbox1.minY >= 0 && structureboundingbox1.maxY < 256)
				{
					World world = sender.getWorld();

					boolean flag1 = false;

					if (args.length >= 10)
					{
						if ("masked".equals(args[9]))
						{
							flag1 = true;
						}
						else if ("filtered".equals(args[9]))
						{
							if (args.length < 12)
							{
								throw new IncorrectUsageException("commands.clone.usage");
							}

							block = getBlock(sender, args[11]);

							if (args.length >= 13)
							{
								predicate = method_13904(block, args[12]);
							}
						}
					}

					List<BlockInfo> list = Lists.newArrayList();
					List<BlockInfo> list1 = Lists.newArrayList();
					List<BlockInfo> list2 = Lists.newArrayList();
					Deque<BlockPos> deque = Lists.newLinkedList();
					BlockPos blockpos3 = new BlockPos(structureboundingbox1.minX - structureboundingbox.minX, structureboundingbox1.minY - structureboundingbox.minY, structureboundingbox1.minZ - structureboundingbox.minZ);

					for (int j = structureboundingbox.minZ; j <= structureboundingbox.maxZ; ++j)
					{
						for (int k = structureboundingbox.minY; k <= structureboundingbox.maxY; ++k)
						{
							for (int l = structureboundingbox.minX; l <= structureboundingbox.maxX; ++l)
							{
								BlockPos blockpos4 = new BlockPos(l, k, j);
								BlockPos blockpos5 = blockpos4.add(blockpos3);
								BlockState iblockstate = world.getBlockState(blockpos4);

								if ((!flag1 || iblockstate.getBlock() != Blocks.AIR) && (block == null || iblockstate.getBlock() == block && (predicate == null || predicate.apply(iblockstate))))
								{
									BlockEntity tileentity = world.getBlockEntity(blockpos4);

									if (tileentity != null)
									{
										NbtCompound nbttagcompound = tileentity.toNbt(new NbtCompound());
										list1.add(new BlockInfo(blockpos5, iblockstate, nbttagcompound));
										deque.addLast(blockpos4);
									}
									else if (!iblockstate.isFullBlock() && !iblockstate.method_11730())
									{
										list2.add(new BlockInfo(blockpos5, iblockstate, null));
										deque.addFirst(blockpos4);
									}
									else
									{
										list.add(new BlockInfo(blockpos5, iblockstate, null));
										deque.addLast(blockpos4);
									}
								}
							}
						}
					}

					ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;

					if (flag)
					{
						for (BlockPos blockpos6 : deque)
						{
							WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos6, Blocks.AIR.getDefaultState(), null);
							BlockEntity tileentity1 = world.getBlockEntity(blockpos6);

							if (tileentity1 instanceof Inventory)
							{
								((Inventory)tileentity1).clear();
							}

							world.setBlockState(blockpos6, Blocks.BARRIER.getDefaultState(), 2 | (update?0:1024)); //carpet
						}

						for (BlockPos blockpos7 : deque)
						{
							world.setBlockState(blockpos7, Blocks.AIR.getDefaultState(), (update?3:131)); //carpet
						}
					}

					List<BlockInfo> list3 = Lists.newArrayList();
					list3.addAll(list);
					list3.addAll(list1);
					list3.addAll(list2);
					List<BlockInfo> list4 = Lists.reverse(list3);

					for (BlockInfo commandclone$staticclonedata : list4)
					{
						WorldEditBridge.recordBlockEdit(worldEditPlayer, world, commandclone$staticclonedata.pos, commandclone$staticclonedata.blockState, commandclone$staticclonedata.nbt);
						BlockEntity tileentity2 = world.getBlockEntity(commandclone$staticclonedata.pos);

						if (tileentity2 instanceof Inventory)
						{
							((Inventory)tileentity2).clear();
						}

						world.setBlockState(commandclone$staticclonedata.pos, Blocks.BARRIER.getDefaultState(), 2 | (update?0:1024)); //carpet
					}

					int i = 0;

					for (BlockInfo commandclone$staticclonedata1 : list3)
					{
						if (world.setBlockState(commandclone$staticclonedata1.pos, commandclone$staticclonedata1.blockState, 2 | (update?0:1024))) //carpet
						{
							++i;
						}
					}
					for (BlockInfo commandclone$staticclonedata2 : list1)
					{
						BlockEntity tileentity3 = world.getBlockEntity(commandclone$staticclonedata2.pos);

						if (commandclone$staticclonedata2.nbt != null && tileentity3 != null)
						{
							commandclone$staticclonedata2.nbt.putInt("x", commandclone$staticclonedata2.pos.getX());
							commandclone$staticclonedata2.nbt.putInt("y", commandclone$staticclonedata2.pos.getY());
							commandclone$staticclonedata2.nbt.putInt("z", commandclone$staticclonedata2.pos.getZ());
							tileentity3.fromNbt(commandclone$staticclonedata2.nbt);
							tileentity3.markDirty();
						}

						world.setBlockState(commandclone$staticclonedata2.pos, commandclone$staticclonedata2.blockState, 2);
					}

					/*carpet mod */
					if (update)
					{
						/*carpet mod end EXTRA INDENTATION START*/
						for (BlockInfo commandclone$staticclonedata3 : list4)
						{
							world.method_8531(commandclone$staticclonedata3.pos, commandclone$staticclonedata3.blockState.getBlock(), false);
						}

						List<ScheduledTick> list5 = world.getScheduledTicks(structureboundingbox, false);

						if (list5 != null)
						{
							for (ScheduledTick nextticklistentry : list5)
							{
								if (structureboundingbox.contains(nextticklistentry.pos))
								{
									BlockPos blockpos8 = nextticklistentry.pos.add(blockpos3);
									world.scheduleTick(blockpos8, nextticklistentry.getBlock(),
											(int)(nextticklistentry.time - world.getLevelProperties().getTime()), nextticklistentry.priority);
								}
							}
						}
					} //carpet mod back extra indentation

					if (i <= 0)
					{
						throw new CommandException("commands.clone.failed");
					}
					else
					{
						sender.setStat(CommandStats.Type.AFFECTED_BLOCKS, i);
						run(sender, this, "commands.clone.success", i);
					}
				}
				else
				{
					throw new CommandException("commands.clone.outOfWorld");
				}
			}
		}
	}

	/**
	 * Get a list of options for when the user presses the TAB key
	 */
	public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos)
	{
		if (args.length > 0 && args.length <= 3)
		{
			return method_10707(args, 0, targetPos);
		}
		else if (args.length > 3 && args.length <= 6)
		{
			return method_10707(args, 3, targetPos);
		}
		else if (args.length > 6 && args.length <= 9)
		{
			return method_10707(args, 6, targetPos);
		}
		else if (args.length == 10)
		{
			return method_2894(args, "replace", "masked", "filtered");
		}
		else if (args.length == 11)
		{
			return method_2894(args, "normal", "force", "move", "noupdate", "force_noupdate", "move_noupdate");
		}
		else
		{
			return args.length == 12 && "filtered".equals(args[9]) ? method_10708(args, Block.REGISTRY.getKeySet()) : Collections.emptyList();
		}
	}

	static class BlockInfo
	{
		public final BlockPos pos;
		public final BlockState blockState;
		public final NbtCompound nbt;

		public BlockInfo(BlockPos posIn, BlockState stateIn, NbtCompound compoundIn)
		{
			this.pos = posIn;
			this.blockState = stateIn;
			this.nbt = compoundIn;
		}
	}
}