package carpet.mixin.redstoneMultimeter;

import carpet.CarpetSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import redstone.multimeter.helper.WorldHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

@Mixin(Chunk.class)
public class ChunkMixin {
	@Shadow @Final private World world;

	@Inject(
			method = "getBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/chunk/ChunkSection;setBlockState(IIILnet/minecraft/block/BlockState;)V",
					shift = At.Shift.AFTER
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void afterSetBlockState(BlockPos pos, BlockState state, CallbackInfoReturnable<BlockState> cir,
			int i, int j, int k, int l, int m, BlockState lv, Block lv2, Block lv3, ChunkSection lv4, boolean bl) {
		if (CarpetSettings.redstoneMultimeter && !world.isClient) {
			WorldHelper.getMultimeter().onBlockChange(world, pos, lv, state);
		}
	}
}
