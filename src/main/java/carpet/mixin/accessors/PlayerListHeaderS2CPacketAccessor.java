package carpet.mixin.accessors;

import net.minecraft.network.packet.s2c.play.TabListS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TabListS2CPacket.class)
public interface PlayerListHeaderS2CPacketAccessor {
    @Accessor void setHeader(Text header);
    @Accessor void setFooter(Text footer);
}
