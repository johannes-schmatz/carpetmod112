package carpet.mixin.playerChunkLoadingFix;

import carpet.CarpetSettings;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.PlayerWorldManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Fix the player chunk map truncation in negative coords causing offsets in chunk loading CARPET-XCOM
@Mixin(PlayerWorldManager.class)
public class PlayerChunkMapMixin {
    @Redirect(
            method = {
                    "method_2109",
                    "method_2116"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;x:D",
                    ordinal = 0
            )
    )
    private double getPosX(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.x) : player.x;
    }

    @Redirect(
            method = {
                    "method_2109",
                    "method_2116"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;z:D",
                    ordinal = 0
            )
    )
    private double getPosZ(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.z) : player.z;
    }

    @Redirect(
            method = "method_2115",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;serverPosX:D",
                    ordinal = 0
            )
    )
    private double getManagedPosX(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.serverPosX) : player.serverPosX;
    }

    @Redirect(
            method = "method_2115",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;serverPosZ:D",
                    ordinal = 0
            )
    )
    private double getManagedPosZ(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.serverPosZ) : player.serverPosZ;
    }

    @Redirect(
            method = "method_2116",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;serverPosX:D",
                    ordinal = 1
            )
    )
    private double getManagedPosX2(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.serverPosX) : player.serverPosX;
    }

    @Redirect(
            method = "method_2116",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;serverPosZ:D",
                    ordinal = 1
            )
    )
    private double getManagedPosZ2(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.serverPosZ) : player.serverPosZ;
    }
}
