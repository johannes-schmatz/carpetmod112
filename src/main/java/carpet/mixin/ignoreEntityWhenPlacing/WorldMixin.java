package carpet.mixin.ignoreEntityWhenPlacing;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class WorldMixin {
    @Redirect(
            method = "canPlaceBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;canBuildIn(Lnet/minecraft/util/math/Box;Lnet/minecraft/entity/Entity;)Z"
            )
    )
    private boolean ignoreEntityWhenPlacing(World world, Box box, Entity entity) {
        return CarpetSettings.ignoreEntityWhenPlacing || world.canBuildIn(box, entity);
    }
}
