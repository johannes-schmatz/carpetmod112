package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({
    Block.class,
    BlockWithEntity.class,
    CobwebBlock.class,
    DeadBushBlock.class,
    IceBlock.class,
    Leaves1Block.class,
    Leaves2Block.class,
    SnowLayerBlock.class,
    TallPlantBlock.class,
    VineBlock.class
})
public class BlockHarvestMixins {
    @Redirect(
            method = "method_8651",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/stat/Stats;mined(Lnet/minecraft/block/Block;)Lnet/minecraft/stat/Stat;"
            )
    )
    private Stat addBlockMeta(Block blockIn, World worldIn, PlayerEntity player, BlockPos pos, BlockState state) {
        return StatHelper.getBlockStateStats(state);
    }
}
