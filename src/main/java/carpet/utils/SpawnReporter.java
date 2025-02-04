package carpet.utils;

import java.util.*;

import carpet.mixin.accessors.MobEntityAccessor;
import carpet.mixin.accessors.WeightingWeightAccessor;
import com.google.common.collect.AbstractIterator;

import net.minecraft.entity.*;
import net.minecraft.entity.living.mob.MobCategory;
import net.minecraft.entity.living.mob.MobEnvironment;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.NaturalSpawner;
import net.minecraft.world.World;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.entity.living.mob.passive.animal.tamable.OcelotEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.text.Text;

import java.lang.Math;

public class SpawnReporter {
	public static boolean mock_spawns = false;

	public static Long track_spawns = 0L;
	public static final HashMap<Integer, HashMap<MobCategory, Pair<Integer, Integer>>> mobcaps = new HashMap<>();
	public static final HashMap<MobCategory, HashMap<String, Long>> spawn_stats = new HashMap<>();
	public static double mobcap_exponent = 0.0D;

	public static final HashMap<String, Long> spawn_attempts = new HashMap<>();
	public static final HashMap<String, Long> overall_spawn_ticks = new HashMap<>();
	public static final HashMap<String, Long> spawn_ticks_full = new HashMap<>();
	public static final HashMap<String, Long> spawn_ticks_fail = new HashMap<>();
	public static final HashMap<String, Long> spawn_ticks_succ = new HashMap<>();
	public static final HashMap<String, Long> spawn_ticks_spawns = new HashMap<>();
	public static final HashMap<String, Long> spawn_cap_count = new HashMap<>();

	public static class SpawnPos {
		public String mob;
		public BlockPos pos;

		public SpawnPos(String mob, BlockPos pos) {
			this.mob = mob;
			this.pos = pos;
		}
	}

	public static final HashMap<MobCategory, EvictingQueue<SpawnPos, Integer>> spawned_mobs = new HashMap<>();
	public static final HashMap<MobCategory, Integer> spawn_tries = new HashMap<>();
	public static BlockPos lower_spawning_limit = null;
	public static BlockPos upper_spawning_limit = null;

	static {
		reset_spawn_stats(true);
	}

	public static void registerSpawn(MobEntity el, MobCategory type) {
		registerSpawn(el, type, Entities.getName(el), 1L);
	}

	public static void registerSpawn(MobEntity el, MobCategory type, String mob, long value) {
		BlockPos pos = new BlockPos(el);
		if (lower_spawning_limit != null) {
			if (!((lower_spawning_limit.getX() <= pos.getX() && pos.getX() <= upper_spawning_limit.getX()) &&
					(lower_spawning_limit.getY() <= pos.getY() && pos.getY() <= upper_spawning_limit.getY()) &&
					(lower_spawning_limit.getZ() <= pos.getZ() && pos.getZ() <= upper_spawning_limit.getZ()))) {
				return;
			}
		}

		long count = spawn_stats.get(type).getOrDefault(mob, 0L);
		spawn_stats.get(type).put(mob, count + value);
		spawned_mobs.get(type).put(new SpawnPos(mob, pos), 1);
	}


	public static List<Text> printMobcapsForDimension(World world, int dim, String name) {
		List<Text> lst = new ArrayList<>();
		lst.add(Messenger.s(null, String.format("Mobcaps for %s:", name)));
		for (MobCategory enumcreaturetype : MobCategory.values()) {
			String type_code = String.format("%s", enumcreaturetype);
			Pair<Integer, Integer> stat = mobcaps.get(dim).getOrDefault(enumcreaturetype, new Pair<>(0, 0));
			int cur = stat.getLeft();
			int max = stat.getRight();
			int rounds = spawn_tries.getOrDefault(enumcreaturetype, 1);
			lst.add(Messenger.m(null,
					String.format("w   %s: ", type_code),
					(cur + max == 0) ? "g -/-" : String.format("%s %d/%d", (cur >= max) ? "r" : ((cur >= 8 * max / 10) ? "y" : "l"), cur, max),
					(rounds == 1) ? "w " : String.format("fi  (%d rounds/tick)", rounds)
			));
		}
		return lst;
	}

	public static List<Text> print_general_mobcaps(World world) {
		String name = world.dimension.getType().getKey();
		int did = world.dimension.getType().getId();
		return printMobcapsForDimension(world, did, name);
	}

