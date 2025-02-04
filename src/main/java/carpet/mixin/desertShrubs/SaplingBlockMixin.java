package carpet.mixin.desertShrubs;

import carpet.CarpetSettings;
import carpet.helpers.BlockSaplingHelper;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(SaplingBlock.class)
public class SaplingBlockMixin {
    @Inject(
            method = "tryGrow(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;Ljava/util/Random;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/SaplingBlock;grow(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;Ljava/util/Random;)V"
            ),
            cancellable = true
    )
    private void desertShrubs(World world, BlockPos pos, BlockState state, Random rand, CallbackInfo ci) {
        if (CarpetSettings.desertShrubs && world.getBiome(pos) == Biomes.DESERT && !BlockSaplingHelper.hasWater(world, pos)) {
            world.setBlockState(pos, Blocks.DEADBUSH.defaultState());
            ci.cancel();
        }
    }
}
