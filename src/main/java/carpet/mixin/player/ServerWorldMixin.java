package carpet.mixin.player;

import carpet.patches.FakeServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(
            method = "canAddEntity",
            at = @At(
                    value = "CONSTANT",
                    args = "classValue=net/minecraft/entity/player/PlayerEntity"
            ),
            cancellable = true
    )
    private void onPlayerEntityClass(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (FakeServerPlayerEntity.shouldFixMinecart()) {
            entity.removed = true;
            cir.setReturnValue(true);
        }
    }
}
