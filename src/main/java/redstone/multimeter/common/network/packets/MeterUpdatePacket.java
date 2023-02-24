package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;

import redstone.multimeter.common.meter.MeterProperties;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class MeterUpdatePacket implements RSMMPacket {
	
	private long id;
	private MeterProperties properties;
	
	public MeterUpdatePacket() {
		
	}
	
	public MeterUpdatePacket(long id, MeterProperties properties) {
		this.id = id;
		this.properties = properties;
	}
	
	@Override
	public void encode(NbtCompound data) {
		data.putLong("id", id);
		data.put("properties", properties.toNbt());
	}
	
	@Override
	public void decode(NbtCompound data) {
		id = data.getLong("id");
		properties = MeterProperties.fromNbt(data.getCompound("properties"));
	}
	
	@Override
	public void execute(MultimeterServer server, ServerPlayerEntity player) {
		server.getMultimeter().updateMeter(player, id, properties);
	}
}
