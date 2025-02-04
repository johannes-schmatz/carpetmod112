package carpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.living.mob.MobCategory;
import net.minecraft.entity.living.mob.MobEnvironment;
import net.minecraft.entity.living.mob.ambient.AmbientEntity;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.entity.living.mob.Monster;
import net.minecraft.entity.living.mob.water.WaterMobEntity;
import net.minecraft.entity.living.mob.passive.animal.AnimalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NaturalSpawner;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class PerimeterDiagnostics {
	public static class Result {
		public int liquid;
		public int ground;
		public int specific;
		public List<BlockPos> samples;

		Result() {
			samples = new ArrayList<>();
		}
	}

	private Biome.SpawnEntry sle;
	private final ServerWorld worldServer;
	private final MobCategory ctype;
	private final MobEntity el;

	private PerimeterDiagnostics(ServerWorld server, MobCategory ctype, MobEntity el) {
		this.sle = null;
		this.worldServer = server;
		this.ctype = ctype;
		this.el = el;
	}

	public static Result countSpots(ServerWorld worldserver, BlockPos epos, MobEntity el) {
		int eY = epos.getY();
		int eX = epos.getX();
		int eZ = epos.getZ();
		Result result = new Result();

		boolean add_water = false;
		boolean add_ground = false;
		MobCategory ctype = null;

		if (el != null) {
			if (el instanceof WaterMobEntity) {
				add_water = true;
				ctype = MobCategory.WATER_CREATURE;
			} else if (el instanceof AnimalEntity) {
				add_ground = true;
				ctype = MobCategory.CREATURE;
			} else if (el instanceof Monster) {
				add_ground = true;
				ctype = MobCategory.MONSTER;
			} else if (el instanceof AmbientEntity) {
				ctype = MobCategory.AMBIENT;
			}
		}
		PerimeterDiagnostics diagnostic = new PerimeterDiagnostics(worldserver, ctype, el);
		for (int x = -128; x <= 128; ++x) {
			for (int z = -128; z <= 128; ++z) {
				if (x * x + z * z > 128 * 128) // cut out a cyllinder first
				{
					continue;
				}
				for (int y = 0; y < 256; ++y) {
					if ((Math.abs(y - eY) > 128)) {
						continue;
					}
					int distsq = (x) * (x) + (eY - y) * (eY - y) + (z) * (z);
					if (distsq > 128 * 128 || distsq < 24 * 24) {
						continue;
					}
					BlockPos pos = new BlockPos(eX + x, y, eZ + z);

					BlockState iblockstate = worldserver.getBlockState(pos);
					BlockState iblockstate_down = worldserver.getBlockState(pos.down());
					BlockState iblockstate_up = worldserver.getBlockState(pos.up());

					if (iblockstate.getMaterial() == Material.WATER && iblockstate_down.getMaterial() == Material.WATER && !iblockstate_up.isConductor()) {
						result.liquid++;
						if (add_water && diagnostic.check_entity_spawn(pos)) {
							result.specific++;
							if (result.samples.size() < 10) {
								result.samples.add(pos);
							}
						}
					} else {
						if (iblockstate_down.isFullBlock()) {
							Block block = iblockstate_down.getBlock();
							boolean flag = block != Blocks.BEDROCK && block != Blocks.BARRIER;
							if (flag && NaturalSpawner.canSpawnInside(iblockstate) && NaturalSpawner.canSpawnInside(iblockstate_up)) {
								result.ground++;
								if (add_ground && diagnostic.check_entity_spawn(pos)) {
									result.specific++;
									if (result.samples.size() < 10) {
										result.samples.add(pos);
									}
								}
							}
						}
					}
				}
			}
		}
		return result;
	}


	private boolean check_entity_spawn(BlockPos pos) {
		if (sle == null || !worldServer.isValidSpawnEntry(ctype, sle, pos)) {
			sle = null;
			for (Biome.SpawnEntry sle : worldServer.getChunkSource().getSpawnEntries(ctype, pos)) {
				if (el.getClass() == sle.type) {
					this.sle = sle;
					break;
				}
			}
			if (sle == null || !worldServer.isValidSpawnEntry(ctype, sle, pos)) {
				return false;
			}
		}

		if (NaturalSpawner.isValidSpawnPos(MobEnvironment.get(sle.type), worldServer, pos)) {
			el.refreshPositionAndAngles(pos.getX() + 0.5F, (float) pos.getY(), pos.getZ() + 0.5F, 0.0F, 0.0F);
			return el.canSpawn() && el.canSpawn();
		}
		return false;
	}
}
