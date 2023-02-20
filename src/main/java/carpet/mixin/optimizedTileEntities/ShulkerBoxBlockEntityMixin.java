package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import carpet.helpers.BlockEntityOptimizer;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.class_2737;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin extends class_2737 implements BlockEntityOptimizer.LazyBlockEntity {
    @Shadow private ShulkerBoxBlockEntity.ShulkerBlockState state;
    // CARPET-optimizedTileEntities: Whether the tile entity is asleep or not.
    // False by default so tile entities wake up upon chunk loading
    private boolean sleeping = false;

    @Override
    public void wakeUp(){
        this.sleeping = false;
    }

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void optimize(CallbackInfo ci) {
        if (CarpetSettings.optimizedTileEntities && sleeping) ci.cancel();
    }

    @Inject(
            method = "method_13742",
            at = @At("RETURN")
    )
    private void onUpdateAnimation(CallbackInfo ci) {
        ShulkerBoxBlockEntity.ShulkerBlockState status = state;
        sleeping = status == ShulkerBoxBlockEntity.ShulkerBlockState.OPENED || status == ShulkerBoxBlockEntity.ShulkerBlockState.CLOSED;
    }

    @Inject(
            method = "onBlockAction",
            at = @At("HEAD")
    )
    private void onClientEvent(int id, int type, CallbackInfoReturnable<Boolean> cir) {
        sleeping = false;
    }
}
