package carpet.mixin.client;

import carpet.mixin.accessors.PlayerListHudAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final private PlayerListHud playerListHud;

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;isIntegratedServerRunning()Z"
            )
    )
    private boolean onDraw(MinecraftClient minecraftClient) {
        if (!minecraftClient.isInSingleplayer()) return false;
        PlayerListHudAccessor hud = ((PlayerListHudAccessor) playerListHud);
        return hud.getFooter() == null && hud.getHeader() == null;
    }
}
