package carpet.mixin.updateSuppressionCrashFix;

import carpet.CarpetSettings;
import carpet.helpers.ThrowableSuppression;
import carpet.utils.Messenger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Redirect(
            method = "tickWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tick()V"
            )
    )
    private void fixUpdateSuppressionCrashTick(ServerWorld worldServer) {
        if (!CarpetSettings.updateSuppressionCrashFix) {
            worldServer.tick();
            return;
        }
        try {
            worldServer.tick();
        } catch (CrashException e) {
            if (!(e.getReport().getException() instanceof ThrowableSuppression)) throw e;
            logUpdateSuppression("world tick");
        } catch (ThrowableSuppression ignored) {
            logUpdateSuppression("world tick");
        }
    }

    @Redirect(
            method = "tickWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickEntities()V"
            )
    )
    private void fixUpdateSuppressionCrashTickEntities(ServerWorld worldServer) {
        if (!CarpetSettings.updateSuppressionCrashFix) {
            worldServer.tickEntities();
            return;
        }
        try {
            worldServer.tickEntities();
        } catch (CrashException e) {
            if (!(e.getReport().getException() instanceof ThrowableSuppression)) throw e;
            logUpdateSuppression("update entities");
        } catch (ThrowableSuppression ignored) {
            logUpdateSuppression("update entities");
        }
    }

    private void logUpdateSuppression(String phase) {
        Messenger.print_server_message((MinecraftServer) (Object) this, "You just caused a server crash in " + phase + ".");
    }
}
