package carpet.commands;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import carpet.mixin.accessors.SurfaceChunkGeneratorAccessor;
import carpet.mixin.accessors.ServerChunkProviderAccessor;
import carpet.mixin.accessors.WorldAccessor;
import carpet.utils.Messenger;
import carpet.utils.extensions.ExtendedEndChunkGenerator;
import carpet.utils.extensions.ExtendedWorld;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import carpet.CarpetSettings;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entities;
import net.minecraft.entity.living.mob.MobCategory;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.entity.living.mob.hostile.ZombieVillagerEntity;
import net.minecraft.server.ChunkHolder;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.text.LiteralText;
import net.minecraft.util.WeightedPicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkGenerator;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WorldChunkSection;
import net.minecraft.world.gen.chunk.OverworldChunkGenerator;

public class CommandRNG extends CommandCarpetBase {
	@Override
	public String getName() {
		return "rng";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "rng <rule> <value>";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandRNG", sender)) return;
		if (args.length <= 0) throw new IncorrectUsageException(getUsage(sender));
		if ("seed".equalsIgnoreCase(args[0])) {
			try {
				World world = sender.getSourceWorld();

				ChunkGenerator gen = ((ServerChunkProviderAccessor) world.getChunkSource()).getChunkGenerator();

				if (gen instanceof OverworldChunkGenerator) {
					int x;
					int z;
					if (args.length < 3) {
						x = sender.getSourceBlockPos().getX() / 16;
						z = sender.getSourceBlockPos().getZ() / 16;
						if (x < 0) {
							x--;
						}
						if (z < 0) {
							z--;
						}
					} else {
						try {
							x = Integer.parseInt(args[1]);
							z = Integer.parseInt(args[2]);
						} catch (NumberFormatException e) {
							x = sender.getSourceBlockPos().getX() / 16;
							z = sender.getSourceBlockPos().getZ() / 16;
							if (x < 0) {
								x--;
							}
							if (z < 0) {
								z--;
							}
						}
					}

					((SurfaceChunkGeneratorAccessor) gen).getWoodlandMansionFeature().place(world, x, z, null);
					sendSuccess(sender, this, String.format("Seed at chunk coords: %d %d seed: %d", x, z, ((ExtendedWorld) world).getRandSeed()));
				}
			} catch (Exception e) {
				System.out.println("some error at seed");
				e.printStackTrace(System.out);
			}
        } else if ("setSeed".equalsIgnoreCase(args[0])) {
			try {
				CarpetSettings.setSeed = Long.parseLong(args[1]);
				sendSuccess(sender, this, "RNG seed set to " + args[1]);
			} catch (Exception e) {
				sendSuccess(sender, this, "rng setSeed <seed>, default seed to 0 for turning off RNG.");
			}
		} else if ("getMobspawningChunk".equalsIgnoreCase(args[0])) {
			long seed;
			int chunkNum;
			int playerSize;
			try {
				chunkNum = Integer.parseInt(args[2]);
			} catch (Exception e) {
				chunkNum = 1;
			}
			try {
				playerSize = Integer.parseInt(args[3]);
			} catch (Exception e) {
				playerSize = 1;
			}
			try {
				seed = Long.parseLong(args[1]);
			} catch (NumberFormatException e) {
				sendSuccess(sender, this, "rng getMobspawningChunk <seed> <chunkNum> <playersHashSize>");
				return;
			}

			World world = sender.getSourceWorld();
			if (world instanceof ServerWorld && sender instanceof PlayerEntity) {
				displayMobSpawningChunkInfo((ServerWorld) world, sender, seed, chunkNum, playerSize);
			}
		} else if ("randomtickedChunksCount".equalsIgnoreCase(args[0])) {
			World world = sender.getSourceWorld();
			int iters = Integer.MAX_VALUE;
			int chunkCount = 0;
			int x = 0;
			int z = 0;

			if (args.length == 2) {
				try {
					iters = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
				}
			}

			if (world instanceof ServerWorld) {
				int count = 0;
				for (Iterator<WorldChunk> iterator = ((ServerWorld) world).getChunkMap().getTickingChunks(); iterator.hasNext() && chunkCount < iters;
						world.profiler.pop()) {
					WorldChunk chunk = iterator.next();
					if (iters != Integer.MAX_VALUE) {
						chunkCount++;
						x = chunk.chunkX;
						z = chunk.chunkZ;
					}
					count++;
				}
				if (iters != Integer.MAX_VALUE) {
					sendSuccess(sender, this, String.format("Number of chunks till chunk index from player position: %d at chunk coord: (%d,%d)", count, x, z));
				} else {
					sendSuccess(sender, this, String.format("Number of chunks around the player random ticking: %d", count));
				}
			}
		} else if ("randomtickedBlocksInRange".equalsIgnoreCase(args[0])) {
			World world = sender.getSourceWorld();

			int x = 0;
			int z = 0;
			int iters = Integer.MAX_VALUE;
			int chunkCount = 0;
			boolean check = false;

			if (args.length == 2) {
				try {
					iters = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
				}
			} else if (args.length == 3) {
				try {
					check = true;
					x = Integer.parseInt(args[1]);
					z = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
				}
			}

			if (world instanceof ServerWorld) {
				int count = 0;
				for (Iterator<WorldChunk> iterator = ((ServerWorld) world).getChunkMap().getTickingChunks(); iterator.hasNext() && chunkCount < iters;
						world.profiler.pop()) {
					WorldChunk chunk = iterator.next();
					if (iters != Integer.MAX_VALUE) {
						chunkCount++;
						x = chunk.chunkX;
						z = chunk.chunkZ;
					}
					if (!check || (x == chunk.chunkX && z == chunk.chunkZ)) {
						for (WorldChunkSection section : chunk.getSections()) {
							if (section != WorldChunk.EMPTY) {
								for (int i = 0; i < 16; ++i) {
									for (int j = 0; j < 16; ++j) {
										for (int k = 0; k < 16; ++k) {
											Block block = section.getBlockState(i, j, k).getBlock();

											if (block != Blocks.AIR) {
												if (rndInfluencingBlock(block)) {
													count++;
												}
											}
										}
									}
								}
							}
						}

						if (check) {
							sendSuccess(sender, this, String.format("Number of rand influencing blocks: %d", count));
							return;
						}
					}
				}
				if (!check) {
					if (iters != Integer.MAX_VALUE) {
						sendSuccess(sender, this, String.format("Number of rand influencing blocks: %d  until hitting chunk: (%d,%d)", count, x, z));
					} else {
						sendSuccess(sender, this, String.format("Number of rand influencing blocks: %d", count));
					}
				} else {
					sendSuccess(sender, this, "Chosen location is not in loaded random ticked area.");
				}
			}
		} else if ("getLCG".equalsIgnoreCase(args[0])) {
			ArrayList<Object> strings = new ArrayList<>();
			for (World world : server.worlds) {
				int seed = ((WorldAccessor) world).getUpdateLCG();
				Messenger.m(sender,
						"w " + world.dimension.getType().toString() + ": ",
						"c " + seed,
						"^w Dimension LCG at beginning of game loop : " + seed,
						"?/rng setLCG " + world.dimension.getType().toString() + " " + seed
				);
			}
		} else if ("setLCG".equalsIgnoreCase(args[0])) {
			if (args.length == 3) {
				for (World world : server.worlds) {
					if (world.dimension.getType().toString().equals(args[1])) {
						try {
							((WorldAccessor) world).setUpdateLCG(Integer.parseInt(args[2]));
							sendSuccess(sender, this, world.dimension.getType() + " LCG changed to " + args[2]);
						} catch (Exception e) {
						}
					}
				}
			}
		} else if ("getEndChunkSeed".equalsIgnoreCase(args[0])) {
			ServerChunkCache chunkProvider = server.getWorld(1).getChunkSource();
			ExtendedEndChunkGenerator chunkGeneratorEnd = ((ExtendedEndChunkGenerator) ((ServerChunkProviderAccessor) chunkProvider).getChunkGenerator());
			long seed = chunkGeneratorEnd.getLastRandomSeed();
			if (chunkGeneratorEnd.wasRandomSeedUsed()) {
				sender.sendMessage(new LiteralText("The ChunkGeneratorEnd seed " + seed + " was already used for population and is currently random!"));
			} else {
				sender.sendMessage(new LiteralText("Current ChunkGeneratorEnd seed: " + seed));
			}
		} else if ("setEndChunkSeed".equalsIgnoreCase(args[0])) {
			if (args.length == 2 || (args.length == 3 && "once".equalsIgnoreCase(args[2]))) {
				ServerChunkCache chunkProvider = server.getWorld(1).getChunkSource();
				ExtendedEndChunkGenerator chunkGeneratorEnd = ((ExtendedEndChunkGenerator) ((ServerChunkProviderAccessor) chunkProvider).getChunkGenerator());
				long seed = parseLong(args[1]);
				chunkGeneratorEnd.setRandomSeedUsed(false);
				chunkGeneratorEnd.setEndChunkSeed(seed);
				if (args.length == 2) {
					CarpetSettings.endChunkSeed = seed;
					sender.sendMessage(new LiteralText("Set the ChunkGeneratorEnd seed to: " + CarpetSettings.endChunkSeed));
				} else if (args.length == 3) {
					chunkGeneratorEnd.setEndChunkSeed(seed);
					sender.sendMessage(new LiteralText("Set the ChunkGeneratorEnd seed once to: " + chunkGeneratorEnd.getLastRandomSeed()));
				}
			} else {
				throw new IncorrectUsageException("/rng setEndChunkSeed <seed> [once]");
			}
		}
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
		if (!CarpetSettings.commandRNG) {
			return Collections.<String>emptyList();
		}
		if (args.length == 1) {
			return suggestMatching(args,
					"seed",
					"setSeed",
					"getMobspawningChunk",
					"randomtickedChunksCount",
					"randomtickedBlocksInRange",
					"logWeather",
					"getLCG",
					"setLCG",
					"getEndChunkSeed",
					"setEndChunkSeed"
			);
		}
		if (args.length >= 2) {
			if ("seed".equalsIgnoreCase(args[0])) {
				BlockPos pos = sender.getSourceBlockPos();
				return getTabComplet(args, 1, pos);
			}
			if ("setSeed".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "0");
			}
			if ("getMobspawningChunk".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "1");
			}
			if ("randomtickedBlocksInRange".equalsIgnoreCase(args[0])) {
				BlockPos pos = sender.getSourceBlockPos();
				return getTabComplet(args, 1, pos);
			}
			if ("logWeather".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "true", "false");
			}
			if ("setLCG".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "OVERWORLD", "NETHER", "THE_END");
			}
			if ("setEndChunkSeed".equalsIgnoreCase(args[0]) && args.length == 3) {
				return suggestMatching(args, "once");
			}
		}

		return Collections.<String>emptyList();
	}

	public static List<String> getTabComplet(String[] inputArgs, int index, @Nullable BlockPos lookedPos) {
		if (lookedPos == null) {
			return Lists.newArrayList("~");
		} else {
			int i = inputArgs.length - 1;
			String s;

			if (i == index) {
				s = Integer.toString(lookedPos.getX() / 16);
			} else {
				if (i != index + 1) {
					return Collections.<String>emptyList();
				}

				s = Integer.toString(lookedPos.getZ() / 16);
			}

			return Lists.newArrayList(s);
		}
	}

	private boolean rndInfluencingBlock(Block block) {
		return Blocks.LAVA == block || Blocks.SAPLING == block || Blocks.GLASS == block || Blocks.VINE == block || Blocks.CARROTS == block ||
				Blocks.WHEAT == block || Blocks.BEETROOTS == block || Blocks.FIRE == block || Blocks.COCOA == block || Blocks.POTATOES == block;

	}

	public void displayMobSpawningChunkInfo(ServerWorld worldServerIn, CommandSource sender, long seed, int chunkNum, int playerSize) {
		PlayerEntity entityplayer = (PlayerEntity) sender;
		Set<ChunkPos> eligibleChunksForSpawning = Sets.newHashSet();

		for (int i = 0; i < (playerSize * 225); i++) {
			ChunkPos chunkpos = new ChunkPos(0, i);
			eligibleChunksForSpawning.add(chunkpos);
		}
		eligibleChunksForSpawning.clear();

		Random rand = new Random();
		rand.setSeed(seed ^ 0x5DEECE66DL);

		int j = MathHelper.floor(entityplayer.x / 16.0D);
		int k = MathHelper.floor(entityplayer.z / 16.0D);
		int l = 8;

		for (int i1 = -8; i1 <= 8; ++i1) {
			for (int j1 = -8; j1 <= 8; ++j1) {
				boolean flag = i1 == -8 || i1 == 8 || j1 == -8 || j1 == 8;
				ChunkPos chunkpos = new ChunkPos(i1 + j, j1 + k);

				if (!eligibleChunksForSpawning.contains(chunkpos)) {
					if (!flag && worldServerIn.getWorldBorder().contains(chunkpos)) {
						ChunkHolder playerchunkmapentry = worldServerIn.getChunkMap().getChunk(chunkpos.x, chunkpos.z);

						if (playerchunkmapentry != null && playerchunkmapentry.isPopulated()) {
							eligibleChunksForSpawning.add(chunkpos);
						}
					}
				}
			}
		}

		BlockPos.Mutable blockpos$mutableblockpos = new BlockPos.Mutable();
		BlockPos blockpos1 = worldServerIn.getSpawnPoint();
		MobCategory enumcreaturetype = MobCategory.MONSTER;
		int chunkCount = 0;
		StringBuffer sb = new StringBuffer();

		for (ChunkPos chunkpos1 : eligibleChunksForSpawning) {
			BlockPos blockpos = getRandomChunkPosition(worldServerIn, rand, chunkpos1.x, chunkpos1.z);
			int k1 = blockpos.getX();
			int l1 = blockpos.getY();
			int i2 = blockpos.getZ();
			BlockState iblockstate = worldServerIn.getBlockState(blockpos);
			chunkCount++;

			if (chunkNum == chunkCount) {
				sb.append("Spawning chunk ").append(chunkNum).append(" coords: ").append(chunkpos1.x).append(",").append(chunkpos1.z).append("\n");
				sb.append("Block spawning point: ").append(blockpos).append("\n");
				int l2 = k1;
				int i3 = l1;
				int j3 = i2;
				int k3 = 6;
				Biome.SpawnEntry biome$spawnlistentry = null;
				EntityData ientitylivingdata = null;

				for (int i4 = 0; i4 < 4; ++i4) {
					l2 += rand.nextInt(6) - rand.nextInt(6);
					i3 += rand.nextInt(1) - rand.nextInt(1);
					j3 += rand.nextInt(6) - rand.nextInt(6);
					blockpos$mutableblockpos.set(l2, i3, j3);
					float f = (float) l2 + 0.5F;
					float f1 = (float) j3 + 0.5F;

					if (!worldServerIn.isPlayerWithinRange(f, i3, f1, 24.0D) && blockpos1.squaredDistanceTo(f, i3, f1) >= 576.0D) {
						if (biome$spawnlistentry == null) {
							biome$spawnlistentry = getSpawnListEntryForTypeAt(worldServerIn, rand, enumcreaturetype, blockpos$mutableblockpos);

							if (biome$spawnlistentry == null) {
								break;
							} else {
								try {
									sb.append("MobType: " + Entities.getName(biome$spawnlistentry.type.getConstructor(World.class).newInstance(worldServerIn)) +
											"\n");
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}

						rand.nextFloat();

						sb.append("Spawning position").append(i4 + 1).append(": ").append(blockpos$mutableblockpos).append("\n");

						MobEntity entityliving;

						try {
							entityliving = biome$spawnlistentry.type.getConstructor(World.class).newInstance(worldServerIn);
						} catch (Exception exception) {
							return;
						}

						if (entityliving instanceof ZombieVillagerEntity) {
							int profession = rand.nextInt(6);

							sb.append("Zomble profession: ").append(profession(profession)).append("\n");
						}
					}
				}
				sendSuccess(sender, this, sb.toString());
				return;
			}
		}
	}

	private String profession(int type) {
		switch (type) {
			case 0:
				return "Farmer";

			case 1:
				return "Librarian";

			case 2:
				return "Priest";

			case 3:
				return "Smith";

			case 4:
				return "Butcher";

			case 5:
			default:
				return "Nitwit";
		}
	}

	public Biome.SpawnEntry getSpawnListEntryForTypeAt(ServerWorld worldServerIn, Random rand, MobCategory creatureType, BlockPos pos) {
		List<Biome.SpawnEntry> list = worldServerIn.getChunkSource().getSpawnEntries(creatureType, pos);
		return list != null && !list.isEmpty() ? WeightedPicker.pick(rand, list) : null;
	}

	public static boolean canCreatureTypeSpawnAtLocation(MobEntity.SpawnEnvironment spawnPlacementTypeIn, World worldIn, BlockPos pos) {
		// MobSpawnerHelper.isSpawnable
		if (!worldIn.getWorldBorder().contains(pos)) {
			return false;
		}
		BlockState iblockstate = worldIn.getBlockState(pos);

		if (spawnPlacementTypeIn == MobEntity.SpawnEnvironment.IN_WATER) {
			return iblockstate.getMaterial() == Material.WATER && worldIn.getBlockState(pos.down()).getMaterial() == Material.WATER &&
					!worldIn.getBlockState(pos.up()).isConductor();
		} else {
			BlockPos blockpos = pos.down();

			if (!worldIn.getBlockState(blockpos).isFullBlock()) {
				return false;
			} else {
				Block block = worldIn.getBlockState(blockpos).getBlock();
				boolean flag = block != Blocks.BEDROCK && block != Blocks.BARRIER;
				return flag && isValidEmptySpawnBlock(iblockstate) && isValidEmptySpawnBlock(worldIn.getBlockState(pos.up()));
			}
		}
	}

	public static boolean isValidEmptySpawnBlock(BlockState state) {
		// MobSpawnerHelper.method_11496
		return !state.blocksAmbientLight() && !state.isSignalSource() && !state.getMaterial().isLiquid() && !AbstractRailBlock.isRail(state);
	}

	private static BlockPos getRandomChunkPosition(World worldIn, Random rand, int x, int z) {
		WorldChunk chunk = worldIn.getChunkAt(x, z);
		int i = x * 16 + rand.nextInt(16);
		int j = z * 16 + rand.nextInt(16);
		int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
		int l = rand.nextInt(k > 0 ? k : chunk.getHighestSectionOffset() + 16 - 1);
		return new BlockPos(i, l, j);
	}
}
