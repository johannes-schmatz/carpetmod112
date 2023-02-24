package redstone.multimeter.server;

import java.util.Collection;

import carpet.CarpetSettings;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.PacketByteBuf;

import carpet.network.PluginChannelHandler;
import redstone.multimeter.common.network.AbstractPacketHandler;
import redstone.multimeter.common.network.PacketManager;
import redstone.multimeter.common.network.RSMMPacket;
import redstone.multimeter.server.meter.ServerMeterGroup;

public class ServerPacketHandler extends AbstractPacketHandler implements PluginChannelHandler {
	
	private final MultimeterServer server;
	
	public ServerPacketHandler(MultimeterServer server) {
		this.server = server;
	}

	@Override
	protected Packet<?> toCustomPayload(String id, PacketByteBuf buffer) {
		return new CustomPayloadS2CPacket(id, buffer);
	}
	
	@Override
	public <P extends RSMMPacket> void send(P packet) {
		Packet<?> mcPacket = encode(packet);
		server.getPlayerManager().sendToAll(mcPacket);
		// TODO: use carpet plugin channels?
	}
	
	public <P extends RSMMPacket> void sendToPlayer(P packet, ServerPlayerEntity player) {
		player.networkHandler.sendPacket(encode(packet));
		// TODO: use carpet plugin channels?
	}
	
	public <P extends RSMMPacket> void sendToPlayers(P packet, Collection<ServerPlayerEntity> players) {
		Packet<?> mcPacket = encode(packet);
		// TODO: use carpet plugin channels?
		
		for (ServerPlayerEntity player : players) {
			player.networkHandler.sendPacket(mcPacket);
		}
	}
	
	public <P extends RSMMPacket> void sendToSubscribers(P packet, ServerMeterGroup meterGroup) {
		sendToPlayers(packet, server.collectPlayers(meterGroup.getSubscribers()));
	}
	
	public void onPacketReceived(PacketByteBuf buffer, ServerPlayerEntity player) {
		try {
			RSMMPacket packet = decode(buffer);
			
			if (CarpetSettings.redstoneMultimeter || packet.force()) {
				packet.execute(server, player);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String[] getChannels() {
		return new String[]{PacketManager.getPacketChannelId()};
	}

	@Override
	public void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player) {
		onPacketReceived(packet.getPayload(), player);
	}
}
