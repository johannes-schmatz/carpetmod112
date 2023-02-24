package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;

import redstone.multimeter.common.meter.MeterProperties;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;

public class AddMeterPacket implements RSMMPacket {
	
	private MeterProperties properties;
	
	public AddMeterPacket() {
		
	}
	
	public AddMeterPacket(MeterProperties properties) {
		this.properties = properties;
	}
	
	@Override
	public void encode(NbtCompound data) {
		data.put("properties", properties.toNbt());
	}
	
	@Override
	public void decode(NbtCompound data) {
		properties = MeterProperties.fromNbt(data.getCompound("properties"));
	}
	
	@Override
	public void execute(MultimeterServer server, ServerPlayerEntity player) {
		server.getMultimeter().addMeter(player, properties);
	}
}
