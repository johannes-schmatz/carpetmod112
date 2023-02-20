package carpet.commands;

import java.util.Collections;
import java.util.List;

import carpet.logging.LoggerRegistry;
import org.jetbrains.annotations.Nullable;

import carpet.CarpetSettings;
import carpet.mixin.accessors.EntityAccessor;
import carpet.utils.extensions.CameraPlayer;

import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;

public class CommandGMC extends CommandCarpetBase
{
    @Override
    public String getCommandName()
    {
        return "c";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return "commands.gamemode.usage";
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if (!command_enabled("commandCameramode", sender)) return;
        if (args.length > 0)
        {
            throw new IncorrectUsageException(getUsageTranslationKey(sender));
        }
        else
        {
            if (!CarpetSettings.commandCameramode)
            {
                run(sender, this, "Quick gamemode switching is disabled");
            }
            ServerPlayerEntity entityplayer = getAsPlayer(sender);
            if(entityplayer.isSpectator()) return;
            if(CarpetSettings.cameraModeSurvivalRestrictions && entityplayer.interactionManager.getGameMode() == GameMode.SURVIVAL) {
                List<HostileEntity> hostiles = sender.getWorld().getEntitiesInBox(HostileEntity.class, new Box(entityplayer.x - 8.0D, entityplayer.y - 5.0D,
                        entityplayer.z - 8.0D, entityplayer.x + 8.0D, entityplayer.y + 5.0D, entityplayer.z + 8.0D), mob -> mob.method_14129(entityplayer));
                StatusEffectInstance fireresist = entityplayer.getEffectInstance(StatusEffect.get("fire_resistance"));
                if(!entityplayer.onGround || entityplayer.method_13055() || (((EntityAccessor) entityplayer).getFireTicks() > 0 && (fireresist == null || fireresist.getDuration() < ((EntityAccessor) entityplayer).getFireTicks())) || entityplayer.getAir() != 300 || !hostiles.isEmpty()){
                    run(sender, this, "Restricted use to: on ground, not in water, not on fire, not flying/falling, not near hostile mobs.");
                    return;
                }
            }
            StatusEffect nightvision = StatusEffect.get("night_vision");
            boolean hasNightvision = entityplayer.getEffectInstance(nightvision) != null;
            ((CameraPlayer) entityplayer).storeCameraData(hasNightvision);
            GameMode gametype = GameMode.SPECTATOR;
            entityplayer.method_3170(gametype);
            if(!hasNightvision && !LoggerRegistry.getLogger("normalCameraVision").subscribed(entityplayer)) {
                StatusEffectInstance potioneffect = new StatusEffectInstance(nightvision, 999999, 0, false, false);
                entityplayer.addStatusEffect(potioneffect);
            }
            ((CameraPlayer) entityplayer).setGamemodeCamera();
        }
    }

    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos)
    {
        return Collections.emptyList();
    }

}
