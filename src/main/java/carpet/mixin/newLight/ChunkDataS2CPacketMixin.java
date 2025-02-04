package carpet.mixin.newLight;

import carpet.CarpetSettings;
import carpet.utils.extensions.NewLightWorld;
import net.minecraft.network.packet.s2c.play.WorldChunkS2CPacket;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldChunkS2CPacket.class)
public class ChunkDataS2CPacketMixin {
    // Because Mixin doesn't allow @Injects before return
    @Redirect(
            method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;I)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/World;dimension:Lnet/minecraft/world/dimension/Dimension;"
            )
    )
    private Dimension getProviderAndProcLight(World world) {
        if (CarpetSettings.newLight) ((NewLightWorld) world).getLightingEngine().procLightUpdates();
        return world.dimension;
    }
}
