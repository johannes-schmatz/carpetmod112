package carpet.mixin.netherRNG;

import carpet.CarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.gen.structure.StructureFeature;
import net.minecraft.world.gen.structure.FortressStructure;

import java.util.Random;

@Mixin(FortressStructure.class)
public abstract class NetherFortressFeatureMixin extends StructureFeature {
    @Redirect(
            method = "isFeatureChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Random;setSeed(J)V",
                    remap = false
            )
    )
    private void setSeed(Random random, long seed) {
        if (CarpetSettings.netherRNG) {
            world.random.setSeed(seed);
        }
        random.setSeed(seed);
    }
}
