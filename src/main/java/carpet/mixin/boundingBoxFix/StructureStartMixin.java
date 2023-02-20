package carpet.mixin.boundingBoxFix;

import carpet.CarpetSettings;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.gen.GeneratorConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GeneratorConfig.class)
public abstract class StructureStartMixin {
    @Shadow protected abstract void setBoundingBoxFromChildren();

    @Inject(
            method = "toNbt",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=Children"
            )
    )
    private void fixBoundingBox(int chunkX, int chunkZ, CallbackInfoReturnable<NbtCompound> cir) {
        //FIXME: why is this not @At("HEAD")?
        // this should be @At("HEAD"), then it saves the correct BB
        if(CarpetSettings.boundingBoxFix) {
            setBoundingBoxFromChildren();
        }
    }
}
