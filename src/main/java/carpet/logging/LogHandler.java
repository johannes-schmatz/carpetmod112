package carpet.logging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;

import carpet.CarpetServer;
import carpet.utils.HUDController;

public abstract class LogHandler {

	public static final LogHandler CHAT = new LogHandler() {
		@Override
		public void handle(ServerPlayerEntity player, Text[] message, Object[] commandParams) {
			Arrays.stream(message).forEach(player::sendMessage);
		}
	};
	public static final LogHandler HUD = new LogHandler() {
		@Override
		public void handle(ServerPlayerEntity player, Text[] message, Object[] commandParams) {
			for (Text m : message)
				HUDController.addMessage(player, m);
		}

		@Override
		public void onRemovePlayer(String playerName) {
			ServerPlayerEntity player = CarpetServer.getMinecraftServer().getPlayerManager().get(playerName);
			if (player != null) HUDController.clear_player(player);
		}
	};

	private static final Map<String, LogHandlerCreator> CREATORS = new HashMap<>();

	static {
		registerCreator("chat", extraArgs -> CHAT);
		registerCreator("hud", extraArgs -> HUD);
		registerCreator("command", CommandLogHandler::new);
	}

	@FunctionalInterface
	private interface LogHandlerCreator {
		LogHandler create(String... extraArgs);
	}

	private static void registerCreator(String name, LogHandlerCreator creator) {
		CREATORS.put(name, creator);
	}

	public static LogHandler createHandler(String name, String... extraArgs) {
		LogHandler handler = CREATORS.get(name).create(extraArgs);
		handler.name = name;
		handler.extraArgs = extraArgs;
		return handler;
	}

	public static List<String> getHandlerNames() {
		return CREATORS.keySet().stream().sorted().collect(Collectors.toList());
	}

	private String name;
	private String[] extraArgs;

	public String getName() {
		return name;
	}

	public String[] getExtraArgs() {
		return extraArgs;
	}

	public abstract void handle(ServerPlayerEntity player, Text[] message, Object[] commandParams);

	public void onAddPlayer(String playerName) {
	}

	public void onRemovePlayer(String playerName) {
	}

}
