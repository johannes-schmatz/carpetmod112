package carpet.mixin.endRNG;

import carpet.CarpetServer;
import carpet.CarpetSettings;

import net.minecraft.structure.EndCityStructure;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(EndCityStructure.class)
public class EndCityFeatureMixin {
    @Redirect(
            method = "shouldStartAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getStructureRandom(III)Ljava/util/Random;"
            )
    )
    private Random endRNG(World world, int x, int z, int seed) {
        if (CarpetSettings.endRNG) return CarpetServer.getInstance().setRandomSeed(x, z, seed);
        return world.getStructureRandom(x, z, seed);
    }
}
