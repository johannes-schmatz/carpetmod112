package carpet;

import carpet.pubsub.PubSubManager;

import java.util.Random;

public class CarpetMod {
	private static CarpetMod instance;

	public static final Random rand = new Random();
	public static final PubSubManager PUBSUB = new PubSubManager();
	public static ThreadLocal<Boolean> playerInventoryStacking = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private CarpetMod() {
		instance = this;
	}

	public static CarpetMod getInstance() {
		return instance;
	}
}
