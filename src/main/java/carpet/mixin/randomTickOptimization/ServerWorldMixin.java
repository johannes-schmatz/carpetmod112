package carpet.mixin.randomTickOptimization;

import carpet.helpers.RandomTickOptimization;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    protected ServerWorldMixin(WorldStorage storage, WorldData data, Dimension dimension, Profiler profiler,
            boolean isClient) {
        super(storage, data, dimension, profiler, isClient);
    }

    // Prevent execution of the original return
    @Redirect(
            method = "scheduleTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;acceptsImmediateTicks()Z"
            )
    )
    private boolean requiresUpdatesWorldGenFix(Block block) {
        return !RandomTickOptimization.needsWorldGenFix && block.acceptsImmediateTicks();
    }

    @Inject(
            method = "scheduleTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;acceptsImmediateTicks()Z"
            ),
            cancellable = true
    )
    private void randomTickWorldGenFix(BlockPos pos, Block blockIn, int delay, int priority, CallbackInfo ci) {
        if (!RandomTickOptimization.needsWorldGenFix) return;
        if (this.isAreaLoaded(pos.add(-8, -8, -8), pos.add(8, 8, 8))) {
            BlockState state = this.getBlockState(pos);
            if (state.getMaterial() != Material.AIR && state.getBlock() == blockIn) {
                state.getBlock().tick(this, pos, state, this.random);
            }
            ci.cancel(); // move return into the if for `needsWorldGenFix`
        }
    }
}
