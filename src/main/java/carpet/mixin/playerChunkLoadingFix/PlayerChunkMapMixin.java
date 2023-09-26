package carpet.mixin.playerChunkLoadingFix;

import carpet.CarpetSettings;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.ChunkMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Fix the player chunk map truncation in negative coords causing offsets in chunk loading CARPET-XCOM
@Mixin(ChunkMap.class)
public class PlayerChunkMapMixin {
    @Redirect(
            method = {
                    "addPlayer",
                    "movePlayer"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;x:D",
                    ordinal = 0
            )
    )
    private double getPosX(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.x) : player.x;
    }

    @Redirect(
            method = {
                    "addPlayer",
                    "movePlayer"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;z:D",
                    ordinal = 0
            )
    )
    private double getPosZ(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.z) : player.z;
    }

    @Redirect(
            method = "removePlayer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;trackedX:D",
                    ordinal = 0
            )
    )
    private double getManagedPosX(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.trackedX) : player.trackedX;
    }

    @Redirect(
            method = "removePlayer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;trackedZ:D",
                    ordinal = 0
            )
    )
    private double getManagedPosZ(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.trackedZ) : player.trackedZ;
    }

    @Redirect(
            method = "movePlayer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;trackedX:D",
                    ordinal = 1
            )
    )
    private double getManagedPosX2(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.trackedX) : player.trackedX;
    }

    @Redirect(
            method = "movePlayer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;trackedZ:D",
                    ordinal = 1
            )
    )
    private double getManagedPosZ2(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.trackedZ) : player.trackedZ;
    }
}
