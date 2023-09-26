package carpet.mixin.structureBlockLimit;

import carpet.helpers.IPlayerSensitiveTileEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.ChunkHolder;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkHolder.class)
public class PlayerChunkMapEntryMixin {
    @Shadow private boolean populated;
    @Shadow @Final private List<ServerPlayerEntity> players;

    @Inject(
            method = "sendBlockEntityUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/BlockEntity;createUpdatePacket()Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;"
            ),
            cancellable = true
    )
    private void sendPlayerSensitiveBlockEntity(BlockEntity be, CallbackInfo ci) {
        if (be instanceof IPlayerSensitiveTileEntity) {
            if (populated) {
                for (ServerPlayerEntity player : players) {
                    BlockEntityUpdateS2CPacket packet = ((IPlayerSensitiveTileEntity) be).getUpdatePacketPlayerSensitive(player);
                    if (packet != null) player.networkHandler.sendPacket(packet);
                }
            }
            ci.cancel();
        }
    }
}
