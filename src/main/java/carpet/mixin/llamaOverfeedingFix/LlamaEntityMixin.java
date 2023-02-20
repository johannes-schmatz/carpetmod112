package carpet.mixin.llamaOverfeedingFix;

import carpet.CarpetSettings;

import net.minecraft.entity.LlamaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LlamaEntity.class)
public class LlamaEntityMixin {
    @Redirect(
            method = "method_13970",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LlamaEntity;method_13990()Z",
                    ordinal = 0
            )
    )
    private boolean isTameOrOverfeeding(LlamaEntity llama) {
        return llama.method_13990() && !(CarpetSettings.llamaOverfeedingFix && llama.isInLove());
    }
}
