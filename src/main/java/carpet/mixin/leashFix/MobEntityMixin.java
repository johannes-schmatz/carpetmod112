package carpet.mixin.leashFix;

import carpet.CarpetSettings;
import carpet.utils.extensions.EntityWithPostLoad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin implements EntityWithPostLoad {
    @Shadow protected abstract void method_6163();
    @Shadow private Entity leashOwner;
    @Shadow private NbtCompound leash;

    @Override
    public void postLoad() {
        if (CarpetSettings.leashFix == CarpetSettings.LeashFix.cool) method_6163();
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=LeftHanded"
            )
    )
    private void saveNBT(NbtCompound compound, CallbackInfo ci) {
        if (leashOwner == null && CarpetSettings.leashFix == CarpetSettings.LeashFix.casual && leash != null) {
            compound.put("Leash", leash);
        }
    }
}