	public static List<Text> recent_spawns(World world, String creature_type_code) {

		MobCategory creature_type = get_creature_type_from_code(creature_type_code);
		List<Text> lst = new ArrayList<>();
		if ((track_spawns == 0L)) {
			lst.add(Messenger.s(null, "Spawn tracking not started"));
			return lst;
		}
		if (creature_type == null) {
			lst.add(Messenger.s(null, String.format("Incorrect creature type: %s", creature_type_code)));
			return lst;
		}

		String type_code = get_type_string(creature_type);

		lst.add(Messenger.s(null, String.format("Recent %s spawns:", type_code)));
		for (SpawnPos entry : spawned_mobs.get(creature_type).keySet()) {
			lst.add(Messenger.m(null, String.format("w  - %s ", entry.mob), Messenger.tp("wb", entry.pos)));
		}

		if (lst.size() == 1) {
			lst.add(Messenger.s(null, " - Nothing spawned yet, sorry."));
		}
		return lst;

	}

	public static List<Text> show_mobcaps(BlockPos pos, World worldIn) {
		DyeColor under = WoolTool.getWoolColorAtPosition(worldIn, pos.down());
		if (under == null) {
			if (track_spawns > 0L) {
				return tracking_report(worldIn);
			} else {
				return print_general_mobcaps(worldIn);
			}
		}
		String creature_type = get_type_code_from_wool_code(under);
		if (creature_type != null) {
			if (track_spawns > 0L) {
				return recent_spawns(worldIn, creature_type);
			} else {
				return printEntitiesByType(creature_type, worldIn);

			}

		}
		if (track_spawns > 0L) {
			return tracking_report(worldIn);
		} else {
			return print_general_mobcaps(worldIn);
		}

	}

	public static String get_type_code_from_wool_code(DyeColor color) {
		switch (color) {
			case RED:
				return "hostile";
			case GREEN:
				return "passive";
			case BLUE:
				return "water";
			case BROWN:
				return "ambient";
		}
		return null;
	}

	public static MobCategory get_creature_type_from_code(String type_code) {
		if ("hostile".equalsIgnoreCase(type_code)) {
			return MobCategory.MONSTER;
		} else if ("passive".equalsIgnoreCase(type_code)) {
			return MobCategory.CREATURE;
		} else if ("water".equalsIgnoreCase(type_code)) {
			return MobCategory.WATER_CREATURE;
		} else if ("ambient".equalsIgnoreCase(type_code)) {
			return MobCategory.AMBIENT;
		}
		return null;
	}


	public static String get_type_string(MobCategory typ) {
		return String.format("%s", typ);
	}

	public static String get_creature_code_from_string(String str) {
		return get_type_string(get_creature_type_from_code(str));
	}

	public static List<Text> printEntitiesByType(String creature_type_code, World worldIn) //Class<?> entityType)
	{
		MobCategory typ = get_creature_type_from_code(creature_type_code);
		List<Text> lst = new ArrayList<>();
		if (typ == null) {
			lst.add(Messenger.m(null, String.format("r Incorrect creature type: %s", creature_type_code)));
			return lst;
		}
		Class<?> cls = typ.getDeclaringClass();
		lst.add(Messenger.s(null, String.format("Loaded entities for %s class:", get_type_string(typ))));
		for (Entity entity : worldIn.entities) {
			if ((!(entity instanceof MobEntity) || !((MobEntity) entity).isPersistent()) && cls.isAssignableFrom(entity.getClass())) {
				lst.add(Messenger.m(null, "w  - ", Messenger.tp("w", entity.x, entity.y, entity.z), "w  : " + Entities.getName(entity)));
			}
		}
		if (lst.size() == 1) {
			lst.add(Messenger.s(null, " - Empty."));
		}
		return lst;
	}

	public static void initialize_mocking() {
		reset_spawn_stats(false);
		mock_spawns = true;

	}

	public static void stop_mocking() {
		reset_spawn_stats(false);
		mock_spawns = false;

	}

