package carpet.mixin.asyncBeaconUpdates;

import carpet.CarpetSettings;

import net.minecraft.block.BeaconBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.BlockWithBlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlock.class)
public abstract class BeaconBlockMixin extends BlockWithBlockEntity {
    protected BeaconBlockMixin(Material material) {
        super(material);
    }

    @Inject(
            method = "neighborChanged",
            at = @At("RETURN")
    )
    private void asyncBeaconUpdates(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, CallbackInfo ci) {
        if (CarpetSettings.asyncBeaconUpdates && world.hasNeighborSignal(pos)) {
            HttpUtil.DOWNLOAD_THREAD_FACTORY.submit(() -> {
                try {
                    world.updateNeighbors(pos, this, true);
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    System.out.println("Beacon thread exiting");
                }
            });
        }
    }
}
