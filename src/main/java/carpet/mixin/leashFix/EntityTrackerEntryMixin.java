package carpet.mixin.leashFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TrackedEntityInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrackedEntityInstance.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private Entity trackedEntity;

    @Inject(
            method = "method_2184",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;method_12790(Lnet/minecraft/entity/Entity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void leashFix(ServerPlayerEntity playerMP, CallbackInfo ci) {
        if (CarpetSettings.leashFix == CarpetSettings.LeashFix.off || !(trackedEntity instanceof MobEntity)) return;
        playerMP.networkHandler.sendPacket(new EntityAttachS2CPacket(trackedEntity, ((MobEntity) trackedEntity).getLeashOwner()));
    }
}
