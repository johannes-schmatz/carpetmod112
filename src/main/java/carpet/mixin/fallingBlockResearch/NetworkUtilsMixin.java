package carpet.mixin.fallingBlockResearch;

import carpet.helpers.FallingBlockResearchHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.util.NetworkUtils;

import java.util.concurrent.ThreadFactory;

@Mixin(NetworkUtils.class)
public class NetworkUtilsMixin {
	@Redirect(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/util/concurrent/ThreadFactoryBuilder;build()Ljava/util/concurrent/ThreadFactory;"
			)
	)
	private static ThreadFactory build(ThreadFactoryBuilder instance) {
		instance.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
			System.out.println("Thread " + t + " crashed:");
			e.printStackTrace();
		});
		ThreadFactory factory = instance.build();
		return (Runnable runnable) -> {
			if (true) {
				return factory.newThread(() -> {
					try {
						runnable.run();
					} finally {
						System.out.println("Beacon thread exiting.");
					}
				});
			} else {
				return factory.newThread(runnable);
			}
		};
	}
}
