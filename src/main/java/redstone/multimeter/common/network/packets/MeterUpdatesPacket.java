package redstone.multimeter.common.network.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;

import redstone.multimeter.common.meter.MeterProperties;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.MultimeterServer;
import redstone.multimeter.util.NbtUtils;

public class MeterUpdatesPacket implements RSMMPacket {
	
	private List<Long> removedMeters;
	private Long2ObjectMap<MeterProperties> meterUpdates;
	
	public MeterUpdatesPacket() {
		this.removedMeters = new ArrayList<>();
		this.meterUpdates = new Long2ObjectOpenHashMap<>();
	}
	
	public MeterUpdatesPacket(List<Long> removedMeters, Map<Long, MeterProperties> updates) {
		this.removedMeters = new ArrayList<>(removedMeters);
		this.meterUpdates = new Long2ObjectOpenHashMap<>(updates);
	}
	
	@Override
	public void encode(NbtCompound data) {
		NbtList ids = new NbtList();
		NbtList list = new NbtList();
		
		for (int index = 0; index < removedMeters.size(); index++) {
			long id = removedMeters.get(index);
			
			NbtLong nbt = new NbtLong(id);
			ids.add(nbt);
		}
		for (Entry<MeterProperties> entry : meterUpdates.long2ObjectEntrySet()) {
			long id = entry.getLongKey();
			MeterProperties update = entry.getValue();
			
			NbtCompound nbt = update.toNbt();
			nbt.putLong("id", id);
			list.add(nbt);
		}
		
		data.put("removed meters", ids);
		data.put("meter updates", list);
	}
	
	@Override
	public void decode(NbtCompound data) {
		NbtList ids = data.getList("removed meters", NbtUtils.TYPE_LONG);
		NbtList list = data.getList("meter updates", NbtUtils.TYPE_COMPOUND);
		
		for (int index = 0; index < ids.size(); index++) {
			NbtElement nbt = ids.get(index);
			
			if (nbt.getType() == NbtUtils.TYPE_LONG) {
				removedMeters.add(((NbtLong)nbt).longValue());
			}
		}
		for (int index = 0; index < list.size(); index++) {
			NbtCompound nbt = list.getCompound(index);
			
			long id = nbt.getLong("id");
			MeterProperties update = MeterProperties.fromNbt(nbt);
			meterUpdates.put(id, update);
		}
	}
	
	@Override
	public void execute(MultimeterServer server, ServerPlayerEntity player) {
		
	}
}
