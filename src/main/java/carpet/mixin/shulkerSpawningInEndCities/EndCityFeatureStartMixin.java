package carpet.mixin.shulkerSpawningInEndCities;

import carpet.CarpetSettings;

import net.minecraft.structure.EndCityStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCityStructure.EndCityGeneratorConfig.class)
public class EndCityFeatureStartMixin {
    @Inject(
            method = "isValid",
            at = @At("HEAD"),
            cancellable = true
    )
    private void alwaysSizeable(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.shulkerSpawningInEndCities) cir.setReturnValue(true);
    }
}
