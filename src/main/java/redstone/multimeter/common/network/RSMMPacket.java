package redstone.multimeter.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;

import redstone.multimeter.server.MultimeterServer;

public interface RSMMPacket {
	
	public void encode(NbtCompound data);
	
	public void decode(NbtCompound data);
	
	public void execute(MultimeterServer server, ServerPlayerEntity player);
	
	/**
	 * Most RSMM packets are ignored if the redstoneMultimeter carpet
	 * rule is not enabled. Some packets are handled anyway in order
	 * to keep RSMM working properly when the carpet rule is toggled.
	 */
	default boolean force() {
		return false;
	}
}
