package carpet.patches;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;

public class FakeServerPlayNetworkHandler extends ServerPlayNetworkHandler {
    public FakeServerPlayNetworkHandler(MinecraftServer server, ClientConnection nm, ServerPlayerEntity playerIn) {
        super(server, nm, playerIn);
    }

    @Override
    public void sendPacket(final Packet<?> packetIn) {
    }

    @Override
    public void method_14977(Text textComponent) {
        player.kill();
    }
}



