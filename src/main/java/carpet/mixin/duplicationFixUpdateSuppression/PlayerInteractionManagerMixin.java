package carpet.mixin.duplicationFixUpdateSuppression;

import carpet.CarpetSettings;
import carpet.helpers.CarefulBreakHelper;
import net.minecraft.block.Blocks;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.WorldGeneratorType;

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

    @Shadow protected abstract boolean mineBlock(BlockPos pos);

    @Redirect(
            method = "tryMineBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ServerPlayerInteractionManager;mineBlock(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean dupeFixEmulateRemove(ServerPlayerInteractionManager playerInteractionManager, BlockPos pos) {
        if (!CarpetSettings.duplicationFixUpdateSuppression) {
            try {
                CarefulBreakHelper.miningPlayer = player;
                return this.mineBlock(pos);
            } finally {
                CarefulBreakHelper.miningPlayer = null;
            }
        }
        int y = pos.getY();
        if (y >= 0 && y < 256) {
            if (world.isClient || world.getData().getGeneratorType() != WorldGeneratorType.DEBUG_ALL_BLOCK_STATES) {
                return world.getBlockState(pos) != Blocks.AIR.defaultState();
            }
        }
        return false;
    }

    @Inject(
            method = "tryMineBlock",
            at = @At("TAIL")
    )
    private void dupeFixRemoveBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Update suppression duplication fix removing the block post inventory updates with exception to flowing water as ice can turn into flowing water CARPET-XCOM
        if (!CarpetSettings.duplicationFixUpdateSuppression || world.getBlockState(pos) == Blocks.FLOWING_WATER.defaultState()) {
            return;
        }
        try {
            CarefulBreakHelper.miningPlayer = player;
            this.mineBlock(pos);
        } finally {
            CarefulBreakHelper.miningPlayer = null;
        }
    }
}
