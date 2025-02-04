package carpet.mixin.pistonGhostBlockFix;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedPistonBlockEntityGhostBlockFix;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovingBlockEntity.class)
public class PistonBlockEntityMixin extends BlockEntity implements ExtendedPistonBlockEntityGhostBlockFix {
    @Shadow private float lastProgress;
    private long lastTicked;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void onUpdate(CallbackInfo ci) {
        this.lastTicked = this.world.getTime();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;I)Z"
            )
    )
    private void onSetBlockState(CallbackInfo ci) {
        if (CarpetSettings.pistonGhostBlocksFix == CarpetSettings.PistonGhostBlocksFix.serverOnly) {
            BlockState state = this.world.getBlockState(this.pos);
            this.world.onBlockChanged(pos.offset(state.get(PistonHeadBlock.FACING).getOpposite()), state, state, 0);
        }
    }

    @Override
    public long getLastTicked() {
        return lastTicked;
    }

    @Override
    public float getLastProgress() {
        return lastProgress;
    }
}
