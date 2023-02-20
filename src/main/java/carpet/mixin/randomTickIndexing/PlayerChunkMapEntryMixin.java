package carpet.mixin.randomTickIndexing;

import carpet.carpetclient.CarpetClientRandomtickingIndexing;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.ChunkPlayerManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkPlayerManager.class)
public class PlayerChunkMapEntryMixin {
    @Inject(
            method = "addPlayer",
            at = @At("RETURN")
    )
    private void onAdd(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetClientRandomtickingIndexing.enableUpdate(player);
    }

    @Inject(
            method = "method_8127",
            at = @At("RETURN")
    )
    private void onRemove(ServerPlayerEntity player, CallbackInfo ci) {
        CarpetClientRandomtickingIndexing.enableUpdate(player);
    }
}
