package carpet.mixin.leashFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.entity.EntityTrackerEntry;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private Entity currentTrackedEntity;

    @Inject(
            method = "updateListener",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;m_3594075(Lnet/minecraft/entity/Entity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void leashFix(ServerPlayerEntity playerMP, CallbackInfo ci) {
        if (CarpetSettings.leashFix == CarpetSettings.LeashFix.off || !(currentTrackedEntity instanceof MobEntity)) return;
        playerMP.networkHandler.sendPacket(new EntityAttachS2CPacket(currentTrackedEntity, ((MobEntity) currentTrackedEntity).getHoldingEntity()));
    }
}
