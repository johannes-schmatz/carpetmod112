package carpet.worldedit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;

import carpet.CarpetServer;

/**
 * Caches data that cannot be accessed from another thread safely.
 */
class ThreadSafeCache {

	private static final long REFRESH_DELAY = 1000 * 30;
	private static final ThreadSafeCache INSTANCE = new ThreadSafeCache();
	private Set<UUID> onlineIds = Collections.emptySet();
	private long lastRefresh = 0;

	/**
	 * Get an concurrent-safe set of UUIDs of online players.
	 *
	 * @return a set of UUIDs
	 */
	public Set<UUID> getOnlineIds() {
		return onlineIds;
	}

	public void tickStart() {
		long now = System.currentTimeMillis();

		if (now - lastRefresh > REFRESH_DELAY) {
			Set<UUID> onlineIds = new HashSet<UUID>();

			for (Object object : CarpetServer.getMinecraftServer().getPlayerManager().getAll()) {
				if (object != null) {
					ServerPlayerEntity player = (ServerPlayerEntity) object;
					onlineIds.add(player.getUuid());
				}
			}

			this.onlineIds = new CopyOnWriteArraySet<UUID>(onlineIds);

			lastRefresh = now;
		}
	}

	public static ThreadSafeCache getInstance() {
		return INSTANCE;
	}

}
