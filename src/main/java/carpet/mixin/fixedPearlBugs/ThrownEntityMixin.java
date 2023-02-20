package carpet.mixin.fixedPearlBugs;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.thrown.ThrowableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ThrowableEntity.class)
public abstract class ThrownEntityMixin extends Entity {
    @Shadow private String ownerName;
    @Shadow protected LivingEntity field_6932;

    public ThrownEntityMixin(World worldIn) {
        super(worldIn);
    }

    // Fixes pearls disappearing when players relog similar to 1.15 CARPET-XCOM
    @Inject(method = "getOwner", at = @At("HEAD"))
    private void pearlCheck(CallbackInfoReturnable<LivingEntity> cir) {
        if (!CarpetSettings.fixedPearlBugs) return;
        if (ownerName == null) {
            if (field_6932 == null) return;
            ownerName = field_6932.getTranslationKey();
        }
        try {
            Entity e = ((ServerWorld) world).getEntity(UUID.fromString(ownerName));
            if (!world.entities.contains(e)) {
                field_6932 = null;
            }
        } catch (Exception e) {
            field_6932 = null;
        }
    }
}
