package carpet.commands;

import carpet.CarpetSettings;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandResults;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import carpet.helpers.CapturedDrops;
import carpet.worldedit.WorldEditBridge;
import org.jetbrains.annotations.Nullable;

public class CommandFeel extends Command {

	public String getName() {
		return "feel";
	}

	public int getRequiredPermissionLevel() {
		return 2;
	}


	public String getUsage(CommandSource sender) {
		return "commands.fill.usage";
	}

	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (args.length < 7) {
			throw new IncorrectUsageException("commands.fill.usage");
		} else {
			sender.addResult(CommandResults.Type.AFFECTED_BLOCKS, 0);
			BlockPos startInput = parseBlockPos(sender, args, 0, false);
			BlockPos endInput = parseBlockPos(sender, args, 3, false);
			Block block = parseBlock(sender, args[6]);
			BlockState state;

			if (args.length >= 8) {
				state = parseBlockState(block, args[7]);
			} else {
				state = block.defaultState();
			}

			BlockPos start = new BlockPos(
					Math.min(startInput.getX(), endInput.getX()),
					Math.min(startInput.getY(), endInput.getY()),
					Math.min(startInput.getZ(), endInput.getZ())
			);
			BlockPos end = new BlockPos(
					Math.max(startInput.getX(), endInput.getX()),
					Math.max(startInput.getY(), endInput.getY()),
					Math.max(startInput.getZ(), endInput.getZ())
			);
			int i = (end.getX() - start.getX() + 1) * (end.getY() - start.getY() + 1) * (end.getZ() - start.getZ() + 1);

			if (start.getY() >= 0 && end.getY() < 256) {
				World world = sender.getSourceWorld();

				NbtCompound nbttagcompound = new NbtCompound();
				boolean flag = false;

				if (args.length >= 10 && block.hasBlockEntity()) {
					String s = parseString(args, 9);

					try {
						nbttagcompound = StringNbtReader.parse(s);
						flag = true;
					} catch (NbtException nbtexception) {
						throw new CommandException("commands.fill.tagError", nbtexception.getMessage());
					}
				}

				FillType mode;
				try {
					mode = args.length <= 8 ? FillType.REPLACE : FillType.valueOf(args[8].toUpperCase(Locale.ROOT));
				} catch (IllegalArgumentException e) {
					mode = FillType.REPLACE;
				}
				Block toReplace = null;
				Predicate<BlockState> toReplacePredicate = state_ -> true;
				if ((mode == FillType.REPLACE || mode == FillType.NOUPDATE) && args.length > 9) {
					toReplace = parseBlock(sender, args[9]);
					if (args.length > 10 && !args[10].equals("-1") && !args[10].equals("*")) {
						toReplacePredicate = parseBlockStatePredicate(toReplace, args[10]);
					}
				}

				ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
				NbtCompound worldEditTag = flag ? nbttagcompound : null;

				List<BlockPos> list = Lists.newArrayList();
				int i666 = 0;

				for (int l = start.getZ(); l <= end.getZ(); ++l) {
					for (int i1 = start.getY(); i1 <= end.getY(); ++i1) {
						for (int j1 = start.getX(); j1 <= end.getX(); ++j1) {
							BlockPos blockpos4 = new BlockPos(j1, i1, l);

							if (args.length >= 9) {
								if (mode != FillType.OUTLINE && mode != FillType.HOLLOW) {
									if (mode == FillType.DESTROY) {
										WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos4, Blocks.AIR.defaultState(), worldEditTag);
										CapturedDrops.setCapturingDrops(true);
										world.breakBlock(blockpos4, true);
										CapturedDrops.setCapturingDrops(false);
										for (ItemEntity drop : CapturedDrops.getCapturedDrops())
											WorldEditBridge.recordEntityCreation(worldEditPlayer, world, drop);
										CapturedDrops.clearCapturedDrops();
									} else if (mode == FillType.KEEP) {
										if (!world.isAir(blockpos4)) {
											continue;
										}
									} else if ((mode == FillType.REPLACE || mode == FillType.NOUPDATE) && !block.hasBlockEntity() && args.length > 9) {
										BlockState state1 = world.getBlockState(blockpos4);
										if (state1.getBlock() != toReplace || !toReplacePredicate.test(state1)) {
											continue;
										}
									}
								} else if (j1 != start.getX() && j1 != end.getX() && i1 != start.getY() && i1 != end.getY() &&
										l != start.getZ() && l != end.getZ()) {
									if (mode == FillType.HOLLOW) {
										WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos4, Blocks.AIR.defaultState(), worldEditTag);
										world.setBlockState(blockpos4, Blocks.AIR.defaultState(), 2);
										list.add(blockpos4);
									}

									continue;
								}
							}

							WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos4, state, worldEditTag);
							BlockEntity tileentity1 = world.getBlockEntity(blockpos4);

							if (tileentity1 instanceof Inventory) {
								((Inventory) tileentity1).clear();
							}

							if (world.setBlockState(blockpos4, state, 2 | (mode == FillType.NOUPDATE ? CarpetSettings.NO_UPDATES : 0))) //CM
							{
								list.add(blockpos4);
								++i666;

								if (flag) {
									BlockEntity tileentity = world.getBlockEntity(blockpos4);

									if (tileentity != null) {
										nbttagcompound.putInt("x", blockpos4.getX());
										nbttagcompound.putInt("y", blockpos4.getY());
										nbttagcompound.putInt("z", blockpos4.getZ());
										tileentity.readNbt(nbttagcompound);
									}
								}
							}
						}
					}
				}

				/*carpet mod */
				if (mode != FillType.NOUPDATE) {
					/*carpet mod end EXTRA INDENT*/
					for (BlockPos blockpos5 : list) {
						Block block2 = world.getBlockState(blockpos5).getBlock();
						world.onBlockChanged(blockpos5, block2, false);
					}
				} //carpet mod back extra indentation

				if (i666 <= 0) {
					throw new CommandException("commands.fill.failed");
				} else {
					sender.addResult(CommandResults.Type.AFFECTED_BLOCKS, i666);
					sendSuccess(sender, this, "commands.fill.success", i666);
				}
			} else {
				throw new CommandException("commands.fill.outOfWorld");
			}
		}
	}


	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length > 0 && args.length <= 3) {
			return suggestCoordinate(args, 0, targetPos);
		} else if (args.length > 3 && args.length <= 6) {
			return suggestCoordinate(args, 3, targetPos);
		} else if (args.length == 7) {
			return suggestMatching(args, Block.REGISTRY.keySet());
		} else if (args.length == 9) {
			return suggestMatching(args, "replace", "destroy", "keep", "hollow", "outline", "noupdate", "replace_noupdate");
		} else {
			return args.length == 10 && ("replace".equalsIgnoreCase(args[8]) || "noupdate".equalsIgnoreCase(args[8])) ?
					suggestMatching(args, Block.REGISTRY.keySet()) : Collections.emptyList();
		}
	}

	enum FillType {
		REPLACE, DESTROY, KEEP, HOLLOW, OUTLINE, NOUPDATE;
	}
}