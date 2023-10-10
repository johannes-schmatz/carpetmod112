package carpet.logging;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Logger {
	/** Reference to the minecraft server. Used to look players up by name. */
	private final MinecraftServer server;

	/** The set of subscribed players. */
	private final Map<String, String> subscribedPlayers;

	/** The logName of this log. Gets prepended to logged messages. */
	private final String logName;

	@Nullable
	private final String defaultOption;

	private final List<String> options;

	private final LogHandler defaultHandler;

	/** The map of player names to the log handler used */
	private final Map<String, LogHandler> handlers;
	/** Added boolean to create a sublist of loggers as a debugger list and use this boolean to distinguish the two. */
	private boolean debugger = false;
	private boolean generic = false;

	public Logger(MinecraftServer server, String logName, @Nullable String def, @Nullable String[] options, LogHandler defaultHandler) {
		this.server = server;
		this.subscribedPlayers = new HashMap<>();
		this.logName = logName;
		this.defaultOption = def;
		if (options == null) {
			this.options = Collections.emptyList();
		} else {
			this.options = Arrays.asList(options);
		}
		this.defaultHandler = defaultHandler;
        this.handlers = new HashMap<>();
	}

	@Nullable
	public String getDefault() {
		return defaultOption;
	}

	public List<String> getOptions() {
		return options;
	}

	public String getLogName() {
		return logName;
	}

	/**
	 * Subscribes the player with the given logName to the logger.
	 */
	public void addPlayer(String playerName, String option, LogHandler handler) {
		subscribedPlayers.put(playerName, option);
		if (handler == null) handler = defaultHandler;
		handlers.put(playerName, handler);
		handler.onAddPlayer(playerName);
		LoggerRegistry.setAccess(this);
	}

	/**
	 * Unsubscribes the player with the given logName from the logger.
	 */
	public void removePlayer(String playerName) {
		handlers.getOrDefault(playerName, defaultHandler).onRemovePlayer(playerName);
		subscribedPlayers.remove(playerName);
		handlers.remove(playerName);
		LoggerRegistry.setAccess(this);
	}

	/**
	 * Sets the LogHandler for the given player
	 */
	public void setHandler(String playerName, LogHandler newHandler) {
		if (newHandler == null) newHandler = defaultHandler;
		LogHandler oldHandler = handlers.getOrDefault(playerName, defaultHandler);
		if (oldHandler != newHandler) {
			oldHandler.onRemovePlayer(playerName);
			handlers.put(playerName, newHandler);
			newHandler.onAddPlayer(playerName);
		}
	}

	/**
	 * Returns true if there are any online subscribers for this log.
	 */
	public boolean hasSubscribers() {
		return !subscribedPlayers.isEmpty();
	}

	public Logger asDebugger() {
		debugger = true;
		return this;
	}

	public Logger asGeneric() {
		generic = true;
		return this;
	}

	public boolean debuggerFilter(int compareDebugger) {
		return 0 == compareDebugger || !debugger && !generic && 1 == compareDebugger || debugger && 2 == compareDebugger || generic && 3 == compareDebugger;
	}

	/**
	 * serves messages to players fetching them from the promise will repeat invocation for players that share the same option
	 */
	@FunctionalInterface
	public interface lMessage {
		Text[] get(String playerOption, PlayerEntity player);
	}

	public void logNoCommand(lMessage messagePromise) {
		log(messagePromise, (Object[]) null);
	}

	public void log(lMessage messagePromise, Object... commandParams) {
		for (Map.Entry<String, String> en : subscribedPlayers.entrySet()) {
			ServerPlayerEntity player = playerFromName(en.getKey());
			if (player != null) {
				Text[] messages = messagePromise.get(en.getValue(), player);
				if (messages != null) sendPlayerMessage(en.getKey(), player, messages, commandParams);
			}
		}
	}

	/**
	 * guarantees that each message for each option will be evaluated once from the promise and served the same way to all other players subscribed to the same
	 * option
	 */
	@FunctionalInterface
	public interface lMessageIgnorePlayer {
		Text[] get(String playerOption);
	}

	public void logNoCommand(lMessageIgnorePlayer messagePromise) {
		log(messagePromise, (Object[]) null);
	}

	public void log(lMessageIgnorePlayer messagePromise, Object... commandParams) {
		Map<String, Text[]> cannedMessages = new HashMap<>();
		for (Map.Entry<String, String> en : subscribedPlayers.entrySet()) {
			ServerPlayerEntity player = playerFromName(en.getKey());
			if (player != null) {
				String option = en.getValue();
				if (!cannedMessages.containsKey(option)) {
					cannedMessages.put(option, messagePromise.get(option));
				}
				Text[] messages = cannedMessages.get(option);
				if (messages != null) sendPlayerMessage(en.getKey(), player, messages, commandParams);
			}
		}
	}

	/**
	 * guarantees that message is evaluated once, so independent from the player and chosen option
	 */
	public void logNoCommand(Supplier<Text[]> messagePromise) {
		log(messagePromise, (Object[]) null);
	}

	public void log(Supplier<Text[]> messagePromise, Object... commandParams) {
		Text[] cannedMessages = null;
		for (Map.Entry<String, String> en : subscribedPlayers.entrySet()) {
			ServerPlayerEntity player = playerFromName(en.getKey());
			if (player != null) {
				if (cannedMessages == null) cannedMessages = messagePromise.get();
				sendPlayerMessage(en.getKey(), player, cannedMessages, commandParams);
			}
		}
	}

	public boolean subscribed(ServerPlayerEntity player) {
		for (Map.Entry<String, String> en : subscribedPlayers.entrySet()) {
			ServerPlayerEntity p = playerFromName(en.getKey());
			if (player.equals(p)) {
				return true;
			}
		}
		return false;
	}

	public void sendPlayerMessage(String playerName, ServerPlayerEntity player, Text[] messages, Object[] commandParams) {
		handlers.getOrDefault(playerName, defaultHandler).handle(player, messages, commandParams);
	}

	/**
	 * Gets the {@code EntityPlayer} instance for a player given their UUID. Returns null if they are offline.
	 */
	protected ServerPlayerEntity playerFromName(String name) {
		return server.getPlayerManager().get(name);
	}

	// ----- Event Handlers ----- //

	public @Nullable String getAcceptedOption(String arg) {
		if (options.contains(arg)) return arg;
		return null;
	}
}