	public static void reset_spawn_stats(boolean full) {
		spawn_stats.clear();
		spawned_mobs.clear();
		for (MobCategory enumcreaturetype : MobCategory.values()) {
			String type_code = String.format("%s", enumcreaturetype);
			if (full) {
				spawn_tries.put(enumcreaturetype, 1);
			}
			for (String suffix : new String[]{"", " (N)", " (E)"}) {
				String code = type_code + suffix;
				overall_spawn_ticks.put(code, 0L);
				spawn_attempts.put(code, 0L);
				spawn_ticks_full.put(code, 0L);
				spawn_ticks_fail.put(code, 0L);
				spawn_ticks_succ.put(code, 0L);
				spawn_ticks_spawns.put(code, 0L);
				spawn_cap_count.put(code, 0L);
			}

			spawn_stats.put(enumcreaturetype, new HashMap<>());
			spawned_mobs.put(enumcreaturetype, new EvictingQueue<>());
		}
		mobcaps.put(-1, new HashMap<>());
		mobcaps.put(0, new HashMap<>());
		mobcaps.put(1, new HashMap<>());
		track_spawns = 0L;
	}

	public static List<Text> tracking_report(World worldIn) {
		List<Text> report = new ArrayList<>();
		if (track_spawns == 0L) {
			report.add(Messenger.m(null, "w Spawn tracking disabled, type '", "wi /spawn tracking start", "/spawn tracking start", "w ' to enable"));
			return report;
		}
		Long duration = (long) worldIn.getServer().getTicks() - track_spawns;
		report.add(Messenger.m(null, "bw --------------------"));
		String simulated = mock_spawns ? "[SIMULATED] " : "";
		String location = (lower_spawning_limit != null) ? String.format("[in (%d, %d, %d)x(%d, %d, %d)]",
				lower_spawning_limit.getX(),
				lower_spawning_limit.getY(),
				lower_spawning_limit.getZ(),
				upper_spawning_limit.getX(),
				upper_spawning_limit.getY(),
				upper_spawning_limit.getZ()
		) : "";
		report.add(Messenger.s(null, String.format("%sSpawn statistics %s: for %.1f min", simulated, location, (duration / 72000.0) * 60)));
		for (MobCategory enumcreaturetype : MobCategory.values()) {
			String type_code = String.format("%s", enumcreaturetype);
			boolean there_are_mobs_to_list = false;
			for (String world_code : new String[]{"", " (N)", " (E)"}) {
				String code = type_code + world_code;
				if (spawn_ticks_spawns.get(code) > 0L) {
					there_are_mobs_to_list = true;
					double hours = overall_spawn_ticks.get(code) / 72000.0;
					report.add(Messenger.s(null, String.format(" > %s (%.1f min), %.1f m/t, {%.1f%%F / %.1f%%- / %.1f%%+}; %.2f s/att",
							code,
							60 * hours,
							(1.0D * spawn_cap_count.get(code)) / spawn_attempts.get(code),
							(100.0D * spawn_ticks_full.get(code)) / spawn_attempts.get(code),
							(100.0D * spawn_ticks_fail.get(code)) / spawn_attempts.get(code),
							(100.0D * spawn_ticks_succ.get(code)) / spawn_attempts.get(code),
							(1.0D * spawn_ticks_spawns.get(code)) / (spawn_ticks_fail.get(code) + spawn_ticks_succ.get(code))
					)));
				}
			}
			if (there_are_mobs_to_list) {
				for (String creature_name : spawn_stats.get(type_code).keySet()) {
					report.add(Messenger.s(null, String.format("   - %s: %d spawns, %d per hour",
							creature_name,
							spawn_stats.get(type_code).get(creature_name),
							(72000 * spawn_stats.get(type_code).get(creature_name) / duration)
					)));
				}
			}
		}
		return report;
	}


	public static void killEntity(MobEntity entity) {
		if (entity.hasVehicle()) {
			entity.getVehicle().remove();
		}
		if (entity.hasPassengers()) {
			for (Entity e : entity.getPassengers()) {
				e.remove();
			}
		}
		if (entity instanceof OcelotEntity) {
			for (Entity e : entity.getSourceWorld().getEntities(OcelotEntity.class, entity.getShape())) {
				e.remove();
			}
		}
		entity.remove();
	}

