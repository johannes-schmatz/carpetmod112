package carpet.utils;

import carpet.helpers.HopperCounter;
import carpet.helpers.TickSpeed;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.PacketCounter;
import carpet.mixin.accessors.PlayerListHeaderS2CPacketAccessor;

import net.minecraft.entity.living.mob.MobCategory;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.TabListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HUDController {
	public static Map<PlayerEntity, List<Text>> player_huds = new HashMap<>();

	public static void addMessage(PlayerEntity player, Text hudMessage) {
		if (player_huds.containsKey(player)) {
			player_huds.get(player).add(new LiteralText("\n"));
		} else {
			player_huds.put(player, new ArrayList<>());
		}
		player_huds.get(player).add(hudMessage);
	}

	public static void clear_player(PlayerEntity player) {
		TabListS2CPacket packet = new TabListS2CPacket();
		PlayerListHeaderS2CPacketAccessor acc = (PlayerListHeaderS2CPacketAccessor) packet;
		acc.setHeader(new LiteralText(""));
		acc.setFooter(new LiteralText(""));
		((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
	}


	public static void update_hud(MinecraftServer server) {
		if (server.getTicks() % 20 != 0) return;

		player_huds.clear();

		if (LoggerRegistry.__autosave) log_autosave(server);

		if (LoggerRegistry.__tps) log_tps(server);

		if (LoggerRegistry.__mobcaps) log_mobcaps();

		if (LoggerRegistry.__counter) log_counter(server);

		if (LoggerRegistry.__packets)
			LoggerRegistry.getLogger("packets").log(() -> packetCounter(), "TOTAL_IN", PacketCounter.totalIn, "TOTAL_OUT", PacketCounter.totalOut);

		for (PlayerEntity player : player_huds.keySet()) {
			TabListS2CPacket packet = new TabListS2CPacket();
			PlayerListHeaderS2CPacketAccessor acc = (PlayerListHeaderS2CPacketAccessor) packet;
			acc.setHeader(new LiteralText(""));
			acc.setFooter(Messenger.m(null, player_huds.get(player).toArray(new Object[0])));
			((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
		}
	}

	private static void log_autosave(MinecraftServer server) {
		int gametick = server.getTicks();
		int previous = gametick % 900;

		if (gametick != 0 && previous == 0) {
			previous = 900;
		}
		int next = 900 - previous;
		String color = Messenger.heatmap_color(previous, 860);
		Text[] message = {
				Messenger.m(null, "g Prev: ", String.format(Locale.US, "%s %d", color, previous), "g  Next: ", String.format(Locale.US, "%s %d", color, next))
		};
		LoggerRegistry.getLogger("autosave").log(() -> message, "Prev", previous, "Next", next);
	}

	private static void log_tps(MinecraftServer server) {
		double MSPT = MathHelper.average(server.averageTickTimes) * 1.0E-6D;
		double TPS = 1000.0D / Math.max((TickSpeed.time_warp_start_time != 0) ? 0.0 : TickSpeed.mspt, MSPT);
		String color = Messenger.heatmap_color(MSPT, TickSpeed.mspt);
		Text[] message = new Text[]{
				Messenger.m(null, "g TPS: ", String.format(Locale.US, "%s %.1f", color, TPS), "g  MSPT: ", String.format(Locale.US, "%s %.1f", color, MSPT))
		};
		LoggerRegistry.getLogger("tps").log(() -> message, "MSPT", MSPT, "TPS", TPS);
	}

	private static void log_mobcaps() {
		List<Object> commandParams = new ArrayList<>();
		for (int dim = -1; dim <= 1; dim++) {
			for (MobCategory type : MobCategory.values()) {
				Pair<Integer, Integer> counts = SpawnReporter.mobcaps.get(dim).getOrDefault(type, new Pair<>(0, 0));
				int actual = counts.getLeft(), limit = counts.getRight();
				Collections.addAll(commandParams, type.name() + "_ACTUAL_DIM_" + dim, actual, type.name() + "_ACTUAL_LIMIT_" + dim, limit);
			}
		}
		LoggerRegistry.getLogger("mobcaps").log((option, player) -> {
			int dim = player.dimensionId;
			switch (option) {
				case "overworld":
					dim = 0;
					break;
				case "nether":
					dim = -1;
					break;
				case "end":
					dim = 1;
					break;
			}
			return send_mobcap_display(dim);
		}, commandParams.toArray());
	}

	private static Text[] send_mobcap_display(int dim) {
		List<Text> components = new ArrayList<>();
		for (MobCategory type : MobCategory.values()) {
			Pair<Integer, Integer> counts = SpawnReporter.mobcaps.get(dim).getOrDefault(type, new Pair<>(0, 0));
			int actual = counts.getLeft();
			int limit = counts.getRight();
			components.add(Messenger.m(null,
					(actual + limit == 0) ? "g -" : Messenger.heatmap_color(actual, limit) + " " + actual,
					Messenger.creatureTypeColor(type) + " /" + ((actual + limit == 0) ? "-" : limit)
			));
			components.add(Messenger.m(null, "w  "));
		}
		components.remove(components.size() - 1);
		return new Text[]{Messenger.m(null, components.toArray(new Object[0]))};
	}

	private static void log_counter(MinecraftServer server) {
		List<Object> commandParams = new ArrayList<>();
		for (HopperCounter counter : HopperCounter.COUNTERS.values())
			Collections.addAll(commandParams, counter.color.name(), counter.getTotalItems());
		LoggerRegistry.getLogger("counter").log((option) -> send_counter_info(server, option), commandParams);
	}

	private static Text[] send_counter_info(MinecraftServer server, String color) {
		HopperCounter counter = HopperCounter.getCounter(color);
		List<Text> res = counter == null ? Collections.emptyList() : counter.format(server, false, true);
		return new Text[]{Messenger.m(null, res.toArray(new Object[0]))};
	}

	private static Text[] packetCounter() {
		Text[] ret = {Messenger.m(null, "w I/" + PacketCounter.totalIn + " O/" + PacketCounter.totalOut),
		};
		PacketCounter.reset();
		return ret;
	}
}
