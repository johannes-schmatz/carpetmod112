package carpet.utils;

import carpet.CarpetSettings;
import carpet.mixin.accessors.ThreadedAnvilChunkStorageAccessor;
import carpet.mixin.accessors.ServerChunkProviderAccessor;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.chunk.ServerChunkCache;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.storage.AnvilChunkStorage;

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.DeflaterOutputStream;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;

public class ChunkLoading {
	public static final ThreadLocal<ServerPlayerEntity> INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY = new ThreadLocal<>();
	public static final LongSet droppedChunksSet_new = new LongOpenHashSet();

	public static void queueUnload113(ServerWorld world, ServerChunkCache chunkproviderserver, WorldChunk chunkIn) {
		if (world.dimension.canChunkUnload(chunkIn.chunkX, chunkIn.chunkZ)) {
			droppedChunksSet_new.add(Long.valueOf(ChunkPos.toLong(chunkIn.chunkX, chunkIn.chunkZ)));
			chunkIn.removed = true;
		}
	}

	public static List<String> test_save_chunks(ServerWorld server, BlockPos pos, boolean verbose) {

		ServerChunkCache serverChunkCache = server.getChunkSource();

		if (serverChunkCache.canSave()) {

			serverChunkCache.save(true);

			ChunkMap chunkMap = server.getChunkMap();

			for (WorldChunk chunk : Lists.newArrayList(serverChunkCache.getLoadedChunks())) {
				if (chunk != null && !chunkMap.hasChunk(chunk.chunkX, chunk.chunkZ)) {
					serverChunkCache.scheduleUnload(chunk);
				}
			}
			return ChunkLoading.tick_reportive_no_action(server, pos, verbose);
		}
		List<String> rep = new ArrayList<String>();
		rep.add("Saving is disabled on the server");
		return rep;
	}


	public static List<String> test_save_chunks_113(ServerWorld server, BlockPos pos, boolean verbose) {

		ServerChunkCache serverChunkCache = server.getChunkSource();

		if (serverChunkCache.canSave()) {

			serverChunkCache.save(true);

			ChunkMap chunkMap = server.getChunkMap();

			for (WorldChunk chunk : Lists.newArrayList(serverChunkCache.getLoadedChunks())) {
				if (chunk != null && !chunkMap.hasChunk(chunk.chunkX, chunk.chunkZ)) {
					queueUnload113(server, serverChunkCache, chunk);
				}
			}
			List<String> rep = ChunkLoading.tick_reportive_no_action_113(server, pos, verbose);
			return rep;
		}
		List<String> rep = new ArrayList<String>();
		rep.add("Saving is disabled on the server");
		return rep;
	}


