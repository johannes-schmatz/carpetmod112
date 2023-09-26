package carpet.mixin.fallingBlockResearch;

import carpet.utils.extensions.ExtendedFileIoThread;
import carpet.utils.extensions.ExtendedThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.chunk.storage.io.FileIoCallback;
import net.minecraft.world.chunk.storage.io.FileIoThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(FileIoThread.class)
public class FileIoThreadMixin implements ExtendedFileIoThread {
	private final List<ExtendedThreadedAnvilChunkStorage> deleteCallbacks = Collections.synchronizedList(new ArrayList<>());

	@Inject(
			method = "registerCallback",
			at = @At("HEAD")
	)
	private void onRunCallbacks(FileIoCallback callback, CallbackInfo ci) {
		if (!deleteCallbacks.isEmpty()) {
			synchronized (deleteCallbacks) {
				for (ExtendedThreadedAnvilChunkStorage i : deleteCallbacks) {
					i.deleteScheduled();
				}
				deleteCallbacks.clear();
			}
		}
	}

	@Override
	public void registerDeletionCallback(ExtendedThreadedAnvilChunkStorage callback) {
		deleteCallbacks.add(callback);
	}
}
