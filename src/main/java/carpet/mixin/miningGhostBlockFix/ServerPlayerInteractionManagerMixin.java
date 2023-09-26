package carpet.mixin.miningGhostBlockFix;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientServer;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow public ServerPlayerEntity player;
    @Shadow public World world;
    @Shadow private BlockPos prevTarget;

    @Inject(
            method = "startMiningBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;updateBlockMiningProgress(ILnet/minecraft/util/math/BlockPos;I)V"
            )
    )
    private void miningGhostBlockFix(BlockPos pos, Direction side, CallbackInfo ci) {
        if (CarpetSettings.miningGhostBlocksFix && CarpetClientServer.activateInstantMine) {
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, prevTarget));
        }
    }
}