	public static int getCurrentHashSize(ServerWorld server) {
		ServerChunkCache serverChunkCache = server.getChunkSource();
		try {
			Set<Long> droppedChunks = ((ServerChunkProviderAccessor) serverChunkCache).getDroppedChunks();
			Field field = droppedChunks.getClass().getDeclaredField("map"); // TODO: reflection!
			field.setAccessible(true);
			HashMap<?, ?> map = (HashMap<?, ?>) field.get(droppedChunks);
			Field tableField = map.getClass().getDeclaredField("table");
			tableField.setAccessible(true);
			Object[] table = (Object[]) tableField.get(map);
			if (table == null) return 2;
			return table.length;
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static int getCurrentHashSize_113() {
		try {
			// TODO: reflection!
			Field field = droppedChunksSet_new.getClass().getDeclaredField("n");
			field.setAccessible(true);
			int n = field.getInt(droppedChunksSet_new);
			return n;
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}


	public static int getChunkOrder(ChunkPos chpos, int hashsize) {
		//return HashMap_hash(Long.hashCode(ChunkPos.asLong(chpos.chunkXPos, chpos.chunkZPos)));
		try { // TODO: reflection!
			Method method = HashMap.class.getDeclaredMethod("hash", Object.class);
			method.setAccessible(true);
			return (Integer) method.invoke(null, Long.hashCode(ChunkPos.toLong(chpos.x, chpos.z))) & (hashsize - 1);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			CarpetSettings.LOG.error("You broke java");
			return -1;
		}
	}

	public static long get_chunk_order_113(ChunkPos chpos, int hashsize) {
		return (HashCommon.mix(ChunkPos.toLong(chpos.x, chpos.z))) & (hashsize - 1L);
	}

	public static List<String> check_unload_order(ServerWorld server, BlockPos pos, BlockPos pos1) {
		List<String> rep = new ArrayList<>();
		int size = getCurrentHashSize(server);
		if (pos1 == null) {
			ChunkPos chpos = new ChunkPos(pos);
			int o = getChunkOrder(chpos, size);
			rep.add("Chunks order of " + chpos + " is " + o + " / " + size);
			return rep;
		}
		ChunkPos chpos1 = new ChunkPos(pos);
		ChunkPos chpos2 = new ChunkPos(pos1);
		int minX = Math.min(chpos1.x, chpos2.x);
		int maxX = Math.max(chpos1.x, chpos2.x);
		int minZ = Math.min(chpos1.z, chpos2.z);
		int maxZ = Math.max(chpos1.z, chpos2.z);
		HashMap<Integer, Integer> stat = new HashMap<>();
		int total = 0;
		for (int chposx = minX; chposx <= maxX; chposx++) {
			for (int chposz = minZ; chposz <= maxZ; chposz++) {
				int o1 = getChunkOrder(new ChunkPos(chposx, chposz), size);
				int count = stat.containsKey(o1) ? stat.get(o1) : 0;
				stat.put(o1, count + 1);
				total++;
			}
		}
		rep.add("Counts of chunks with specific unload order / " + size + " (" + total + " total)");
		SortedSet<Integer> keys = new TreeSet<>(stat.keySet());
		for (int key : keys) {
			rep.add(" - order " + key + ": " + stat.get(key));

		}
		return rep;

	}

	public static List<String> check_unload_order_13(ServerWorld server, BlockPos pos, BlockPos pos1) {
		List<String> rep = new ArrayList<>();
		int size = getCurrentHashSize(server);
		if (pos1 == null) {
			ChunkPos chpos = new ChunkPos(pos);
			int o = (int) get_chunk_order_113(chpos, size);
			int olong = (int) get_chunk_order_113(chpos, 1 << 20);
			rep.add("Chunks order of " + chpos + " is " + o + " / " + size + ", or part of " + Integer.toBinaryString(olong));
			return rep;
		}
		ChunkPos chpos1 = new ChunkPos(pos);
		ChunkPos chpos2 = new ChunkPos(pos1);
		int minX = Math.min(chpos1.x, chpos2.x);
		int maxX = Math.max(chpos1.x, chpos2.x);
		int minZ = Math.min(chpos1.z, chpos2.z);
		int maxZ = Math.max(chpos1.z, chpos2.z);
		HashMap<Integer, Integer> stat = new HashMap<>();
		int total = 0;
		for (int chposx = minX; chposx <= maxX; chposx++) {
			for (int chposz = minZ; chposz <= maxZ; chposz++) {
				int o1 = (int) get_chunk_order_113(new ChunkPos(chposx, chposz), size);
				int count = stat.getOrDefault(o1, 0);
				stat.put(o1, count + 1);
				total++;
			}
		}
		rep.add("Counts of chunks with specific unload order / " + size + " (" + total + " total)");
		SortedSet<Integer> keys = new TreeSet<>(stat.keySet());
		for (int key : keys) {
			rep.add(" - order " + key + ": " + stat.get(key));

		}
		return rep;

	}

	public static List<String> protect_13(ServerWorld server, BlockPos pos, BlockPos pos1, String protect) {
		int size = getCurrentHashSize(server);
		String rest = protect.replaceAll("[\\D]", "");
		if (!(rest.equals(""))) {
			size = Integer.parseInt(rest);
			size = HashCommon.nextPowerOfTwo(size - 1);
		}
		if (size < 256) size = 256;
		List<String> rep = new ArrayList<>();
		if (pos1 == null) {
			pos1 = pos;
		}
		ChunkPos chpos1 = new ChunkPos(pos);
		ChunkPos chpos2 = new ChunkPos(pos1);
		int minX = Math.min(chpos1.x, chpos2.x);
		int maxX = Math.max(chpos1.x, chpos2.x);
		int minZ = Math.min(chpos1.z, chpos2.z);
		int maxZ = Math.max(chpos1.z, chpos2.z);
		int lenx = maxX - minX + 1;
		int lenz = maxZ - minZ + 1;
		HashMap<Integer, Integer> stat = new HashMap<>();
		int total = 0;
		for (int chposx = minX; chposx <= maxX; chposx++) {
			for (int chposz = minZ; chposz <= maxZ; chposz++) {
				int o1 = (int) get_chunk_order_113(new ChunkPos(chposx, chposz), size);
				int count = stat.containsKey(o1) ? stat.get(o1) : 0;
				stat.put(o1, count + 1);
				total++;
			}
		}
		rep.add("Counts of chunks with specific unload order out of " + size + " (" + total + " total chunks to protect)");
		SortedSet<Integer> keys = new TreeSet<>(stat.keySet());
		String chunklist = "";
		int order_to_protect = 1;
		for (int key : keys) {
			chunklist += String.format("%d:%d ", key, stat.get(key));
			order_to_protect = key;
		}
		rep.add(chunklist);

		int best_config_chunks = Integer.MAX_VALUE;
		int best_config_minx = 0;
		int best_config_maxx = 0;
		int best_config_minz = 0;
		int best_config_maxz = 0;
		int protect_limit = (int) (0.75 * size);
		for (int xdir = -1; xdir <= 1; xdir += 2) {
			for (int zdir = -1; zdir <= 1; zdir += 2) {

				for (int ex = 1; ex < protect_limit; ex++) {
					for (int ez = 1; ez < protect_limit; ez++) {
						if ((lenx + ex) * (lenz + ez) > protect_limit) break;
						if ((lenx + ex) * (lenz + ez) > best_config_chunks) break;
						int cminx = xdir < 0 ? minX - ex : minX;
						int cminz = zdir < 0 ? minZ - ez : minZ;
						int cmaxx = xdir < 0 ? maxX : maxX + ex;
						int cmaxz = zdir < 0 ? maxZ : maxZ + ez;
						int protecting_chunks = 0;
						for (int cx = cminx; cx <= cmaxx; cx++) {
							for (int cz = cminz; cz <= cmaxz; cz++) {
								int order = (int) get_chunk_order_113(new ChunkPos(cx, cz), size);
								if (order > order_to_protect) {
									protecting_chunks++;
								}
							}
						}
						if (protecting_chunks > 100) {
							int lx = cmaxx - cminx + 1;
							int lz = cmaxz - cminz + 1;
							if (lx * lz < best_config_chunks) {
								best_config_chunks = lx * lz;
								best_config_minx = cminx;
								best_config_maxx = cmaxx;
								best_config_minz = cminz;
								best_config_maxz = cmaxz;
								break;
							}

						}
					}
				}
			}
		}
		if (best_config_chunks != Integer.MAX_VALUE) {
			rep.add("you can protect this configuration with " + best_config_chunks + " chunks");
			rep.add("    from block: " + (best_config_minx << 4) + ", " + (best_config_minz << 4) + " to " + ((best_config_maxx << 4) + 15) + ", " +
					((best_config_maxz << 4) + 15));
		} else {
			rep.add("You can't protect this configuration with less than " + protect_limit + " chunks around");
		}
		return rep;
	}


	public static String stringify_chunk_id(ServerChunkCache provider, int index, Long olong, int size) {
		WorldChunk chunk = ((ServerChunkProviderAccessor) provider).getLoadedChunksMap().get(olong);

		return String.format(" - %4d: (%d, %d) at X %d, Z %d (order: %d / %d)",
				index + 1,
				chunk.chunkX,
				chunk.chunkZ,
				chunk.chunkX * 16 + 7,
				chunk.chunkZ * 16 + 7,
				ChunkLoading.getChunkOrder(new ChunkPos(chunk.chunkX, chunk.chunkZ), size),
				size
		);
	}

	public static String stringify_chunk_id_113(ServerChunkCache provider, int index, Long olong, int size) {
		WorldChunk chunk = ((ServerChunkProviderAccessor) provider).getLoadedChunksMap().get(olong);

		return String.format(" - %4d: (%d, %d) at X %d, Z %d (order: %d / %d)",
				index + 1,
				chunk.chunkX,
				chunk.chunkZ,
				chunk.chunkX * 16 + 7,
				chunk.chunkZ * 16 + 7,
				ChunkLoading.get_chunk_order_113(new ChunkPos(chunk.chunkX, chunk.chunkZ), size),
				size
		);
	}

	public static List<String> tick_reportive_no_action(ServerWorld world, BlockPos pos, boolean verbose) {
		ServerChunkCache provider = world.getChunkSource();
		List<String> rep = new ArrayList<>();
		int test_chunk_xpos = 0;
		int test_chunk_zpos = 0;
		if (pos != null) {
			test_chunk_xpos = pos.getX() >> 4;
			test_chunk_zpos = pos.getZ() >> 4;
		}
		int current_size = ChunkLoading.getCurrentHashSize(world);
		if (!world.isSaving) {
			Set<Long> droppedChunks = ((ServerChunkProviderAccessor) provider).getDroppedChunks();
			if (!droppedChunks.isEmpty()) {
				Iterator<Long> iterator = droppedChunks.iterator();
				List<Long> chunks_ids_order = new ArrayList<>();
				int selected_chunk = -1;
				int iti = 0;
				int i = 0;
				for (i = 0; iterator.hasNext(); iterator.remove()) {
					Long olong = iterator.next();
					WorldChunk chunk = ((ServerChunkProviderAccessor) provider).getLoadedChunksMap().get(olong);

					if (chunk != null && chunk.removed) {
						if (pos != null && chunk.chunkX == test_chunk_xpos && chunk.chunkZ == test_chunk_zpos) selected_chunk = i;
						chunks_ids_order.add(olong);
						++i;
					}
					++iti;
				}
				if (i != iti) {
					rep.add("There were some ineligible chunks to be unloaded,");
					rep.add("so the actual 100 chunk mark might not be accurate");
				}
				int total = chunks_ids_order.size();
				List<Integer> llll = Arrays.asList(0, 1, 2, -1, 97, 98, 99, -2, 100, 101, 102, -1, total - 3, total - 2, total - 1);
				if (total <= 100) {
					rep.add(String.format("There is only %d chunks to unload, all will be unloaded", total));
					llll = (total > 5) ? Arrays.asList(0, 1, -1, total - 2, total - 1) : Arrays.asList(-2);
				}
				if (verbose) {
					for (int iii = 0; iii < chunks_ids_order.size(); iii++) {
						rep.add(stringify_chunk_id(provider, iii, chunks_ids_order.get(iii), current_size));
					}
				} else {
					for (int idx : llll) {
						if (idx < 0) {
							if (idx == -1) {
								rep.add("    ....");
							} else {
								rep.add("--------");
							}
						} else {
							if (idx >= total) {
								continue;
							}
							rep.add(stringify_chunk_id(provider, idx, chunks_ids_order.get(idx), current_size));
						}
					}
				}
				if (pos != null) {

					if (selected_chunk == -1) {
						rep.add("Selected chunk was not marked for unloading");
					} else {
						rep.add(String.format("Selected chunk was %d on the list", selected_chunk + 1));
					}
				}
			} else {
				rep.add("There are no chunks to get unloaded");
			}
		} else {
			rep.add("Level Saving is disabled.");
		}
		return rep;
	}


	public static List<String> tick_reportive_no_action_113(ServerWorld world, BlockPos pos, boolean verbose) {
		ServerChunkCache provider = world.getChunkSource();
		List<String> rep = new ArrayList<>();
		int test_chunk_xpos = 0;
		int test_chunk_zpos = 0;
		if (pos != null) {
			test_chunk_xpos = pos.getX() >> 4;
			test_chunk_zpos = pos.getZ() >> 4;
		}
		if (!world.isSaving) {
			if (!droppedChunksSet_new.isEmpty()) {
				Iterator<Long> iterator = droppedChunksSet_new.iterator();
				List<Long> chunks_ids_order = new ArrayList<>();
				Map<Long, Integer> chunk_to_len = new HashMap<>();
				int selected_chunk = -1;
				int iti = 0;
				int i = 0;
				int current_size = ChunkLoading.getCurrentHashSize_113();
				for (i = 0; iterator.hasNext(); iterator.remove()) {

					Long olong = iterator.next();
					WorldChunk chunk = ((ServerChunkProviderAccessor) provider).getLoadedChunksMap().get(olong);
					((ServerChunkProviderAccessor) provider).getDroppedChunks().remove(olong);

					if (chunk != null && chunk.removed) {
						if (pos != null && chunk.chunkX == test_chunk_xpos && chunk.chunkZ == test_chunk_zpos) selected_chunk = i;
						chunks_ids_order.add(olong);
						chunk_to_len.put(olong, current_size);
						current_size = ChunkLoading.getCurrentHashSize_113();
						++i;
					}
					++iti;
				}
				if (i != iti) {
					rep.add("There were some ineligible chunks to be unloaded,");
					rep.add("so the actual 100 chunk mark might not be accurate");
				}
				int total = chunks_ids_order.size();
				List<Integer> llll = Arrays.asList(0, 1, 2, -1, 97, 98, 99, -2, 100, 101, 102, -1, total - 3, total - 2, total - 1);
				if (total <= 100) {
					rep.add(String.format("There is only %d chunks to unload, all will be unloaded", total));
					llll = (total > 5) ? Arrays.asList(0, 1, -1, total - 2, total - 1) : Arrays.asList(-2);
				}
				if (verbose) {
					for (int iii = 0; iii < chunks_ids_order.size(); iii++) {
						rep.add(stringify_chunk_id_113(provider, iii, chunks_ids_order.get(iii), chunk_to_len.get(chunks_ids_order.get(iii))));
					}
				} else {
					for (int idx : llll) {
						if (idx < 0) {
							if (idx == -1) {
								rep.add("    ....");
							} else {
								rep.add("--------");
							}
						} else {
							if (idx >= total) {
								continue;
							}
							rep.add(stringify_chunk_id_113(provider, idx, chunks_ids_order.get(idx), chunk_to_len.get(chunks_ids_order.get(idx))));
						}
					}
				}
				if (pos != null) {

					if (selected_chunk == -1) {
						rep.add("Selected chunk was not marked for unloading");
					} else {
						rep.add(String.format("Selected chunk was %d on the list", selected_chunk + 1));
						rep.add(stringify_chunk_id_113(provider,
								selected_chunk,
								chunks_ids_order.get(selected_chunk),
								chunk_to_len.get(chunks_ids_order.get(selected_chunk))
						));
					}
				}
			} else {
				rep.add("There are no chunks to get unloaded");
			}
		} else {
			rep.add("Level Saving is disabled.");
		}
		return rep;
	}

	public static int getSavedChunkSize(WorldChunk chunk) {
		NbtCompound chunkTag = new NbtCompound();
		NbtCompound levelTag = new NbtCompound();
		chunkTag.put("Level", levelTag);
		chunkTag.putInt("DataVersion", 1343);
		((ThreadedAnvilChunkStorageAccessor) getChunkLoader(chunk)).invokeWriteChunkToNBT(chunk, chunk.getWorld(), levelTag);
		CountingOutputStream counter = new CountingOutputStream(NullOutputStream.NULL_OUTPUT_STREAM);
		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(counter)));
			NbtIo.write(chunkTag, (DataOutput) out);
			out.flush();
			out.close();
		} catch (IOException ignore) {
		}
		return counter.getCount();
	}

	public static AnvilChunkStorage getChunkLoader(WorldChunk chunk) {
		return (AnvilChunkStorage) ((ServerChunkProviderAccessor) chunk.getWorld().getChunkSource()).getChunkLoader();
	}
}
