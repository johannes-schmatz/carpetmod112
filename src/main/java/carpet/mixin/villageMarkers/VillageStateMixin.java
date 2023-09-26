package carpet.mixin.villageMarkers;

import carpet.carpetclient.CarpetClientMarkers;
import carpet.utils.extensions.ExtendedVillageCollection;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.minecraft.world.village.SavedVillageData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SavedVillageData.class)
public class VillageStateMixin implements ExtendedVillageCollection {
    @Shadow private World world;
    private boolean updateMarkers;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/village/SavedVillageData;addDoorsToVillages()V",
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
            method = "removeVillagesWithoutDoors",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/village/SavedVillageData;markDirty()V"
            )
    )
    private void updateOnRemove(CallbackInfo ci) {
        updateMarkers = true;
    }

    @Inject(
            method = "addDoorsToVillages",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/village/Village;addDoor(Lnet/minecraft/world/village/VillageDoor;)V"
            )
    )
    private void updateOnAdd(CallbackInfo ci) {
        updateMarkers = true;
    }

    @Inject(
            method = "readNbt",
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
