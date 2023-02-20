package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
            method = "method_3170",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;method_14157()V"
            )
    )
    private void onChangeToSpectator(GameMode gameType, CallbackInfo ci) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ((ServerWorld) world).getPlayerWorldManager().method_2115((ServerPlayerEntity) (Object) this);
        }
    }

    @Inject(
            method = "method_3170",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;method_10763(Lnet/minecraft/entity/Entity;)V"
            )
    )
    private void onChangeFromSpectator(GameMode gameType, CallbackInfo ci) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ((ServerWorld) world).getPlayerWorldManager().method_2109((ServerPlayerEntity) (Object) this);
        }
    }
}
