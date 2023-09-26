package carpet.mixin.leashFix;

import carpet.CarpetSettings;
import carpet.utils.extensions.EntityWithPostLoad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin implements EntityWithPostLoad {
    @Shadow protected abstract void readLeashNbt();
    @Shadow private Entity holdingEntity;
    @Shadow private NbtCompound leashNbt;

    @Override
    public void postLoad() {
        if (CarpetSettings.leashFix == CarpetSettings.LeashFix.cool) readLeashNbt();
    }

    @Inject(
            method = "writeCustomNbt",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=LeftHanded"
            )
    )
    private void saveNBT(NbtCompound compound, CallbackInfo ci) {
        if (holdingEntity == null && CarpetSettings.leashFix == CarpetSettings.LeashFix.casual && leashNbt != null) {
            compound.put("Leash", leashNbt);
        }
    }
}
