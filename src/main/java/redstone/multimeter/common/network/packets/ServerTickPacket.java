package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;

import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class ServerTickPacket implements RSMMPacket {
	
	private long serverTime;
	
	public ServerTickPacket() {
		
	}
	
	public ServerTickPacket(long serverTime) {
		this.serverTime = serverTime;
	}
	
	@Override
	public void encode(NbtCompound data) {
		data.putLong("server time", serverTime);
	}
	
	@Override
	public void decode(NbtCompound data) {
		serverTime = data.getLong("server time");
	}
	
	@Override
	public void execute(MultimeterServer server, ServerPlayerEntity player) {
		
	}
}
