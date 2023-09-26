package carpet.mixin.potions;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.living.effect.StatusEffectInstance;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Redirect(
            method = "onStatusEffectRemoved",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/player/PlayerEntity;onStatusEffectRemoved(Lnet/minecraft/entity/living/effect/StatusEffectInstance;)V"
            )
    )
    private void finishPotionEffectHead(PlayerEntity entityPlayer, StatusEffectInstance effect) {
        if (!CarpetSettings.effectsFix) super.onStatusEffectRemoved(effect);
    }

    @Inject(
            method = "onStatusEffectRemoved",
            at = @At("RETURN")
    )
    private void finishedPotionEffectReturn(StatusEffectInstance effect, CallbackInfo ci) {
        if (CarpetSettings.effectsFix) super.onStatusEffectRemoved(effect);
    }
}
