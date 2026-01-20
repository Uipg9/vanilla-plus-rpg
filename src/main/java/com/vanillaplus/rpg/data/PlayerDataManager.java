package com.vanillaplus.rpg.data;

import com.vanillaplus.rpg.VanillaPlusRpg;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player data persistence using file-based NBT storage
 * UPDATED FOR 1.21.11: Uses file-based approach since getPersistentData doesn't exist
 */
public class PlayerDataManager {
    private static final String DATA_DIR = "vanillaplusrpg_playerdata";
    
    // In-memory cache
    private static final Map<UUID, CompoundTag> PLAYER_DATA_CACHE = new HashMap<>();
    
    // Data keys
    private static final String KEY_MONEY = "money";
    private static final String KEY_RPG_LEVEL = "rpgLevel";
    private static final String KEY_RPG_XP = "rpgXp";
    private static final String KEY_DAILY_EARNINGS = "dailyEarnings";
    private static final String KEY_LAST_LOGIN = "lastLogin";
    
    /**
     * Get the data directory path for a player
     * 1.21.11: Access server through level().getServer()
     */
    private static Path getPlayerDataPath(ServerPlayer player) {
        // Get server via the level - works in all 1.21.x versions
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        return server
            .getWorldPath(LevelResource.ROOT)
            .resolve(DATA_DIR)
            .resolve(player.getUUID().toString() + ".dat");
    }
    
    /**
     * Get or create mod data compound for player
     * Uses in-memory cache with file persistence
     */
    private static CompoundTag getModData(ServerPlayer player) {
        UUID uuid = player.getUUID();
        
        // Check cache first
        if (PLAYER_DATA_CACHE.containsKey(uuid)) {
            return PLAYER_DATA_CACHE.get(uuid);
        }
        
        // Try to load from file
        Path dataPath = getPlayerDataPath(player);
        if (Files.exists(dataPath)) {
            try {
                CompoundTag data = NbtIo.readCompressed(dataPath, NbtAccounter.unlimitedHeap());
                PLAYER_DATA_CACHE.put(uuid, data);
                return data;
            } catch (Exception e) {
                VanillaPlusRpg.LOGGER.error("Failed to load player data for {}", uuid, e);
            }
        }
        
        // Create new data
        CompoundTag newData = new CompoundTag();
        PLAYER_DATA_CACHE.put(uuid, newData);
        return newData;
    }
    
    /**
     * Save mod data to file
     */
    private static void saveModData(ServerPlayer player, CompoundTag modData) {
        UUID uuid = player.getUUID();
        PLAYER_DATA_CACHE.put(uuid, modData);
        
        // Save to file
        Path dataPath = getPlayerDataPath(player);
        try {
            Files.createDirectories(dataPath.getParent());
            NbtIo.writeCompressed(modData, dataPath);
        } catch (Exception e) {
            VanillaPlusRpg.LOGGER.error("Failed to save player data for {}", uuid, e);
        }
    }
    
    /**
     * Unload player data from cache (call on player leave)
     */
    public static void unloadPlayer(ServerPlayer player) {
        // Save first
        CompoundTag data = PLAYER_DATA_CACHE.get(player.getUUID());
        if (data != null) {
            saveModData(player, data);
        }
        PLAYER_DATA_CACHE.remove(player.getUUID());
    }
    
    // ========== MONEY ==========
    
    public static long getMoney(ServerPlayer player) {
        CompoundTag data = getModData(player);
        // 1.21.11: Use getLongOr with default
        return data.getLongOr(KEY_MONEY, 0L);
    }
    
    public static void setMoney(ServerPlayer player, long amount) {
        CompoundTag data = getModData(player);
        data.putLong(KEY_MONEY, Math.max(0, amount));
        saveModData(player, data);
    }
    
    public static void addMoney(ServerPlayer player, long amount) {
        setMoney(player, getMoney(player) + amount);
    }
    
    public static boolean removeMoney(ServerPlayer player, long amount) {
        long current = getMoney(player);
        if (current >= amount) {
            setMoney(player, current - amount);
            return true;
        }
        return false;
    }
    