	public static List<Text> report(BlockPos pos, World worldIn) {
		List<Text> rep = new ArrayList<>();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		WorldChunk chunk = worldIn.getChunk(pos);
		int max_chunk = MathHelper.roundUp(chunk.getHeight(new BlockPos(x, 0, z)) + 1, 16);
		int lc = max_chunk > 0 ? max_chunk : chunk.getHighestSectionOffset() + 16 - 1;
		String where = (y >= lc) ? "above" : "below";
		rep.add(Messenger.s(null,
				String.format("Maximum spawn Y value for (%+d, %+d) is %d. You are %d blocks %s it", x, z, lc, MathHelper.abs(y - lc), where)
		));
		rep.add(Messenger.s(null, "Spawns:"));
		for (MobCategory enumcreaturetype : MobCategory.values()) {
			String type_code = String.format("%s", enumcreaturetype).substring(0, 3);
			List<Biome.SpawnEntry> lst = ((ServerChunkCache) worldIn.getChunkSource()).getSpawnEntries(enumcreaturetype, pos);
			if (lst != null && !lst.isEmpty()) {
				for (Biome.SpawnEntry animal : lst) {
					boolean canspawn = NaturalSpawner.isValidSpawnPos(MobEnvironment.get(animal.type), worldIn, pos);
					int will_spawn = -1;
					//boolean fits = false;
					//boolean fits1 = false;

					MobEntity entityliving;
					try {
						entityliving = animal.type.getConstructor(World.class).newInstance(worldIn);
					} catch (Exception exception) {
						exception.printStackTrace();
						return rep;
					}

					boolean fits_true = false;
					boolean fits_false = false;

					if (canspawn) {
						will_spawn = 0;
						for (int attempt = 0; attempt < 50; ++attempt) {
							float f = (float) x + 0.5F;
							float f1 = (float) z + 0.5F;
							entityliving.refreshPositionAndAngles((double) f, (double) y, (double) f1, worldIn.random.nextFloat() * 360.0F, 0.0F);
							boolean fits1 = entityliving.canSpawn();

							for (int i = 0; i < 20; ++i) {
								if (entityliving.canSpawn()) {
									will_spawn += 1;
								}
							}
							entityliving.initialize(worldIn.getLocalDifficulty(new BlockPos(entityliving)), null);
							// the code invokes onInitialSpawn after getCanSpawnHere
							boolean fits = fits1 && entityliving.canSpawn();
							if (fits) {
								fits_true = true;
							} else {
								fits_false = true;
							}

							killEntity(entityliving);

							try {
								entityliving = animal.type.getConstructor(new Class[]{World.class}).newInstance(new Object[]{worldIn});
							} catch (Exception exception) {
								exception.printStackTrace();
								return rep;
							}
						}
					}

					String creature_name = Entities.getName(entityliving);
					String pack_size =
							String.format("%d", entityliving.getLimitPerChunk());//String.format("%d-%d", animal.minGroupCount, animal.maxGroupCount);
					int weight = ((WeightingWeightAccessor) animal).getWeight();
					if (canspawn) {
						String c = (fits_true && will_spawn > 0) ? "e" : "gi";
						rep.add(Messenger.m(null,
								String.format("%s %s: %s (%d), %s, can: ", c, type_code, creature_name, weight, pack_size),
								"l YES",
								c + " , fit: ",
								((fits_true && fits_false) ? "y YES and NO" : (fits_true ? "l YES" : "r NO")),
								c + " , will: ",
								((will_spawn > 0) ? "l " : "r ") + Math.round((double) will_spawn) / 10 + "%"
						));
					} else {
						rep.add(Messenger.m(null, String.format("gi %s: %s (%d), %s, can: ", type_code, creature_name, weight, pack_size), "n NO"));
					}
					killEntity(entityliving);
				}
			}
		}
		return rep;
	}

	// Added optimized despawn mobs causing netlag by Luflosi CARPET-XCOM
	public static boolean willImmediatelyDespawn(MobEntity entity) {
		MobEntityAccessor accessor = (MobEntityAccessor) entity;
		if (accessor.invokeCanImmediatelyDespawn() || entity.isPersistent()) return false;
		World world = entity.world;
		boolean playerInDimension = false;
		for (PlayerEntity playerEntity : world.players) {
			if (!playerEntity.isSpectator()) {
				playerInDimension = true;
				double distanceSq = playerEntity.getSquaredDistanceTo(entity.x, entity.y, entity.z);
				if (distanceSq <= 128.0 * 128.0) {
					return false;
				}
			}
		}
		return playerInDimension;
	}

	public static Iterator<ChunkPos> createChunkIterator(Set<ChunkPos> chunks, MobCategory category, Runnable onEnd) {
		return new AbstractIterator<ChunkPos>() {
			int tries = spawn_tries.getOrDefault(category, 1);
			Iterator<ChunkPos> orig;

			@Override
			protected ChunkPos computeNext() {
				while (orig == null || !orig.hasNext()) {
					if (tries-- == 0) {
						onEnd.run();
						return endOfData();
					}
					orig = chunks.iterator();
				}
				return orig.next();
			}
		};
	}
}