package carpet.mixin.entityTrackerFix;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.entity.EntityTrackerEntry;
import net.minecraft.entity.living.player.PlayerEntity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private int trackedDistance;
    @Shadow @Final private Entity currentTrackedEntity;

    @Redirect(
            method = "broadcastTo",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/entity/EntityTrackerEntry;trackedDistance:I"
            )
    )
    private int entityTrackerFix(EntityTrackerEntry entry) {
        if (!CarpetSettings.entityTrackerFix) return trackedDistance;
        if (currentTrackedEntity instanceof MinecartEntity || currentTrackedEntity instanceof BoatEntity) {
            for (Entity e : currentTrackedEntity.getPassengers()) {
                if (e instanceof PlayerEntity) return 512;
            }
        }
        return trackedDistance;
    }
}
