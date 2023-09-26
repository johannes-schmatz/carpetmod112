package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.Block;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(
            method = "doScheduledTicks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/BlockState;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void setChunkLoadingReason(boolean runAllPending, CallbackInfoReturnable<Boolean> cir, Iterator<ScheduledTick> iterator, ScheduledTick entry, int unused) {
        CarpetClientChunkLogger.setReason(() -> "Block update: " + Block.REGISTRY.getKey(entry.getBlock()) + " at " + entry.pos);
    }

    /*
    // extra int i
    @Surrogate
    private void setChunkLoadingReason(boolean runAllPending, CallbackInfoReturnable<Boolean> cir, int listSize, Iterator<NextTickListEntry> iterator, NextTickListEntry entry) {
        setChunkLoadingReason(runAllPending, cir, iterator, entry, 0);
    }
     */

    @Inject(
            method = "doScheduledTicks",
            at = @At("RETURN")
    )
    private void resetChunkLoadingReason(boolean runAllPending, CallbackInfoReturnable<Boolean> cir) {
        CarpetClientChunkLogger.resetReason();
    }

    @Inject(
            method = "doBlockEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;sendPacket(Lnet/minecraft/entity/living/player/PlayerEntity;DDDDILnet/minecraft/network/packet/Packet;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onBlockEvent(CallbackInfo ci, int index, Iterator<BlockEvent> iterator, BlockEvent data) {
        CarpetClientChunkLogger.setReason(() -> "Queued block event: " + data);
    }

    @Inject(
            method = "doBlockEvents",
            at = @At("RETURN")
    )
    private void onBlockEventsDone(CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
