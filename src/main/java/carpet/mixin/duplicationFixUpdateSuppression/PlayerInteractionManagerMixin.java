package carpet.mixin.duplicationFixUpdateSuppression;

import carpet.CarpetSettings;
import carpet.helpers.CarefulBreakHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin {
    @Shadow public World world;
    @Shadow public ServerPlayerEntity player;

    @Shadow protected abstract boolean tryBreakBlock(BlockPos pos);

    @Redirect(
            method = "method_10766",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;tryBreakBlock(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean dupeFixEmulateRemove(ServerPlayerInteractionManager playerInteractionManager, BlockPos pos) {
        if (!CarpetSettings.duplicationFixUpdateSuppression) {
            try {
                CarefulBreakHelper.miningPlayer = player;
                return this.tryBreakBlock(pos);
            } finally {
                CarefulBreakHelper.miningPlayer = null;
            }
        }
        int y = pos.getY();
        if (y >= 0 && y < 256) {
            if (world.isClient || world.getLevelProperties().getGeneratorType() != LevelGeneratorType.DEBUG) {
                return world.getBlockState(pos) != Blocks.AIR.getDefaultState();
            }
        }
        return false;
    }

    @Inject(
            method = "method_10766",
            at = @At("TAIL")
    )
    private void dupeFixRemoveBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Update suppression duplication fix removing the block post inventory updates with exception to flowing water as ice can turn into flowing water CARPET-XCOM
        if (!CarpetSettings.duplicationFixUpdateSuppression || world.getBlockState(pos) == Blocks.FLOWING_WATER.getDefaultState()) {
            return;
        }
        try {
            CarefulBreakHelper.miningPlayer = player;
            this.tryBreakBlock(pos);
        } finally {
            CarefulBreakHelper.miningPlayer = null;
        }
    }
}
