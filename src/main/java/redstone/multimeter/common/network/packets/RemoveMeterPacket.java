package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;

import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class RemoveMeterPacket implements RSMMPacket {
	
	private long id;
	
	public RemoveMeterPacket() {
		
	}
	
	public RemoveMeterPacket(long id) {
		this.id = id;
	}
	
	@Override
	public void encode(NbtCompound data) {
		data.putLong("id", id);
	}
	
	@Override
	public void decode(NbtCompound data) {
		id = data.getLong("id");
	}
	
	@Override
	public void execute(MultimeterServer server, ServerPlayerEntity player) {
		server.getMultimeter().removeMeter(player, id);
	}
}
