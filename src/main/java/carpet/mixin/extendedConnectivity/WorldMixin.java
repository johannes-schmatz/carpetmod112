package carpet.mixin.extendedConnectivity;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public abstract void neighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos);
    @Shadow public abstract void neighborStateChanged(BlockPos pos, Block changedBlock, BlockPos changedBlockPos);

    @Inject(
            method = "updateNeighbors",
            at = @At("HEAD")
    )
    private void extendedConnectivityNotify(BlockPos pos, Block blockType, boolean updateObservers, CallbackInfo ci) {
        if (CarpetSettings.extendedConnectivity) {
            BlockPos posd = pos.down();
            this.neighborChanged(posd.west(), blockType, pos);
            this.neighborChanged(posd.east(), blockType, pos);
            this.neighborChanged(posd.down(), blockType, pos);
            //this.neighborChanged(pos.up(), blockType);
            this.neighborChanged(posd.north(), blockType, pos);
            this.neighborChanged(posd.south(), blockType, pos);
            if (updateObservers) {
                this.neighborStateChanged(posd.west(), blockType, posd);
                this.neighborStateChanged(posd.east(), blockType, posd);
                this.neighborStateChanged(posd.down(), blockType, posd);
                //this.neighborStateChanged(pos.up(), blockType, pos);
                this.neighborStateChanged(posd.north(), blockType, posd);
                this.neighborStateChanged(posd.south(), blockType, posd);
            }
        }
    }

    @Inject(
            method = "updateNeighborsExcept",
            at = @At("HEAD")
    )
    private void extendedConnectivityExcept(BlockPos pos, Block blockType, Direction skipSide, CallbackInfo ci) {
        if (CarpetSettings.extendedConnectivity) {
            BlockPos posd = pos.down();
            if (skipSide != Direction.WEST) this.neighborChanged(posd.west(), blockType, posd);
            if (skipSide != Direction.EAST) this.neighborChanged(posd.east(), blockType, posd);
            if (skipSide != Direction.DOWN) this.neighborChanged(posd.down(), blockType, posd);
            //if (skipSide != Direction.UP) this.neighborChanged(pos.up(), blockType, posd);
            if (skipSide != Direction.NORTH) this.neighborChanged(posd.north(), blockType, posd);
            if (skipSide != Direction.SOUTH) this.neighborChanged(posd.south(), blockType, posd);
        }
    }
}
