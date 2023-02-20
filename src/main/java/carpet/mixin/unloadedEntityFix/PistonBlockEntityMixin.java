package carpet.mixin.unloadedEntityFix;

import carpet.CarpetSettings;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonBlockEntity.class)
public class PistonBlockEntityMixin {
    @Redirect(
            method = {
                    "method_13758",
                    "method_13754"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;move(Lnet/minecraft/entity/MovementType;DDD)V"
            )
    )
    private void moveAndUpdate(Entity entity, MovementType type, double x, double y, double z) {
        entity.move(type, x, y, z);
        if (CarpetSettings.unloadedEntityFix) {
            // Add entity to the correct chunk after moving
            entity.world.checkChunk(entity, false);
        }
    }
}
