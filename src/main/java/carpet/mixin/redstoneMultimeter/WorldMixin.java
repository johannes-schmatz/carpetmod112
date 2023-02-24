package carpet.mixin.redstoneMultimeter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import redstone.multimeter.common.TickTask;
import redstone.multimeter.helper.WorldHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Iterator;

@Mixin(World.class)
public class WorldMixin {
	@Inject(
			method = "method_13691",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/ObserverBlock;method_13711(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;" +
							"Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V"
			)
	)
	private void onObserverUpdate(BlockPos pos, Block block, BlockPos sourcePos, CallbackInfo ci) {
		WorldHelper.onObserverUpdate((World) ((Object) this), pos);
	}

	@Inject(
			method = "tickEntities",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V",
					ordinal = 1
			)
	)
	private void startTickTaskGlobalEntities(CallbackInfo ci) {
		WorldHelper.startTickTask(TickTask.GLOBAL_ENTITIES); // RSMM
	}

	@Inject(
			method = "tickEntities",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;tick()V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onEntityTicked(CallbackInfo ci, int i, Entity lv) {
		WorldHelper.onEntityTick((World) ((Object) this), lv);
	}

	@Inject(
			method = "tickEntities",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
					ordinal = 1
			)
	)
	private void swapTickTaskRegularEntities(CallbackInfo ci) {
		WorldHelper.swapTickTask(TickTask.REGULAR_ENTITIES); // RSMM
	}

	@Inject(
			method = "tickEntities",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
					ordinal = 2
			)
	)
	private void swapTickTaskBlockEntities(CallbackInfo ci) {
		WorldHelper.swapTickTask(TickTask.BLOCK_ENTITIES); // RSMM
	}

	@Inject(
			method = "tickEntities",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/Tickable;tick()V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onBlockEntityTick(CallbackInfo ci, Iterator<?> iterator, BlockEntity lv9, BlockPos lv10, Iterator<?> injectorAllocatedLocal7) {
		WorldHelper.onBlockEntityTick((World) ((Object) this), lv9);
	}

	@Inject(
			method = "tickEntities",
			at = @At("TAIL")
	)
	private void endTickTaskBlockEntities(CallbackInfo ci) {
		WorldHelper.endTickTask();
	}

	@Inject(
			method = "updateHorizontalAdjacent",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;neighbourUpdate(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" +
							"Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onComparatorUpdate(BlockPos pos, Block block, CallbackInfo ci,
			Iterator<?> var3, Direction lv, BlockPos lv2, BlockState lv3) {
		WorldHelper.onComparatorUpdate((World) ((Object) this), lv2);
	}
}
