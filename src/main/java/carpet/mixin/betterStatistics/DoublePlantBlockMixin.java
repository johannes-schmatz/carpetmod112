package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.stat.Stat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DoublePlantBlock.class)
public class DoublePlantBlockMixin {
    @Redirect(
            method = "onBreak",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/stat/Stats;mined(Lnet/minecraft/block/Block;)Lnet/minecraft/stat/Stat;"
            )
    )
    private Stat addBlockMeta(Block blockIn, World worldIn, BlockPos pos, BlockState state) {
        return StatHelper.getBlockStateStats(state);
    }
}
