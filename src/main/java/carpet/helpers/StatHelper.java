package carpet.helpers;

import carpet.mixin.accessors.ScoreCriteriaStatAccessor;
import carpet.mixin.accessors.StatCraftingAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.*;
import net.minecraft.scoreboard.criterion.ScoreboardCriterion;
import net.minecraft.scoreboard.criterion.StatCriterion;
import net.minecraft.server.GameProfileCache;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.stat.ServerPlayerStats;
import net.minecraft.stat.ItemStat;
import net.minecraft.stat.PlayerStats;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class StatHelper {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<UUID, PlayerStats> cache;
    private static long cacheTime;
    private static final Stat[] BLOCK_STATE_STATS = new Stat[256 * 16];
    private static final Int2ObjectMap<Stat> CRAFT_META_STATS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Stat> OBJECT_USE_META_STATS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Stat> OBJECTS_PICKED_UP_META_STATS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Stat> OBJECTS_DROPPED_META_STATS = new Int2ObjectOpenHashMap<>();

    public static File[] getStatFiles(MinecraftServer server) {
        File statsDir = new File(server.getWorld(0).getStorage().getDir(), "stats");
        return statsDir.listFiles((dir, name) -> name.endsWith(".json"));
    }

    public static Map<UUID, PlayerStats> getAllStatistics(MinecraftServer server) {
        if (cache != null && server.getTicks() - cacheTime < 100) return cache;
        File[] files = getStatFiles(server);
        HashMap<UUID, PlayerStats> stats = new HashMap<>();
        PlayerManager players = server.getPlayerManager();
        for (File file : files) {
            String filename = file.getName();
            String uuidString = filename.substring(0, filename.lastIndexOf(".json"));
            try {
                UUID uuid = UUID.fromString(uuidString);
                ServerPlayerEntity player = players.get(uuid);
                if (player != null) {
                    stats.put(uuid, players.getStats(player));
                } else {
                    ServerPlayerStats manager = new ServerPlayerStats(server, file);
                    manager.load();
                    stats.put(uuid, manager);
                }
            } catch (IllegalArgumentException ignored) {}
        }
        cache = stats;
        cacheTime = server.getTicks();
        return stats;
    }

    @Nullable
    public static String getUsername(MinecraftServer server, UUID uuid) {
        GameProfileCache profileCache = server.getGameProfileCache();
        GameProfile profile = profileCache.get(uuid);
        if (profile != null) return profile.getName();
        MinecraftSessionService sessionService = server.getSessionService();
        profile = sessionService.fillProfileProperties(new GameProfile(uuid, null), false);
        if (profile.isComplete()) return profile.getName();
        LOGGER.warn("Could not find name of user " + uuid);
        return null;
    }

    public static void initialize(Scoreboard scoreboard, MinecraftServer server, ScoreboardObjective objective) {
        LOGGER.info("Initializing " + objective);
        ScoreboardCriterion criteria = objective.getCriterion();
        if (!(criteria instanceof StatCriterion)) return;
        Stat stat = ((ScoreCriteriaStatAccessor) criteria).getStat();
        for (Map.Entry<UUID, PlayerStats> statEntry : getAllStatistics(server).entrySet()) {
            PlayerStats stats = statEntry.getValue();
            int value = stats.get(stat);
            if (value == 0) continue;
            String username = getUsername(server, statEntry.getKey());
            if (username == null) continue;
            ScoreboardScore score = scoreboard.getScore(username, objective);
            score.set(value);
            LOGGER.info("Initialized score " + objective.getName() + " of " + username + " to " + value);
        }
    }

    public static Stat getBlockStateStats(BlockState state) {
        Block block = state.getBlock();
        int id = Block.getId(block);
        int meta = block.getMetadataFromState(state);
        return BLOCK_STATE_STATS[(id << 4) | meta];
    }

    public static Stat getCraftStats(Item item, int meta) {
        int id = Item.getId(item);
        return CRAFT_META_STATS.get((id << 4) | (meta & 0xf));
    }

    public static Stat getObjectUseStats(Item item, int meta) {
        int id = Item.getId(item);
        return OBJECT_USE_META_STATS.get((id << 4) | (meta & 0xf));
    }

    public static Stat getObjectsPickedUpStats(Item item, int meta) {
        int id = Item.getId(item);
        return OBJECTS_PICKED_UP_META_STATS.get((id << 4) | (meta & 0xf));
    }

    public static Stat getDroppedObjectStats(Item item, int meta) {
        int id = Item.getId(item);
        return OBJECTS_DROPPED_META_STATS.get((id << 4) | (meta & 0xf));
    }

    private interface StatStorage {
        void store(int stateId, Stat stat);
    }

    private static void registerSubStats(ItemStat baseStat, StatStorage storage, Function<Text, TranslatableText> textFun) {
        Item item = ((StatCraftingAccessor) baseStat).getItem();
        int id = Item.getId(item);
        if (item.canAlwaysUse()) {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                ItemStack stackWithMeta = new ItemStack(item, 1, meta);
                Text text = textFun.apply(stackWithMeta.getDisplayName());
                StatSubItem statWithMeta = (StatSubItem) new StatSubItem(baseStat, meta, text).register();
                storage.store(stateId, statWithMeta);
            }
        } else {
            for (int meta = 0; meta < 16; meta++) {
                int stateId = (id << 4) | meta;
                storage.store(stateId, baseStat);
            }
        }
    }

    public static void addCraftStats(ItemStat baseStat) {
        registerSubStats(baseStat, CRAFT_META_STATS::put, text -> new TranslatableText("stat.craftItem", text));
    }

    public static void addMineStats(ItemStat baseStat) {
        registerSubStats(baseStat, (state, stat) -> BLOCK_STATE_STATS[state] = stat, text -> new TranslatableText("stat.mineBlock", text));
    }

    public static void addUseStats(ItemStat baseStat) {
        registerSubStats(baseStat, OBJECT_USE_META_STATS::put, text -> new TranslatableText("stat.useItem", text));
    }

    public static void addPickedUpStats(ItemStat baseStat) {
        registerSubStats(baseStat, OBJECTS_PICKED_UP_META_STATS::put, text -> new TranslatableText("stat.pickup", text));
    }

    public static void addDroppedStats(ItemStat baseStat) {
        registerSubStats(baseStat, OBJECTS_DROPPED_META_STATS::put, text -> new TranslatableText("stat.drop", text));
    }
}
