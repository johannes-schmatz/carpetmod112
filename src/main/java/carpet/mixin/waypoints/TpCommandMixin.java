package carpet.mixin.waypoints;

import carpet.CarpetSettings;
import carpet.utils.Waypoint;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.TpCommand;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TpCommand.class)
public abstract class TpCommandMixin extends Command {
    @Inject(
            method = "run",
            at = @At("HEAD"),
            cancellable = true
    )
    private void teleportToWaypoint(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci) throws CommandException {
        if (args.length >= 1 && args.length <= 2 && CarpetSettings.commandWaypoint) {
            Entity entity = args.length == 1 ? asPlayer(sender) : parseEntity(server, sender, args[0]);
            Waypoint waypoint = Waypoint.find(args[args.length - 1], (ServerWorld) entity.world, server.worlds);
            if (waypoint != null) {
                waypoint.teleport(entity);
                sendSuccess(sender, this, "commands.tp.success", entity.getName(), waypoint.getFullName());
                ci.cancel();
            }
        }
    }
}
