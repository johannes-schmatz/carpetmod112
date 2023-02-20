package carpet.mixin.huskSpawningInTemples;

import carpet.CarpetSettings;
import net.minecraft.entity.HuskEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HuskEntity.class)
public class HuskEntityMixin {
    @Redirect(
            method = "canSpawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;hasDirectSunlight(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean alwaysInTemple(World world, BlockPos pos) {
        if (world.hasDirectSunlight(pos)) return true;
        if (!CarpetSettings.huskSpawningInTemples) return false;
        return ((ServerWorld) world).getChunkProvider().method_14961(world, "Temple", pos);
    }
}
