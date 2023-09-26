package carpet.mixin.client;

import carpet.mixin.accessors.PlayerListHudAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.gui.overlay.PlayerTabOverlay;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(GameGui.class)
public abstract class InGameHudMixin {
    @Shadow @Final private PlayerTabOverlay playerTabOverlay;

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z"
            )
    )
    private boolean onDraw(Minecraft minecraftClient) {
        if (!minecraftClient.isInSingleplayer()) return false;
        PlayerListHudAccessor hud = ((PlayerListHudAccessor) playerTabOverlay);
        return hud.getFooter() == null && hud.getHeader() == null;
    }
}
