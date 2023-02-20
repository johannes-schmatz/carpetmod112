package carpet.commands;

import java.util.Collections;
import java.util.List;

import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class CommandFillBiome extends CommandCarpetBase
{
    @Override
    public String getCommandName()
    {
        return "fillbiome";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return "/fillbiome <from: x z> <to: x z> <biome>";
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandFillBiome", sender))
            return;
        
        if (args.length < 5)
            throw new IncorrectUsageException(getUsageTranslationKey(sender));
        
        int x1 = (int) Math.round(getCoordinate(sender.getBlockPos().getX(), args[0], false).getAmount());
        int z1 = (int) Math.round(getCoordinate(sender.getBlockPos().getZ(), args[1], false).getAmount());
        int x2 = (int) Math.round(getCoordinate(sender.getBlockPos().getX(), args[2], false).getAmount());
        int z2 = (int) Math.round(getCoordinate(sender.getBlockPos().getZ(), args[3], false).getAmount());
        
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);
        
        Biome biome;
        try
        {
            biome = Biome.getBiomeFromIndex(Integer.parseInt(args[4]));
        }
        catch (NumberFormatException e)
        {
            biome = Biome.REGISTRY.get(new Identifier(args[4]));
        }
        if (biome == null)
        {
            throw new CommandException("Unknown biome " + args[4]);
        }
        byte biomeId = (byte) (Biome.getBiomeIndex(biome) & 255);
        
        ServerWorld world = (ServerWorld) sender.getWorld();
        if (!world.isRegionLoaded(new BlockPos(minX, 0, minZ), new BlockPos(maxX, 0, maxZ)))
        {
            throw new CommandException("commands.fill.outOfWorld");
        }
        
        BlockPos.Mutable pos = new BlockPos.Mutable();
        
        for (int x = minX; x <= maxX; x++)
        {
            for (int z = minZ; z <= maxZ; z++)
            {
                Chunk chunk = world.getChunk(pos.set(x, 0, z));
                chunk.getBiomeArray()[(x & 15) | (z & 15) << 4] = biomeId;
                chunk.setModified();
            }
        }
        
        int minChunkX = Math.floorDiv(minX, 16);
        int maxChunkX = Math.floorDiv(maxX, 16);
        int minChunkZ = Math.floorDiv(minZ, 16);
        int maxChunkZ = Math.floorDiv(maxZ, 16);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
        {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
            {
                ChunkPlayerManager entry = world.getPlayerWorldManager().method_12811(chunkX, chunkZ);
                if (entry != null)
                {
                    Chunk chunk = entry.getChunk();
                    if (chunk != null)
                    {
                        ChunkDataS2CPacket packet = new ChunkDataS2CPacket(chunk, 65535);
                        for (ServerPlayerEntity player : ((PlayerChunkMapEntryAccessor) entry).getPlayers())
                            player.networkHandler.sendPacket(packet);
                    }
                }
            }
        }
        
        run(sender, this, ((maxX - minX + 1) * (maxZ - minZ + 1)) + " biome blocks changed");
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1 || args.length == 3)
        {
            if (targetPos == null)
                return method_2894(args, "~");
            else
                return method_2894(args, String.valueOf(targetPos.getX()));
        }
        else if (args.length == 2 || args.length == 4)
        {
            if (targetPos == null)
                return method_2894(args, "~");
            else
                return method_2894(args, String.valueOf(targetPos.getZ()));
        }
        else if (args.length == 5)
        {
            return method_10708(args, Biome.REGISTRY.getKeySet());
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
