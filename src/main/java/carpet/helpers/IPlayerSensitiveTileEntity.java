package carpet.helpers;

import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface IPlayerSensitiveTileEntity
{
    BlockEntityUpdateS2CPacket getUpdatePacketPlayerSensitive(ServerPlayerEntity player);
}
