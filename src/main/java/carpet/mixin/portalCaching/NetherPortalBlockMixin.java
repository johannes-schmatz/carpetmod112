package carpet.mixin.portalCaching;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedPortalForcer;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.PortalBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortalBlock.class)
public class NetherPortalBlockMixin {
    @Inject(
            method = "create",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/PortalBlock$PortalBuilder;build()V",
                    shift = At.Shift.AFTER
            ),
            expect = 2
    )
    private void clearOnSpawnPortal(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.portalCaching) ((ExtendedPortalForcer) ((ServerWorld) world).getPortalForcer()).clearHistoryCache();
    }

    @Inject(
            method = "neighborChanged",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;)Z",
                    shift = At.Shift.AFTER
            ),
            expect = 2
    )
    private void clearOnNeighborChange(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci) {
        if (CarpetSettings.portalCaching) ((ExtendedPortalForcer) ((ServerWorld) world).getPortalForcer()).clearHistoryCache();
    }
}
