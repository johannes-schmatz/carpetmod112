package carpet.mixin.villageMarkers;

import carpet.utils.extensions.ExtendedVillageCollection;
import net.minecraft.world.World;
import net.minecraft.world.village.Village;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Village.class)
public class VillageMixin {
    @Shadow private World world;

    @Inject(
            method = "cleanUpDoors",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Iterator;remove()V",
                    remap = false
            )
    )
    private void updateOldDoors(CallbackInfo ci) {
        ((ExtendedVillageCollection) world.getVillages()).markVillageMarkersDirty();
    }
}
