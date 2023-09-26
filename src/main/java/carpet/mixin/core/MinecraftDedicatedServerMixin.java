package carpet.mixin.core;

import carpet.CarpetServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class MinecraftDedicatedServerMixin {
    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/GameProfileCache;setOnlineMode(Z)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onServerLoaded(CallbackInfoReturnable<Boolean> cir) {
        CarpetServer.getInstance().onServerLoaded();
    }
}
