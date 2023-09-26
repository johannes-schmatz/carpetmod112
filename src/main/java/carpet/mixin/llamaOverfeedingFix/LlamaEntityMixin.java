package carpet.mixin.llamaOverfeedingFix;

import carpet.CarpetSettings;

import net.minecraft.unmapped.C_7410869;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(C_7410869.class)
public class LlamaEntityMixin {
    @Redirect(
            method = "m_8210696",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/unmapped/C_7410869;isTame()Z",
                    ordinal = 0
            )
    )
    private boolean isTameOrOverfeeding(C_7410869 llama) {
        return llama.isTame() && !(CarpetSettings.llamaOverfeedingFix && llama.isInLove());
    }
}
