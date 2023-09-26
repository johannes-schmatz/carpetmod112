package carpet.worldedit;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import com.sk89q.worldedit.LocalSession;

class WECUIPacketHandler {
    public static final Charset UTF_8_CHARSET = StandardCharsets.UTF_8;

    public static void onCustomPayload(CustomPayloadC2SPacket rawPacket, ServerPlayerEntity player) {
        if (rawPacket.getChannel().equals(CarpetWorldEdit.CUI_PLUGIN_CHANNEL)) {
            LocalSession session = CarpetWorldEdit.inst.getSession(player);

            if (session.hasCUISupport()) {
                return;
            }
        
            PacketByteBuf buff = rawPacket.getData();
            buff.resetReaderIndex();
            byte[] bytes = new byte[buff.readableBytes()];
            buff.readBytes(bytes);
            String text = new String(bytes, UTF_8_CHARSET);
            session.handleCUIInitializationMessage(text);
        }
    }

}