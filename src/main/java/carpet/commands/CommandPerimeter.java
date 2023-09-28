package carpet.commands;

import carpet.utils.Messenger;
import carpet.utils.PerimeterDiagnostics;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.entity.Entities;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.storage.AnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandPerimeter extends CommandCarpetBase {
	@Override
	public String getName() {
		return "perimetercheck";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "/perimetercheck <X> <Y> <Z> <target_entity?>";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandPerimeterInfo", sender)) return;
		if (args.length < 1) {
			throw new IncorrectUsageException(getUsage(sender));
		} else {
			String s;
			BlockPos blockpos = sender.getSourceBlockPos();
			Vec3d vec3d = sender.getSourcePos();
			double d0 = vec3d.x;
			double d1 = vec3d.y;
			double d2 = vec3d.z;
			if (args.length >= 3) {
				d0 = parseCoordinate(d0, args[0], true);
				d1 = parseCoordinate(d1, args[1], false);
				d2 = parseCoordinate(d2, args[2], true);
				blockpos = new BlockPos(d0, d1, d2);
			}
			World world = sender.getSourceWorld();
			NbtCompound nbttagcompound = new NbtCompound();
			MobEntity entityliving = null;
			if (args.length >= 4) {
				s = args[3];
				nbttagcompound.putString("id", s);
				entityliving = (MobEntity) AnvilChunkStorage.loadEntity(nbttagcompound, world, d0, d1 + 2, d2, true);
				if (entityliving == null) {
					throw new CommandException("Failed to test entity");
				}
			}
			PerimeterDiagnostics.Result res = PerimeterDiagnostics.countSpots((ServerWorld) world, blockpos, entityliving);
			if (sender instanceof PlayerEntity) {
				Messenger.m(sender, "w Spawning spaces around ", Messenger.tp("b", blockpos));
			}
			sendSuccess(sender, this, "Spawn spaces:");
			sendSuccess(sender, this, String.format("  potential in-liquid: %d", res.liquid));
			sendSuccess(sender, this, String.format("  potential on-ground: %d", res.ground));
			if (entityliving != null) {
				sendSuccess(sender, this, String.format("  %s: %d", entityliving.getDisplayName().buildString(), res.specific));
				if (sender instanceof PlayerEntity) {
					res.samples.forEach(bp -> Messenger.m(sender, "w   ", Messenger.tp("w", bp)));
				} else {
					res.samples.forEach(bp -> sendSuccess(sender, this, String.format("    [ %d, %d, %d ]", bp.getX(), bp.getY(), bp.getZ())));
				}
				entityliving.remove();
			}
		}
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
		if (args.length == 4) {
			return suggestMatching(args, Entities.getIds());
		} else {
			return args.length > 0 && args.length <= 3 ? suggestCoordinate(args, 0, pos) : Collections.emptyList();
		}
	}
}

