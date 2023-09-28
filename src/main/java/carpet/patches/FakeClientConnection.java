package carpet.patches;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketFlow;

public class FakeClientConnection extends Connection {
	public FakeClientConnection(PacketFlow p) {
		super(p);
	}

	@Override
	public void disableAutoRead() {
	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public void handleDisconnection() {
	}
}
