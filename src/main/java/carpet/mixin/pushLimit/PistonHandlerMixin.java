package carpet.mixin.pushLimit;

import carpet.CarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.block.piston.PistonMoveStructureResolver;

@Mixin(PistonMoveStructureResolver.class)
public class PistonHandlerMixin {
    @ModifyConstant(
            method = "addColumn",
            constant = @Constant(intValue = 12)
    )
    private int pushLimit(int original) {
        return CarpetSettings.pushLimit;
    }
}
