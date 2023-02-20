package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.command.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandRegistry;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(CommandRegistry.class)
public class CommandRegistryMixin {
    private final ThreadLocal<Boolean> weSession = new ThreadLocal<>();

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            )
    )
    private Object startEditSession(Map<String, Command> commandMap, Object commandName, CommandSource sender) {
        //noinspection SuspiciousMethodCalls
        Command command = commandMap.get(commandName);
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        boolean nonWorldEditCommand = command != null && !command.getClass().getName().startsWith("carpet.worldedit.");
        boolean weSession = nonWorldEditCommand && WorldEditBridge.worldEditEnabled();
        this.weSession.set(weSession);
        if (weSession) WorldEditBridge.startEditSession(worldEditPlayer);
        return command;
    }

    @Inject(
            method = "execute",
            at = @At(
                    value = "CONSTANT",
                    args = "intValue=-1"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onCommand(CommandSource sender, String rawCommand, CallbackInfoReturnable<Integer> cir, String[] args, String commandName, Command command) {
        WorldEditBridge.onCommand(command, sender, args);
    }

    // Not as good as a finally block, but should do the job since CommandExceptions are already handled
    @Inject(
            method = "execute",
            at = @At("RETURN")
    )
    private void endEditSession(CommandSource sender, String rawCommand, CallbackInfoReturnable<Integer> cir) {
        if (!weSession.get()) return;
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        WorldEditBridge.finishEditSession(worldEditPlayer);
        weSession.set(false);
    }
}
