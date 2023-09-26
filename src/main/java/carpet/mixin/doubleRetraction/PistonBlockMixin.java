package carpet.mixin.doubleRetraction;

import carpet.CarpetSettings;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.PistonBaseBlock;
import net.minecraft.block.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBaseBlock.class)
public class PistonBlockMixin {
    @Shadow @Final public static BooleanProperty EXTENDED;

    @Inject(
            method = "checkExtended",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V",
                    ordinal = 1
            )
    )
    private void onRetract(World worldIn, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (CarpetSettings.doubleRetraction) {
            worldIn.setBlockState(pos, state.set(EXTENDED, false), 2);
        }
    }
}
