package carpet.mixin.redstoneMultimeter;

import carpet.CarpetServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import redstone.multimeter.common.TickTask;
import redstone.multimeter.helper.WorldHelper;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(
			method = "setupWorld()V",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/server/MinecraftServer;profiling:Z",
					ordinal = 0
			)
	)
	private void onStartTick(CallbackInfo ci) {
		CarpetServer.getInstance().rsmmServer.tickStart();
		WorldHelper.startTickTask(TickTask.TICK);
	}

	@Inject(
			method = "setupWorld()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V",
					ordinal = 1
			)
	)
	private void startTickTaskAutosave(CallbackInfo ci) {
		WorldHelper.startTickTask(TickTask.AUTOSAVE);
	}
	@Inject(
			method = "setupWorld()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;pop()V",
					ordinal = 0
			)
	)
	private void endTickTaskAutosave(CallbackInfo ci) {
		WorldHelper.endTickTask();
	}

	@Inject(
			method = "setupWorld()V",
			at = @At("TAIL")
	)
	private void onEndTick(CallbackInfo ci) {
		WorldHelper.endTickTask();
		CarpetServer.getInstance().rsmmServer.tickEnd();
	}

	@Inject(
			method = "tick()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V",
					ordinal = 0,
					shift = At.Shift.AFTER
			)
	)
	private void startTickTaskJobs(CallbackInfo ci) {
		WorldHelper.startTickTask(TickTask.PACKETS);
	}

	@Inject(
			method = "tick()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
					ordinal = 0,
					shift = At.Shift.AFTER
			)
	)
	private void swapTickTaskLevels(CallbackInfo ci) {
		WorldHelper.swapTickTask(TickTask.LEVELS);
	}

	@Inject(
			method = "tick()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/world/ServerWorld;tickEntities()V"
			)
	)
	private void startTickTaskEntities(CallbackInfo ci) {
		WorldHelper.startTickTask(TickTask.ENTITIES);
	}

	@Inject(
			method = "tick()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/world/ServerWorld;tickEntities()V",
					shift = At.Shift.AFTER
			)
	)
	private void endTickTaskEntities(CallbackInfo ci) {
		WorldHelper.endTickTask();
	}

	@Inject(
			method = "tick()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
					ordinal = 1,
					shift = At.Shift.AFTER
			)
	)
	private void swapTickTaskConnections(CallbackInfo ci) {
		WorldHelper.swapTickTask(TickTask.CONNECTIONS);
	}

	@Inject(
			method = "tick()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
					ordinal = 2,
					shift = At.Shift.AFTER
			)
	)
	private void swapTickTaskPlayerPing(CallbackInfo ci) {
		WorldHelper.swapTickTask(TickTask.PLAYER_PING);
	}

	@Inject(
			method = "tick()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
					ordinal = 3,
					shift = At.Shift.AFTER
			)
	)
	private void swapTickTaskCommandFunctions(CallbackInfo ci) {
		WorldHelper.swapTickTask(TickTask.COMMAND_FUNCTIONS);
	}

	@Inject(
			method = "tick()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
					ordinal = 4,
					shift = At.Shift.AFTER
			)
	)
	private void swapTickTaskServerGui(CallbackInfo ci) {
		WorldHelper.swapTickTask(TickTask.SERVER_GUI);
	}

	@Inject(
			method = "tick()V",
			at = @At("TAIL")
	)
	private void endTickTask(CallbackInfo ci) {
		WorldHelper.endTickTask();
	}

	@Inject(
			method = "method_14912",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;",
					ordinal = 0
			)
	)
	private void reloadOptions(CallbackInfo ci) {
		CarpetServer.getInstance().rsmmServer.getMultimeter().reloadOptions();
	}

	// TODO interf with this
	public boolean isPaused() {
		return false;
	}
}
