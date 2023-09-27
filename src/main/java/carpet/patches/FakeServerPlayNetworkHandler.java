package carpet.patches;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.network.Connection;
import net.minecraft.network.packet.Packet;

public class FakeServerPlayNetworkHandler extends ServerPlayNetworkHandler {
	public FakeServerPlayNetworkHandler(MinecraftServer server, Connection nm, ServerPlayerEntity playerIn) {
		super(server, nm, playerIn);
	}

	@Override
	public void sendPacket(final Packet<?> packetIn) {
	}

	@Override
	public void sendDisconnect(Text textComponent) {
		player.m_3468489();
	}
}



