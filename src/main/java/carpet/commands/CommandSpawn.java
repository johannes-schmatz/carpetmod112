package carpet.commands;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.entity.living.mob.MobCategory;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import carpet.utils.SpawnReporter;
import carpet.helpers.TickSpeed;

import java.util.ArrayList;

public class CommandSpawn extends CommandCarpetBase {
	@Override
	public String getUsage(CommandSource sender) {
		return "Usage:\nspawn list <X> <Y> <Z>\nspawn entities/rates <... | passive | hostile | ambient | water>\nspawn mobcaps <set <num>, nether, overworld, end>\nspawn tracking <.../stop/start/hostile/passive/water/ambient>\nspawn mocking <true/false>";
	}

	@Override
	public String getName() {
		return "spawn";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandSpawn", sender)) return;
		if (args.length == 0) {
			throw new IncorrectUsageException(getUsage(sender));
		}
		World world = sender.getSourceWorld();
		if ("list".equalsIgnoreCase(args[0])) {
			BlockPos blockpos = parseBlockPos(sender, args, 1, false);
            if (world.isChunkLoaded(blockpos)) {
                msg(sender, SpawnReporter.report(blockpos, world));
                return;
            } else {
                throw new CommandException("commands.setblock.outOfWorld");
            }
		} else if ("tracking".equalsIgnoreCase(args[0])) {
			if (args.length == 1) {
				msg(sender, SpawnReporter.tracking_report(world));
				return;
			} else if ("start".equalsIgnoreCase(args[1])) {
				if (SpawnReporter.track_spawns == 0L) {
					BlockPos lsl = null;
					BlockPos usl = null;
					if (args.length == 8) {
						BlockPos a = parseBlockPos(sender, args, 2, false);
						BlockPos b = parseBlockPos(sender, args, 5, false);
						lsl = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
						usl = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
					} else if (args.length != 2) {
						sendSuccess(sender, this, "Wrong syntax: /spawn tracking start <X1 Y1 Z1 X2 Y2 Z2>");
						return;
					}
					SpawnReporter.reset_spawn_stats(false);
					SpawnReporter.track_spawns = (long) world.getServer().getTicks();
					SpawnReporter.lower_spawning_limit = lsl;
					SpawnReporter.upper_spawning_limit = usl;
					sendSuccess(sender, this, "Spawning tracking started.");
				} else {
					sendSuccess(sender, this, "You are already tracking spawning.");
				}
			} else if ("stop".equalsIgnoreCase(args[1])) {
				msg(sender, SpawnReporter.tracking_report(world));
				SpawnReporter.reset_spawn_stats(false);
				SpawnReporter.track_spawns = 0L;
				SpawnReporter.lower_spawning_limit = null;
				SpawnReporter.upper_spawning_limit = null;
				sendSuccess(sender, this, "Spawning tracking stopped.");
			} else {
				msg(sender, SpawnReporter.recent_spawns(world, args[1]));
			}
			return;
		} else if ("test".equalsIgnoreCase(args[0])) {
			String counter = null;
			long warp = 72000;
			if (args.length >= 2) {

				warp = parseLong(args[1], 20, 720000);
				if (args.length >= 3) {
					counter = args[2];
				}
			}
			//stop tracking
			SpawnReporter.reset_spawn_stats(false);
			//start tracking
			SpawnReporter.track_spawns = (long) server.getTicks();
			//counter reset
			if (counter == null) {
				HopperCounter.resetAll(server);
			} else {
				HopperCounter hopperCounter = HopperCounter.getCounter(counter);
				if (hopperCounter != null) hopperCounter.reset(server);
			}

			// tick warp 0
			TickSpeed.tickrate_advance(null, 0, null, null);
			// tick warp given player
			PlayerEntity player = null;
			if (sender instanceof PlayerEntity) {
				player = (PlayerEntity) sender;
			}
			TickSpeed.tickrate_advance(player, warp, null, sender);
			sendSuccess(sender, this, String.format("Started spawn test for %d ticks", warp));
			return;

		} else if ("mocking".equalsIgnoreCase(args[0])) {
			boolean doMock = parseBoolean(args[1]);
			if (doMock) {
				SpawnReporter.initialize_mocking();
				sendSuccess(sender, this, "Mock spawns started, Spawn statistics reset");
			} else {
				SpawnReporter.stop_mocking();
				sendSuccess(sender, this, "Normal mob spawning, Spawn statistics reset");
			}
			return;
		} else if ("rates".equalsIgnoreCase(args[0])) {
			if (args.length >= 2 && "reset".equalsIgnoreCase(args[1])) {
				for (MobCategory s : SpawnReporter.spawn_tries.keySet()) {
					SpawnReporter.spawn_tries.put(s, 1);
				}
			} else if (args.length >= 3) {
				String str = args[1];
				int num = parseInt(args[2], 0, 1000);
				SpawnReporter.spawn_tries.put(SpawnReporter.get_creature_type_from_code(str), num);
			}
			if (sender instanceof ServerPlayerEntity) {
				msg(sender, SpawnReporter.print_general_mobcaps(world));
			}
			return;
		} else if ("mobcaps".equalsIgnoreCase(args[0])) {
			if (args.length == 1) {
				msg(sender, SpawnReporter.print_general_mobcaps(world));
				return;
			}
			if (args.length > 1) {
				switch (args[1]) {
					case "set":
						if (args.length > 2) {
							int desired_mobcap = parseInt(args[2], 0);
							double desired_ratio = (double) desired_mobcap / MobCategory.MONSTER.getCap();
							SpawnReporter.mobcap_exponent = 4.0 * Math.log(desired_ratio) / Math.log(2.0);
							sendSuccess(sender, this, String.format("Mobcaps for hostile mobs changed to %d, other groups will follow", desired_mobcap));
							return;
						}
						msg(sender, SpawnReporter.print_general_mobcaps(world));
						return;
					case "overworld":
						msg(sender, SpawnReporter.printMobcapsForDimension(world, 0, "overworld"));
						return;
					case "nether":
						msg(sender, SpawnReporter.printMobcapsForDimension(world, -1, "nether"));
						return;
					case "end":
						msg(sender, SpawnReporter.printMobcapsForDimension(world, 1, "the end"));
						return;
				}
			}


		} else if ("entities".equalsIgnoreCase(args[0])) {
			if (args.length == 1) {
				msg(sender, SpawnReporter.print_general_mobcaps(world));
				return;
			} else {
				msg(sender, SpawnReporter.printEntitiesByType(args[1], world));
				return;
			}
		}
		throw new IncorrectUsageException(getUsage(sender));

	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
		if (!CarpetSettings.commandSpawn) {
			return Collections.emptyList();
		}
		if (args.length == 1) {
			return suggestMatching(args, "list", "mocking", "tracking", "mobcaps", "rates", "entities", "test");
		}
		if ("list".equalsIgnoreCase(args[0]) && args.length <= 4) {
			return suggestCoordinate(args, 1, pos);
		}
		if (args.length == 2) {
			if ("tracking".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "start", "stop", "hostile", "passive", "ambient", "water");
			}
			if ("mocking".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "true", "false");
			}
			if ("entities".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "hostile", "passive", "ambient", "water");
			}
			if ("rates".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "reset", "hostile", "passive", "ambient", "water");
			}
			if ("mobcaps".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "set", "nether", "overworld", "end");
			}
			if ("test".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "24000", "72000");
			}
		}
		if ("test".equalsIgnoreCase(args[0]) && (args.length == 3)) {
			List<String> lst = new ArrayList<>();
			for (DyeColor clr : DyeColor.values()) {
				lst.add(clr.getName());
			}
			String[] stockArr = new String[lst.size()];
			stockArr = lst.toArray(stockArr);
			return suggestMatching(args, stockArr);
		}
		if ("mobcaps".equalsIgnoreCase(args[0]) && "set".equalsIgnoreCase(args[1]) && (args.length == 3)) {
			return suggestMatching(args, "70");
		}
		if ("tracking".equalsIgnoreCase(args[0]) && "start".equalsIgnoreCase(args[1]) && args.length > 2 && args.length <= 5) {
			return suggestCoordinate(args, 2, pos);
		}
		if ("tracking".equalsIgnoreCase(args[0]) && "start".equalsIgnoreCase(args[1]) && args.length > 5 && args.length <= 8) {
			return suggestCoordinate(args, 5, pos);
		}
		return Collections.emptyList();
	}
}
