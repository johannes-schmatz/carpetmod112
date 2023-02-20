package carpet.mixin.structureBlockLimit;

import carpet.helpers.IPlayerSensitiveTileEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.ChunkPlayerManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkPlayerManager.class)
public class PlayerChunkMapEntryMixin {
    @Shadow private boolean field_13865;
    @Shadow @Final private List<ServerPlayerEntity> players;

    @Inject(
            method = "method_8120",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/BlockEntity;getUpdatePacket()Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;"
            ),
            cancellable = true
    )
    private void sendPlayerSensitiveBlockEntity(BlockEntity be, CallbackInfo ci) {
        if (be instanceof IPlayerSensitiveTileEntity) {
            if (field_13865) {
                for (ServerPlayerEntity player : players) {
                    BlockEntityUpdateS2CPacket packet = ((IPlayerSensitiveTileEntity) be).getUpdatePacketPlayerSensitive(player);
                    if (packet != null) player.networkHandler.sendPacket(packet);
                }
            }
            ci.cancel();
        }
    }
}
