package redstone.multimeter.common.meter.event;

import net.minecraft.nbt.NbtCompound;

public class MeterEvent {
	
	private EventType type;
	private int metadata;
	
	private MeterEvent() {
		
	}
	
	public MeterEvent(EventType type, int metadata) {
		this.type = type;
		this.metadata = metadata;
	}
	
	public EventType getType() {
		return type;
	}
	
	public int getMetadata() {
		return metadata;
	}
	
	public NbtCompound toNbt() {
		NbtCompound nbt = new NbtCompound();
		
		nbt.put("type", type.toNbt());
		nbt.putInt("metadata", metadata);
		
		return nbt;
	}
	
	public static MeterEvent fromNbt(NbtCompound nbt) {
		MeterEvent event = new MeterEvent();
		
		event.type = EventType.fromNbt(nbt.getCompound("type"));
		event.metadata = nbt.getInt("metadata");
		
		return event;
	}
}
