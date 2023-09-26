package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(
            method = "setGameMode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;m_2820371()V"
            )
    )
    private void onChangeToSpectator(GameMode gameType, CallbackInfo ci) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ((ServerWorld) world).getChunkMap().removePlayer((ServerPlayerEntity) (Object) this);
        }
    }

    @Inject(
            method = "setGameMode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;setCamera(Lnet/minecraft/entity/Entity;)V"
            )
    )
    private void onChangeFromSpectator(GameMode gameType, CallbackInfo ci) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ((ServerWorld) world).getChunkMap().addPlayer((ServerPlayerEntity) (Object) this);
        }
    }
}
