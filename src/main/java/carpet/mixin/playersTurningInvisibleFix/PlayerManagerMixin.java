package carpet.mixin.playersTurningInvisibleFix;

import carpet.CarpetSettings;
import carpet.mixin.accessors.WorldAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(
            method = "respawn",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;remove(Ljava/lang/Object;)Z"
            )
    )
    private void removeFromChunk(ServerPlayerEntity player, int dimension, boolean conqueredEnd, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        if (CarpetSettings.playersTurningInvisibleFix) {
            player.getServerWorld()
                    .getChunkAt(player.chunkX, player.chunkZ)
                    .removeEntity(player, player.chunkY);
        }
    }

    @Redirect(
            method = "teleportToDimension",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;removeEntityNow(Lnet/minecraft/entity/Entity;)V"
            )
    )
    private void removePlayerOnDimensionChange(ServerWorld world, Entity player) {
        if (CarpetSettings.playersTurningInvisibleFix) {
            world.removeEntity(player);
        } else {
            world.removeEntityNow(player);
        }
    }

    @Inject(
            method = "teleportEntityToDimension",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiler/Profiler;pop()V",
                    ordinal = 0
            )
    )
    private void onTransfer(Entity entityIn, int lastDimension, ServerWorld oldWorldIn, ServerWorld toWorldIn, CallbackInfo ci) {
        // Players pulling disappear act when using portals. Fix for MC-92916 CARPET-XCOM
        if (CarpetSettings.playersTurningInvisibleFix && entityIn.isLoaded && ((WorldAccessor) oldWorldIn).invokeIsChunkLoaded(entityIn.chunkX, entityIn.chunkZ, true)) {
            if (entityIn.isLoaded && ((WorldAccessor) oldWorldIn).invokeIsChunkLoaded(entityIn.chunkX, entityIn.chunkZ, true)) {
                oldWorldIn.getChunkAt(entityIn.chunkX, entityIn.chunkZ)
                        .removeEntity(entityIn, entityIn.chunkY);
            }
        }
    }
}
