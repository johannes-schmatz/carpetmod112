package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;

import redstone.multimeter.RedstoneMultimeter;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class HandshakePacket implements RSMMPacket {
	
	private String modVersion;
	
	public HandshakePacket() {
		modVersion = RedstoneMultimeter.MOD_VERSION;
	}
	
	@Override
	public void encode(NbtCompound data) {
		data.putString("mod version", modVersion);
	}
	
	@Override
	public void decode(NbtCompound data) {
		modVersion = data.getString("mod version");
	}
	
	@Override
	public void execute(MultimeterServer server, ServerPlayerEntity player) {
		server.onHandshake(player, modVersion);
	}
	
	@Override
	public boolean force() {
		return true;
	}
}
