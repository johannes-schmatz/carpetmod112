package carpet.mixin.pistonSerializationFix;

import carpet.CarpetSettings;

import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.nbt.NbtCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovingBlockEntity.class)
public class PistonBlockEntityMixin {
    @Shadow private float lastProgress;
    @Shadow private float progress;

    @Inject(
            method = "readNbt",
            at = @At("RETURN")
    )
    private void onDeserialize(NbtCompound compound, CallbackInfo ci) {
        if (CarpetSettings.pistonSerializationFix && compound.isType("lastProgress", 5)) {
            this.lastProgress = compound.getFloat("lastProgress");
        }
    }

    @Redirect(
            method = "writeNbt",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/entity/MovingBlockEntity;lastProgress:F"
            )
    )
    private float serializeProgress(MovingBlockEntity te, NbtCompound compound) {
        if (!CarpetSettings.pistonSerializationFix) return lastProgress;
        compound.putFloat("lastProgress", lastProgress);
        return progress;
    }
}
