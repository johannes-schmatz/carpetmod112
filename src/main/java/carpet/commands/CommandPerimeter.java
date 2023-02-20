package carpet.commands;

import carpet.utils.Messenger;
import carpet.utils.PerimeterDiagnostics;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandPerimeter extends CommandCarpetBase
{
    @Override
    public String getCommandName()
    {
        return "perimetercheck";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return "/perimetercheck <X> <Y> <Z> <target_entity?>";
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandPerimeterInfo", sender)) return;
        if (args.length < 1)
        {
            throw new IncorrectUsageException(getUsageTranslationKey(sender));
        }
        else
        {
            String s;
            BlockPos blockpos = sender.getBlockPos();
            Vec3d vec3d = sender.getPos();
            double d0 = vec3d.x;
            double d1 = vec3d.y;
            double d2 = vec3d.z;
            if (args.length >= 3)
            {
                d0 = parseDouble(d0, args[0], true);
                d1 = parseDouble(d1, args[1], false);
                d2 = parseDouble(d2, args[2], true);
                blockpos = new BlockPos(d0, d1, d2);
            }
            World world = sender.getWorld();
            NbtCompound nbttagcompound = new NbtCompound();
            MobEntity entityliving = null;
            if (args.length >= 4)
            {
                s = args[3];
                nbttagcompound.putString("id", s);
                entityliving = (MobEntity) ThreadedAnvilChunkStorage.method_11782(nbttagcompound, world, d0, d1+2, d2, true);
                if (entityliving == null)
                {
                    throw new CommandException("Failed to test entity");
                }
            }
            PerimeterDiagnostics.Result res = PerimeterDiagnostics.countSpots((ServerWorld) world, blockpos, entityliving);
            if (sender instanceof PlayerEntity)
            {
                Messenger.m(sender, "w Spawning spaces around ",Messenger.tp("b",blockpos));
            }
            run(sender, this, "Spawn spaces:");
            run(sender, this, String.format("  potential in-liquid: %d",res.liquid));
            run(sender, this, String.format("  potential on-ground: %d",res.ground));
            if (entityliving != null)
            {
                run(sender, this, String.format("  %s: %d",entityliving.getName().asUnformattedString(),res.specific));
                if (sender instanceof PlayerEntity)
                {
                    res.samples.forEach(bp -> Messenger.m(sender, "w   ", Messenger.tp("w", bp)));
                }
                else
                {
                    res.samples.forEach(bp -> run(sender, this, String.format("    [ %d, %d, %d ]", bp.getX(),bp.getY(),bp.getZ())));
                }
                entityliving.remove();
            }
        }
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 4)
        {
            return method_10708(args, EntityType.getIdentifiers());
        }
        else
        {
            return args.length > 0 && args.length <= 3 ? method_10707(args, 0, pos) : Collections.emptyList();
        }
    }
}

