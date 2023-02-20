package carpet.commands;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import carpet.logging.logHelpers.DebugLogHelper;
import carpet.utils.extensions.CameraPlayer;

import net.minecraft.command.IncorrectUsageException;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.effect.StatusEffect;

public class CommandGMS extends CommandCarpetBase
{
    @Override
    public String getCommandName()
    {
        return "s";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return "commands.gamemode.usage";
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandCameramode", sender)) return;
        if (args.length > 0)
        {
            throw new IncorrectUsageException(getUsageTranslationKey(sender));
        }
        else
        {
            ServerPlayerEntity entityplayer = getAsPlayer(sender);
            setPlayerToSurvival(server, entityplayer,false);
        }
    }

    public static void setPlayerToSurvival(MinecraftServer server, ServerPlayerEntity entityplayer, boolean alwaysPutPlayerInSurvival) {
        GameMode gametype = server.method_3026();
        if(entityplayer.interactionManager.getGameMode() != GameMode.SURVIVAL) {
            DebugLogHelper.invisDebug(() -> "s1: " + entityplayer.world.playerEntities.contains(entityplayer));
            if(((CameraPlayer) entityplayer).moveToStoredCameraData() &&  !alwaysPutPlayerInSurvival) {
                DebugLogHelper.invisDebug(() -> "s7: " + entityplayer.world.playerEntities.contains(entityplayer));
                return;
            }
            entityplayer.fallDistance = 0;
            DebugLogHelper.invisDebug(() -> "s5: " + entityplayer.world.playerEntities.contains(entityplayer));
            if(gametype != GameMode.SPECTATOR) {
                entityplayer.method_3170(gametype);
            } else {
                entityplayer.method_3170(GameMode.SURVIVAL);
            }
            if(!((CameraPlayer) entityplayer).hadNightvision()) entityplayer.removeStatusEffect(StatusEffects.NIGHT_VISION);
            DebugLogHelper.invisDebug(() -> "s6: " + entityplayer.world.playerEntities.contains(entityplayer));
        }
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos)
    {
        return Collections.emptyList();
    }
}
