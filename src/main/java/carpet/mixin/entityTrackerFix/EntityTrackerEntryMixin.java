package carpet.mixin.entityTrackerFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TrackedEntityInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TrackedEntityInstance.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private int trackingDistance;
    @Shadow @Final private Entity trackedEntity;

    @Redirect(
            method = "method_10770",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/TrackedEntityInstance;trackingDistance:I"
            )
    )
    private int entityTrackerFix(TrackedEntityInstance entry) {
        if (!CarpetSettings.entityTrackerFix) return trackingDistance;
        if (trackedEntity instanceof AbstractMinecartEntity || trackedEntity instanceof BoatEntity) {
            for (Entity e : trackedEntity.getPassengerList()) {
                if (e instanceof PlayerEntity) return 512;
            }
        }
        return trackingDistance;
    }
}
