package redstone.multimeter.common.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;

import redstone.multimeter.common.meter.MeterGroup;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.Multimeter;
import redstone.multimeter.server.MultimeterServer;

public class MeterGroupRefreshPacket implements RSMMPacket {
	
	private String name;
	private NbtCompound meterGroupData;
	
	public MeterGroupRefreshPacket() {
		
	}
	
	public MeterGroupRefreshPacket(MeterGroup meterGroup) {
		this.name = meterGroup.getName();
		this.meterGroupData = meterGroup.toNbt();
	}
	
	@Override
	public void encode(NbtCompound data) {
		data.putString("name", name);
		data.put("data", meterGroupData);
	}
	
	@Override
	public void decode(NbtCompound data) {
		name = data.getString("name");
		meterGroupData = data.getCompound("data");
	}
	
	@Override
	public void execute(MultimeterServer server, ServerPlayerEntity player) {
		Multimeter multimeter = server.getMultimeter();
		
		if (multimeter.hasSubscription(player)) {
			multimeter.refreshMeterGroup(player);
		}
	}
}