    // ========== RPG LEVEL ==========
    
    public static int getRpgLevel(ServerPlayer player) {
        CompoundTag data = getModData(player);
        int level = data.getIntOr(KEY_RPG_LEVEL, 1);
        return Math.max(1, level); // Minimum level 1
    }
    
    public static void setRpgLevel(ServerPlayer player, int level) {
        CompoundTag data = getModData(player);
        data.putInt(KEY_RPG_LEVEL, Math.max(1, level));
        saveModData(player, data);
    }
    
    public static void addRpgLevel(ServerPlayer player, int amount) {
        setRpgLevel(player, getRpgLevel(player) + amount);
    }
    
    // ========== RPG XP ==========
    
    public static int getRpgXp(ServerPlayer player) {
        CompoundTag data = getModData(player);
        return data.getIntOr(KEY_RPG_XP, 0);
    }
    
    public static void setRpgXp(ServerPlayer player, int xp) {
        CompoundTag data = getModData(player);
        data.putInt(KEY_RPG_XP, Math.max(0, xp));
        saveModData(player, data);
    }
    
    /**
     * Add XP and handle level ups
     * XP required for next level = level * 100
     */
    public static void addRpgXp(ServerPlayer player, int amount) {
        int currentXp = getRpgXp(player);
        int currentLevel = getRpgLevel(player);
        int newXp = currentXp + amount;
        
        // Check for level up
        int xpRequired = getXpRequired(currentLevel);
        while (newXp >= xpRequired) {
            newXp -= xpRequired;
            currentLevel++;
            xpRequired = getXpRequired(currentLevel);
        }
        
        setRpgXp(player, newXp);
        setRpgLevel(player, currentLevel);
    }
    
    /**
     * Get XP required for next level
     * Formula: level * 100 (so level 1 needs 100 XP, level 10 needs 1000 XP)
     */
    public static int getXpRequired(int level) {
        return level * 100;
    }
    
    /**
     * Get XP progress as percentage (0.0 to 1.0)
     */
    public static float getXpProgress(ServerPlayer player) {
        int currentXp = getRpgXp(player);
        int required = getXpRequired(getRpgLevel(player));
        return (float) currentXp / (float) required;
    }
    
    // ========== DAILY EARNINGS ==========
    
    public static long getDailyEarnings(ServerPlayer player) {
        CompoundTag data = getModData(player);
        return data.getLongOr(KEY_DAILY_EARNINGS, 0L);
    }
    
    public static void addToDailyEarnings(ServerPlayer player, long amount) {
        CompoundTag data = getModData(player);
        long current = data.getLongOr(KEY_DAILY_EARNINGS, 0L);
        data.putLong(KEY_DAILY_EARNINGS, current + amount);
        saveModData(player, data);
    }
    
    public static void resetDailyEarnings(ServerPlayer player) {
        CompoundTag data = getModData(player);
        data.putLong(KEY_DAILY_EARNINGS, 0L);
        saveModData(player, data);
    }
    
    // ========== UTILITIES ==========
    
    public static long getLastLogin(ServerPlayer player) {
        CompoundTag data = getModData(player);
        return data.getLongOr(KEY_LAST_LOGIN, 0L);
    }
    
    public static void setLastLogin(ServerPlayer player) {
        CompoundTag data = getModData(player);
        data.putLong(KEY_LAST_LOGIN, System.currentTimeMillis());
        saveModData(player, data);
    }
    
    /**
     * Check if this is the player's first login
     */
    public static boolean isFirstLogin(ServerPlayer player) {
        return getLastLogin(player) == 0L;
    }
    
    /**
     * Initialize a new player with starting values
     */
    public static void initNewPlayer(ServerPlayer player) {
        if (isFirstLogin(player)) {
            setMoney(player, 100); // Start with 100 coins
            setRpgLevel(player, 1);
            setRpgXp(player, 0);
            resetDailyEarnings(player);
            setLastLogin(player);
        }
    }
    
    /**
     * Format money for display
     */
    public static String formatMoney(long amount) {
        if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return String.format("%.1fK", amount / 1_000.0);
        }
        return String.valueOf(amount);
    }
}
