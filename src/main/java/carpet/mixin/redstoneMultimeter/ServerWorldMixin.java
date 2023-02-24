package carpet.mixin.redstoneMultimeter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import redstone.multimeter.common.TickTask;
import redstone.multimeter.helper.WorldHelper;
import redstone.multimeter.util.DimensionUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.BlockAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ScheduledTick;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.SaveHandler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;

import java.util.Iterator;
import java.util.Random;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
	protected ServerWorldMixin(SaveHandler handler, LevelProperties properties, Dimension dim, Profiler profiler, boolean isClient) {
		super(handler, properties, dim, profiler, isClient);
	}

	private String dimensionName;

	@Inject(
			method = "<init>",
			at = @At("TAIL")
	)
	private void onInit(MinecraftServer server, SaveHandler handler, LevelProperties properties, int dimensionId, Profiler profiler, CallbackInfo ci) {
		dimensionName = DimensionUtils.getId(dimension.getDimensionType()).toString();
	}

	@Inject(
			method = "tick",
			at = @At("HEAD")
	)
	private void onStartTick(CallbackInfo ci) {
		WorldHelper.startTickTask(TickTask.TICK_WORLD, dimensionName); // RSMM
	}

	@Inject(
			method = "awakenPlayers",
			at = @At("HEAD")
	)
	private void startTickTaskAwakenPlayers(CallbackInfo ci) {
		WorldHelper.startTickTask(TickTask.WAKE_SLEEPING_PLAYERS);
	}

	@Inject(
			method = "awakenPlayers",
			at = @At("TAIL")
	)
	private void endTickTaskAwakenPlayers(CallbackInfo ci) {
		WorldHelper.endTickTask();
	}


	@Inject(
			method = "tickBlocks",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/chunk/Chunk;populateBlockEntities(Z)V",
					ordinal = 1
			)
	)
	private void startTickTaskTickChunk(CallbackInfo ci) {
		WorldHelper.swapTickTask(false, TickTask.TICK_CHUNK);
	}

	@Inject(
			method = "tickBlocks",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
					ordinal = 2,
					shift = At.Shift.AFTER
			)
	)
	private void swapTickTaskThunder(CallbackInfo ci) {
		WorldHelper.swapTickTask(false, TickTask.THUNDER);
	}

	@Inject(
			method = "tickBlocks",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
					ordinal = 3,
					shift = At.Shift.AFTER
			)
	)
	private void swapTickTaskPrecipitation(CallbackInfo ci) {
		WorldHelper.swapTickTask(false, TickTask.PRECIPITATION);
	}

	@Inject(
			method = "tickBlocks",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
					ordinal = 4,
					shift = At.Shift.AFTER
			)
	)
	private void swapTickTaskRandomTicks(CallbackInfo ci) {
		WorldHelper.swapTickTask(false, TickTask.RANDOM_TICKS);
	}

	@Redirect(
			method = "tickBlocks",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/Block;onRandomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Ljava/util/Random;)V"
			)
	)
	private void onRandomTick(Block block, World world, BlockPos pos, BlockState state, Random random) {
		WorldHelper.onRandomTick(world, pos);
		block.onRandomTick(world, pos, state, random);
	}

	@Inject(
			method = "tickBlocks",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;pop()V",
					ordinal = 1
			)
	)
	private void endTickTaskRandomTicks(CallbackInfo ci) {
		WorldHelper.endTickTask(false); // TODO: check if this actually works
	}

	@Inject(
			method = "method_11491",
			at = @At("HEAD")
	)
	private void startTickTaskPlayers(CallbackInfo ci) {
		WorldHelper.startTickTask(TickTask.PACKETS);
	}

	@Inject(
			method = "method_11491",
			at = @At("TAIL")
	)
	private void endTickTaskPlayers(CallbackInfo ci) {
		WorldHelper.endTickTask();
	}

	@Inject(
			method = "method_3644",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;",
					ordinal = 1
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onScheduledTick(boolean bl, CallbackInfoReturnable<Boolean> cir, Iterator iterator, ScheduledTick lv2, int k, BlockState lv3) {
		WorldHelper.onScheduledTick((ServerWorld) ((Object) this), lv2);
	}

	@Inject(
			method = "method_2131",
			at = @At("HEAD")
	)
	private void startTickTaskBlockEvents(CallbackInfo ci) {
		WorldHelper.startTickTask(TickTask.BLOCK_EVENTS);

		WorldHelper.currentBlockEventDepth = 0;
	}

	@Inject(
			method = "method_2131",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/world/ServerWorld$BlockActionList;clear()V",
					shift = At.Shift.AFTER
			)
	)
	private void currentBlockEventDepth(CallbackInfo ci) {
		WorldHelper.currentBlockEventDepth++;
	}

	@Inject(
			method = "method_2131",
			at = @At("TAIL")
	)
	private void endTickTaskBlockEvents(CallbackInfo ci) {
		WorldHelper.endTickTask();
	}

	@Inject(
			method = "method_2137",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onBlockEvent(BlockAction arg, CallbackInfoReturnable<Boolean> cir, BlockState lv) {
		if (lv.getBlock() == arg.getBlock()) {
			WorldHelper.onBlockEvent((ServerWorld) ((Object) this), arg);
		}
	}

	@Inject(
			method = "tickWeather",
			at = @At("HEAD")
	)
	private void startTickTaskWeather(CallbackInfo ci) {
		WorldHelper.startTickTask(TickTask.WEATHER);
	}

	@Inject(
			method = "tickWeather",
			at = @At("TAIL")
	)
	private void endTickTaskWeather(CallbackInfo ci) {
		WorldHelper.endTickTask();
	}
}
