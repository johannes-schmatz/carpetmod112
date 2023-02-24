package redstone.multimeter.common.meter.log;

import redstone.multimeter.common.TickPhase;
import redstone.multimeter.common.meter.event.MeterEvent;

import net.minecraft.nbt.NbtCompound;

public class EventLog {
	
	private long tick;
	private int subtick;
	private TickPhase tickPhase;
	private MeterEvent event;
	
	private EventLog() {
		
	}
	
	public EventLog(long tick, int subtick, TickPhase tickPhase, MeterEvent event) {
		this.tick = tick;
		this.subtick = subtick;
		this.tickPhase = tickPhase;
		this.event = event;
	}
	
	public long getTick() {
		return tick;
	}
	
	public int getSubtick() {
		return subtick;
	}
	
	public boolean isAt(long tick) {
		return this.tick == tick;
	}
	
	public boolean isAt(long tick, int subtick) {
		return this.tick == tick && this.subtick == subtick;
	}
	
	public boolean isBefore(long tick) {
		return this.tick < tick;
	}
	
	public boolean isBefore(long tick, int subtick) {
		if (this.tick == tick) {
			return this.subtick < subtick;
		}
		
		return this.tick < tick;
	}
	
	public boolean isBefore(EventLog event) {
		return isBefore(event.getTick(), event.getSubtick());
	}
	
	public boolean isAfter(long tick) {
		return this.tick > tick;
	}
	
	public boolean isAfter(long tick, int subtick) {
		if (this.tick == tick) {
			return this.subtick > subtick;
		}
		
		return this.tick > tick;
	}
	
	public boolean isAfter(EventLog event) {
		return isAfter(event.getTick(), event.getSubtick());
	}
	
	public TickPhase getTickPhase() {
		return tickPhase;
	}
	
	public MeterEvent getEvent() {
		return event;
	}
	
	public NbtCompound toNbt() {
		NbtCompound nbt = new NbtCompound();
		
		nbt.put("meter event", event.toNbt());
		nbt.putLong("tick", tick);
		nbt.putInt("subtick", subtick);
		nbt.put("tick phase", tickPhase.toNbt());
		
		return nbt;
	}
	
	public static EventLog fromNbt(NbtCompound nbt) {
		EventLog log = new EventLog();
		
		log.event = MeterEvent.fromNbt(nbt.getCompound("meter event"));
		log.tick = nbt.getLong("tick");
		log.subtick = nbt.getInt("subtick");
		log.tickPhase = TickPhase.fromNbt(nbt.getCompound("tick phase"));
		
		return log;
	}
}
