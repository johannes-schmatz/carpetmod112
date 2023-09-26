package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({
    Block.class,
    BlockWithBlockEntity.class,
    CobwebBlock.class,
    DeadBushBlock.class,
    IceBlock.class,
    LeavesBlock.class,
    Leaves2Block.class,
    SnowLayerBlock.class,
    TallPlantBlock.class,
    VineBlock.class
})
public class BlockHarvestMixins {
    @Redirect(
            method = "afterMinedByPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/stat/Stats;blockMined(Lnet/minecraft/block/Block;)Lnet/minecraft/stat/Stat;"
            )
    )
    private Stat addBlockMeta(Block blockIn, World worldIn, PlayerEntity player, BlockPos pos, BlockState state) {
        return StatHelper.getBlockStateStats(state);
    }
}
