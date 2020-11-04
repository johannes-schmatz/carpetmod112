package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.InstantComparators;
import carpet.utils.extensions.ExtendedTileEntityComparator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(BlockRedstoneComparator.class)
public abstract class BlockRedstoneComparatorMixin {
    @Shadow protected abstract int calculateOutput(World worldIn, BlockPos pos, IBlockState state);

    @Shadow protected abstract boolean isPowered(IBlockState state);

    @Shadow protected abstract boolean shouldBePowered(World worldIn, BlockPos pos, IBlockState state);

    @Inject(method = "updateTick", at = @At("RETURN"))
    private void logOnUpdateTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (LoggerRegistry.__instantComparators) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityComparator) {
                TileEntityComparator comparator = (TileEntityComparator) te;
                int index = (int) Math.floorMod(worldIn.getTotalWorldTime(), 3);
                // output signal 0 is generally considered to just be a too fast pulse for a comparator, rather
                // than an instant comparator
                ExtendedTileEntityComparator ext = (ExtendedTileEntityComparator) comparator;
                if (comparator.getOutputSignal() != ext.getScheduledOutputSignal()[index] && comparator.getOutputSignal() != 0) {
                    InstantComparators.onInstantComparator(worldIn, pos, ext.getBuggy()[index]);
                }
            }
        }
    }

    @Inject(method = "updateState", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void logOnPowerChange(World worldIn, BlockPos pos, IBlockState state, CallbackInfo ci, int computedOutput, TileEntity tileentity) {
        int currentOutput = tileentity instanceof TileEntityComparator ? ((TileEntityComparator)tileentity).getOutputSignal() : 0;
        if (LoggerRegistry.__instantComparators && (currentOutput != computedOutput || this.isPowered(state) != this.shouldBePowered(worldIn, pos, state))) {
            if (tileentity instanceof TileEntityComparator) {
                TileEntityComparator comparator = (TileEntityComparator) tileentity;
                int index = (int) Math.floorMod(worldIn.getTotalWorldTime() + 2, 3);
                ExtendedTileEntityComparator ext = (ExtendedTileEntityComparator) comparator;
                ext.getScheduledOutputSignal()[index] = computedOutput;
                ext.getBuggy()[index] = computedOutput == currentOutput;
            } else {
                InstantComparators.onNoTileEntity(worldIn, pos);
            }
        }
    }

    @Redirect(method = "updateState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockTickPending(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)Z"))
    private boolean logOnTickPending(World world, BlockPos pos, Block block, World world2, BlockPos pos2, IBlockState state) {
        if (!world.isBlockTickPending(pos, block)) return false;
        if (LoggerRegistry.__instantComparators) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityComparator) {
                TileEntityComparator comparator = (TileEntityComparator) te;
                int index = (int) Math.floorMod(world.getTotalWorldTime() + 2, 3);
                ExtendedTileEntityComparator ext = (ExtendedTileEntityComparator) comparator;
                ext.getScheduledOutputSignal()[index] = calculateOutput(world, pos, state);
            }
        }
        return true;
    }
}
