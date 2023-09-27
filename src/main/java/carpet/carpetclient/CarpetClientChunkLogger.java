package carpet.carpetclient;
/*
 *  Authors: Xcom and 0x53ee71ebe11e
 *
 *  Backend for the the carpetclient chunk debugging tool by
 *  Earthcomputer, Xcom and 0x53ee71ebe11e
 *
 */

import carpet.CarpetSettings;
import carpet.mixin.accessors.ServerChunkProviderAccessor;
import carpet.mixin.accessors.PlayerChunkMapAccessor;
import carpet.utils.LRUCache;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import net.minecraft.block.state.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.ChunkHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.server.world.chunk.ServerChunkCache;

import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class CarpetClientChunkLogger {
    public static CarpetClientChunkLogger logger = new CarpetClientChunkLogger();

    public boolean enabled = true;
    StackTraces stackTraces = new StackTraces();
    private final ChunkLoggerSerializer clients = new ChunkLoggerSerializer();
    private final ArrayList<ChunkLog> eventsThisGametick = new ArrayList<>();
    public static String reason = null;
    public static String oldReason = null;

    private static final int MAX_STACKTRACE_SIZE = 60;

    public enum Event {
        NONE,
        UNLOADING,
        LOADING,
        PLAYER_ENTERS("Player added to chunk"),
        PLAYER_LEAVES("Player removed from chunk"),
        QUEUE_UNLOAD,
        CANCEL_UNLOAD,
        GENERATING,
        POPULATING("Populating chunk"),
        GENERATING_STRUCTURES("Generating structure");

        public final @Nullable String reason;

        Event() {
            this.reason = null;
        }

        Event(@Nullable String reason) {
            this.reason = reason;
        }
    }

    private static class ChunkLog {
        final int chunkX;
        final int chunkZ;
        final int chunkDimension;
        final Event event;
        final InternedString stackTrace;
        final InternedString reason;

        ChunkLog(int x, int z, int d, Event e, InternedString trace, InternedString reason) {
            this.chunkX = x;
            this.chunkZ = z;
            this.chunkDimension = d;
            this.event = e;
            this.stackTrace = trace;
            this.reason = reason;
        }
    }

    public static void resetToOldReason() {
        reason = oldReason;
    }

    public static void setReason(String r) {
        oldReason = reason;
        reason = r;
    }

    public static void setReason(ChunkLoadingReason reason) {
        if (!logger.enabled) return;
        setReason(reason.getDescription());
    }

    public static void setReason(Supplier<ChunkLoadingReason> reason) {
        if (!logger.enabled) return;
        setReason(reason.get().getDescription());
    }

    public static void resetReason() {
        reason = null;
    }

    public static BlockState getBlockState(WorldView world, BlockPos pos, String reason) {
        setReason(reason);
        BlockState state = world.getBlockState(pos);
        resetToOldReason();
        return state;
    }
    public static BlockState getBlockState(WorldView world, BlockPos pos, ChunkLoadingReason reason) {
        setReason(reason);
        BlockState state = world.getBlockState(pos);
        resetToOldReason();
        return state;
    }
    public static BlockState getBlockState(WorldView world, BlockPos pos, Supplier<ChunkLoadingReason> reason) {
        setReason(reason);
        BlockState state = world.getBlockState(pos);
        resetToOldReason();
        return state;
    }

    /*
     * main logging function
     * logs a change in a chunk including a stacktrace if required by the client
     */
    public void log(World w, int x, int z, Event e) {
        if (!enabled) return;
        log(x, z, getWorldIndex(w), e, stackTraces.internStackTrace(), e.reason != null ? stackTraces.internString(e.reason) : stackTraces.internReason());
    }

    /*
     * called at the end of a gametick to send all events to the registered clients
     */
    public void sendAll() {
        clients.sendUpdates();
        this.eventsThisGametick.clear();
    }

    /*
     * removes all players and disables the logging
     */

    public void disable() {
        enabled = false;
        clients.kickAllPlayers();
        this.eventsThisGametick.clear();
    }

    /*
     * called when registering a new player
     */
    public void registerPlayer(ServerPlayerEntity sender, PacketByteBuf data) {
        clients.registerPlayer(sender, data);
    }

    /*
     * unregisters a single player
     */
    public void unregisterPlayer(ServerPlayerEntity player) {
        clients.unregisterPlayer(player);
    }

    private ArrayList<ChunkLog> getInitialChunksForNewClient(MinecraftServer server) {
        ArrayList<ChunkLog> forNewClient = new ArrayList<>();
        int dimension = -1;
        for (World w : server.worlds) {
            ServerChunkCache provider = (ServerChunkCache) (w.getChunkSource());
            dimension++;
            for (WorldChunk c : provider.getLoadedChunks()) {
                forNewClient.add(new ChunkLog(c.chunkX, c.chunkZ, dimension, Event.LOADING, null, null));
                if (((ServerChunkProviderAccessor) provider).getDroppedChunks().contains(ChunkPos.toLong(c.chunkX, c.chunkZ))) {
                    forNewClient.add(new ChunkLog(c.chunkX, c.chunkZ, dimension, Event.QUEUE_UNLOAD, null, null));
                    if (!c.removed) {
                        forNewClient.add(new ChunkLog(c.chunkX, c.chunkZ, dimension, Event.CANCEL_UNLOAD, null, null));
                    }
                }
            }
            ChunkMap chunkmap = ((ServerWorld) w).getChunkMap();
            Iterator<ChunkPos> i = carpetGetAllChunkCoordinates(chunkmap);
            while (i.hasNext()) {
                ChunkPos pos = i.next();
                forNewClient.add(new ChunkLog(pos.x, pos.z, dimension, Event.PLAYER_ENTERS, null, null));
            }
        }
        return forNewClient;
    }

    /*
     * Gets the coordinates of all chunks
     */
    private static Iterator<ChunkPos> carpetGetAllChunkCoordinates(ChunkMap map){
        return new AbstractIterator<ChunkPos>() {
            final Iterator<ChunkHolder> allChunks = Iterators.concat(((PlayerChunkMapAccessor) map).getEntries().iterator(),
                    ((PlayerChunkMapAccessor) map).getEntriesWithoutChunks().iterator());

            @Override
            protected ChunkPos computeNext() {
                if (allChunks.hasNext()) {
                    return allChunks.next().getPos();
                } else {
                    return this.endOfData();
                }
            }
        };
    }

    private ArrayList<ChunkLog> getEventsThisGametick() {
        return this.eventsThisGametick;
    }

    private void log(int x, int z, int d, Event event, InternedString stackTrace, InternedString reasonID) {
        this.eventsThisGametick.add(new ChunkLog(x, z, d, event, stackTrace, reasonID));
    }

    private static int getWorldIndex(World w) {
        int i = 0;
        for (World o : w.getServer().worlds) {
            if (o == w) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private static class InternedString {
        public final String obfuscated;
        public final int id;

        public InternedString(int id, String obfuscated) {
            this.id = id;
            this.obfuscated = obfuscated;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InternedString that = (InternedString) o;
            return id == that.id && Objects.equals(obfuscated, that.obfuscated);
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    private static class StackTraces {
        private final Map<String, InternedString> internedStrings = new LRUCache<>(128); // 64 ~ 98%, 128+ > 99%
        private int nextId = 1;

        private InternedString internString(String s) {
            if (s == null) return null;
            InternedString internedString = internedStrings.get(s);
            if (internedString == null) {
                internedString = new InternedString(nextId++, s);
                internedStrings.put(s, internedString);
            }
            return internedString;
        }

        private InternedString internStackTrace() {
            StackTraceElement[] trace = new Throwable().getStackTrace();
            String obfuscated = asString(trace);
            InternedString internedString = internedStrings.get(obfuscated);
            if (internedString == null) {
                internedString = new InternedString(nextId++, obfuscated);
                internedStrings.put(obfuscated, internedString);
            }
            return internedString;
        }

        public InternedString internReason() {
            return this.internString(reason);
        }

        private static String asString(StackTraceElement[] trace) {
            StringBuilder stacktrace = new StringBuilder();
            for (int i = 0; i < trace.length && i < MAX_STACKTRACE_SIZE; i++) {
                StackTraceElement e = trace[i];

                if ("CarpetClientChunkLogger.java".equals(e.getFileName())) {
                    continue;
                }
                if (stacktrace.length() > 0) {
                    stacktrace.append("\n");
                }
                stacktrace.append(e);
            }
            return stacktrace.toString();
        }
    }

    private class ChunkLoggerSerializer {

        private static final int PACKET_EVENTS = 0;
        private static final int PACKET_STACKTRACE = 1;
        private static final int PACKET_ACCESS_DENIED = 2;

        private static final int STACKTRACES_BATCH_SIZE = 10;
        private static final int LOGS_BATCH_SIZE = 1000;

        private final Map<ServerPlayerEntity, HashSet<InternedString>> sentTracesForPlayer = new WeakHashMap<>();

        public void registerPlayer(ServerPlayerEntity sender, PacketByteBuf data) {
            if (!CarpetSettings.chunkDebugTool) {
                CarpetClientMessageHandler.sendNBTChunkData(sender, PACKET_ACCESS_DENIED, new NbtCompound());
                return;
            }
            boolean addPlayer = data.readBoolean();
            if (addPlayer) {
                enabled = true;
                this.sentTracesForPlayer.put(sender, new HashSet<>());
                this.sendInitalChunks(sender);
            } else {
                this.unregisterPlayer(sender);
            }
        }

        public void unregisterPlayer(ServerPlayerEntity player) {
            sentTracesForPlayer.remove(player);
            if (sentTracesForPlayer.isEmpty()) {
                enabled = false;
            }
        }

        private void sendInitalChunks(ServerPlayerEntity sender) {
            MinecraftServer server = sender.getServer();
            ArrayList<ChunkLog> logs = getInitialChunksForNewClient(server);
            sendMissingStackTracesForPlayer(sender, logs);
            for (int i = 0; i < logs.size(); i += LOGS_BATCH_SIZE) {
                boolean complete = (i + LOGS_BATCH_SIZE) >= logs.size();
                List<ChunkLog> batch = logs.subList(i, Integer.min(i + LOGS_BATCH_SIZE, logs.size()));
                NbtCompound chunkData = serializeEvents(batch, -server.getTicks() - 1, i, complete);
                if (chunkData != null) {
                    CarpetClientMessageHandler.sendNBTChunkData(sender, PACKET_EVENTS, chunkData);
                }
            }
        }

        private void sendUpdates() {
            if (this.sentTracesForPlayer.isEmpty()) {
                return;
            }

            ArrayList<ChunkLog> logs = getEventsThisGametick();
            MinecraftServer server = this.sentTracesForPlayer.keySet().iterator().next().server;

            for (ServerPlayerEntity client : this.sentTracesForPlayer.keySet()) {
                this.sendMissingStackTracesForPlayer(client, logs);
            }
            for (int i = 0; i < logs.size(); i += LOGS_BATCH_SIZE) {
                boolean complete = (i + LOGS_BATCH_SIZE) >= logs.size();
                List<ChunkLog> batch = logs.subList(i, Integer.min(i + LOGS_BATCH_SIZE, logs.size()));
                NbtCompound chunkData = serializeEvents(batch, server.getTicks(), i, complete);
                if (chunkData != null) {
                    for (ServerPlayerEntity player : this.sentTracesForPlayer.keySet()) {
                        CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_EVENTS, chunkData);
                    }
                }
            }
        }

        private void sendMissingStackTracesForPlayer(ServerPlayerEntity player, ArrayList<ChunkLog> events) {
            HashSet<InternedString> missingTraces = new HashSet<>();
            HashSet<InternedString> sentTraces = this.sentTracesForPlayer.getOrDefault(player, null);

            if (sentTraces == null) {
                return;
            }
            for (ChunkLog log : events) {
                InternedString stackTrace = log.stackTrace;
                InternedString reason = log.reason;
                if (stackTrace != null && !sentTraces.contains(stackTrace)) {
                    sentTraces.add(stackTrace);
                    missingTraces.add(stackTrace);
                }
                if (reason != null && !sentTraces.contains(reason)) {
                    sentTraces.add(reason);
                    missingTraces.add(reason);
                }
            }
            ArrayList<InternedString> missingList = new ArrayList<>(missingTraces);
            for (int i = 0; i < missingList.size(); i += STACKTRACES_BATCH_SIZE) {
                List<InternedString> part = missingList.subList(i, Integer.min(i + STACKTRACES_BATCH_SIZE, missingList.size()));
                NbtCompound stackData = serializeStackTraces(part);
                if (stackData != null) {
                    CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_STACKTRACE, stackData);
                }
            }
        }

        private NbtCompound serializeEvents(List<ChunkLog> events, int gametick, int dataOffset, boolean complete) {
            if (events.isEmpty()) {
                return null;
            }
            NbtCompound chunkData = new NbtCompound();
            int[] data = new int[6 * events.size()];
            int i = 0;
            for (ChunkLog log : events) {
                data[i++] = log.chunkX;
                data[i++] = log.chunkZ;
                data[i++] = log.chunkDimension;
                data[i++] = log.event.ordinal();
                data[i++] = log.stackTrace == null ? 0 : log.stackTrace.id;
                data[i++] = log.reason == null ? 0 : log.reason.id;
            }
            chunkData.putInt("size", events.size());
            chunkData.putIntArray("data", data);
            chunkData.putInt("offset", dataOffset);
            chunkData.putInt("time", gametick);
            chunkData.putBoolean("complete", complete);
            return chunkData;
        }

        private NbtCompound serializeStackTraces(List<InternedString> strings) {
            if (strings.isEmpty()) {
                return null;
            }
            NbtList list = new NbtList();
            for (InternedString obfuscated : strings) {
                NbtCompound stackTrace = new NbtCompound();
                stackTrace.putInt("id", obfuscated.id);
                stackTrace.putString("stack", obfuscated.obfuscated);
                list.add(stackTrace);
            }
            NbtCompound stackList = new NbtCompound();
            stackList.put("stackList", list);
            return stackList;
        }

        private void kickAllPlayers() {
            for (ServerPlayerEntity player : this.sentTracesForPlayer.keySet()) {
                CarpetClientMessageHandler.sendNBTChunkData(player, PACKET_ACCESS_DENIED, new NbtCompound());
            }
            this.sentTracesForPlayer.clear();
        }
    }
}