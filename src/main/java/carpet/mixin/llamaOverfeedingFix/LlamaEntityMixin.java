package carpet.mixin.llamaOverfeedingFix;

import carpet.CarpetSettings;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.living.mob.passive.animal.LlamaEntity;

@Mixin(LlamaEntity.class)
public class LlamaEntityMixin {
    @Redirect(
            method = "m_8210696",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/mob/passive/animal/LlamaEntity;isTame()Z",
                    ordinal = 0
            )
    )
    private boolean isTameOrOverfeeding(LlamaEntity llama) {
        return llama.isTame() && !(CarpetSettings.llamaOverfeedingFix && llama.isInLove());
    }
}
