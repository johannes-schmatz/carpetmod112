package carpet.mixin.villageMarkers;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.ExtendedVillageCollection;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.minecraft.village.VillageState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillageState.class)
public class VillageStateMixin implements ExtendedVillageCollection {
    @Shadow private World world;
    private boolean updateMarkers;

    @Inject(
            method = "method_2839",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/VillageState;method_2849()V",
                    shift = At.Shift.AFTER
            )
    )
    private void updateMarkers(CallbackInfo ci) {
        if (updateMarkers) {
            CarpetClientMarkers.updateClientVillageMarkers(world);
            updateMarkers = false;
        }
    }

    @Inject(
            method = "method_2845",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/VillageState;markDirty()V"
            )
    )
    private void updateOnRemove(CallbackInfo ci) {
        updateMarkers = true;
    }

    @Inject(
            method = "method_2849",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/Village;method_2817(Lnet/minecraft/village/VillageDoor;)V"
            )
    )
    private void updateOnAdd(CallbackInfo ci) {
        updateMarkers = true;
    }

    @Inject(
            method = "fromNbt",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private void updateOnDeserialize(NbtCompound nbt, CallbackInfo ci) {
        updateMarkers = true;
    }

    @Override
    public void markVillageMarkersDirty() {
        updateMarkers = true;
    }
}
