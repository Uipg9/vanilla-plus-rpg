# 💾 Data Storage & Persistence - Minecraft 1.21.11

**Complete NBT storage guide for saving mod data persistently**

> ✅ **Production-Tested**: Data system from Pocket Life mod storing coins, upgrades, tool durability, active operations, etc.
> 
> ⚠️ **NBT API Change in 1.21.11**: Some methods now return `Optional<Tag>` instead of nullable.

---

## Table of Contents

1. [Storage Methods Overview](#overview)
2. [Player Persistent Data](#player-data)
3. [World/Server Data](#world-data)
4. [NBT File Storage](#nbt-files)
5. [DataManager Pattern](#datamanager)
6. [Save/Load Hooks](#hooks)
7. [Complete Examples](#examples)

---

## <a id="overview"></a>Storage Methods Overview

| Method | Scope | Persistence | Use Case |
|--------|-------|-------------|----------|
| **In-Memory** | Session only | Lost on restart | Temporary data, caches |
| **Player NBT** | Per-player | Saved with player | Player stats, inventory data |
| **World NBT** | Per-world | Saved with world | Global state, world config |
| **Custom NBT Files** | Global | Manual save/load | Databases, complex data |

---

## <a id="player-data"></a>Player Persistent Data

### Basic Player NBT Access

```java
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerDataHelper {
    
    // Save data to player
    public static void savePlayerData(ServerPlayer player, String key, int value) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag modData = nbt.getCompound("YourModID");
        modData.putInt(key, value);
        nbt.put("YourModID", modData);
    }
    
    // Load data from player
    public static int loadPlayerData(ServerPlayer player, String key, int defaultValue) {
        CompoundTag nbt = player.getPersistentData();
        if (nbt.contains("YourModID")) {
            CompoundTag modData = nbt.getCompound("YourModID");
            return modData.contains(key) ? modData.getInt(key) : defaultValue;
        }
        return defaultValue;
    }
    
    // Check if player has data
    public static boolean hasPlayerData(ServerPlayer player, String key) {
        CompoundTag nbt = player.getPersistentData();
        return nbt.contains("YourModID") && 
               nbt.getCompound("YourModID").contains(key);
    }
}
```

### Complete Player Data Manager

```java
public class PlayerDataManager {
    private static final String MOD_KEY = "YourModID";
    
    // Save multiple values
    public static void saveAll(ServerPlayer player, 
                               int coins, 
                               int level, 
                               String rank) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag modData = nbt.getCompound(MOD_KEY);
        
        modData.putInt("coins", coins);
        modData.putInt("level", level);
        modData.putString("rank", rank);
        modData.putLong("lastLogin", System.currentTimeMillis());
        
        nbt.put(MOD_KEY, modData);
    }
    
    // Load all data
    public static PlayerData load(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        
        if (!nbt.contains(MOD_KEY)) {
            return new PlayerData();  // Default values
        }
        
        CompoundTag modData = nbt.getCompound(MOD_KEY);
        
        return new PlayerData(
            modData.getInt("coins"),
            modData.getInt("level"),
            modData.getString("rank"),
            modData.getLong("lastLogin")
        );
    }
    
    // Increment value
    public static void addCoins(ServerPlayer player, int amount) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag modData = nbt.getCompound(MOD_KEY);
        
        int current = modData.getInt("coins");
        modData.putInt("coins", current + amount);
        
        nbt.put(MOD_KEY, modData);
    }
    
    // Save list of strings
    public static void saveStringList(ServerPlayer player, String key, List<String> list) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag modData = nbt.getCompound(MOD_KEY);
        
        ListTag listTag = new ListTag();
        for (String value : list) {
            listTag.add(StringTag.valueOf(value));
        }
        modData.put(key, listTag);
        
        nbt.put(MOD_KEY, modData);
    }
    
    // Load list of strings
    public static List<String> loadStringList(ServerPlayer player, String key) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains(MOD_KEY)) return new ArrayList<>();
        
        CompoundTag modData = nbt.getCompound(MOD_KEY);
        if (!modData.contains(key)) return new ArrayList<>();
        
        ListTag listTag = modData.getList(key, 8);  // 8 = STRING type
        List<String> result = new ArrayList<>();
        
        for (int i = 0; i < listTag.size(); i++) {
            result.add(listTag.getString(i));
        }
        
        return result;
    }
}

// Data class
public class PlayerData {
    public int coins;
    public int level;
    public String rank;
    public long lastLogin;
    
    public PlayerData() {
        this.coins = 0;
        this.level = 1;
        this.rank = "Newcomer";
        this.lastLogin = System.currentTimeMillis();
    }
    
    public PlayerData(int coins, int level, String rank, long lastLogin) {
        this.coins = coins;
        this.level = level;
        this.rank = rank;
        this.lastLogin = lastLogin;
    }
}
```

---

## <a id="world-data"></a>World/Server Data

### Server-Wide Storage

```java
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public class ServerDataManager {
    private static final String MOD_KEY = "YourModID";
    
    // Save server data
    public static void saveServerData(MinecraftServer server, String key, int value) {
        CompoundTag nbt = server.overworld().getDataStorage()
            .computeIfAbsent(tag -> tag, CompoundTag::new, MOD_KEY);
        
        nbt.putInt(key, value);
    }
    
    // Load server data
    public static int loadServerData(MinecraftServer server, String key, int defaultValue) {
        CompoundTag nbt = server.overworld().getDataStorage()
            .computeIfAbsent(tag -> tag, CompoundTag::new, MOD_KEY);
        
        return nbt.contains(key) ? nbt.getInt(key) : defaultValue;
    }
}
```

---

## <a id="nbt-files"></a>Custom NBT File Storage

### File-Based Data Storage

```java
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import java.io.File;
import java.io.IOException;

public class FileDataManager {
    private static File getDataFile(MinecraftServer server, String filename) {
        File worldDir = server.overworld().getDataStorage().getDataFolder().getParentFile();
        File modDir = new File(worldDir, "yourmod");
        if (!modDir.exists()) {
            modDir.mkdirs();
        }
        return new File(modDir, filename + ".dat");
    }
    
    // Save to file
    public static void saveToFile(MinecraftServer server, String filename, CompoundTag data) {
        try {
            File file = getDataFile(server, filename);
            NbtIo.writeCompressed(data, file.toPath());
        } catch (IOException e) {
            server.getLogger().error("Failed to save data file: " + filename, e);
        }
    }
    
    // Load from file
    public static CompoundTag loadFromFile(MinecraftServer server, String filename) {
        try {
            File file = getDataFile(server, filename);
            if (file.exists()) {
                return NbtIo.readCompressed(file.toPath());
            }
        } catch (IOException e) {
            server.getLogger().error("Failed to load data file: " + filename, e);
        }
        return new CompoundTag();
    }
    
    // Check if file exists
    public static boolean fileExists(MinecraftServer server, String filename) {
        return getDataFile(server, filename).exists();
    }
}
```

### Example: Shop Data Storage

```java
public class ShopDataManager {
    private static final String SHOP_FILE = "shop_data";
    
    public static void saveShopInventory(MinecraftServer server, Map<String, Integer> prices) {
        CompoundTag data = new CompoundTag();
        
        for (Map.Entry<String, Integer> entry : prices.entrySet()) {
            data.putInt(entry.getKey(), entry.getValue());
        }
        
        data.putLong("lastUpdate", System.currentTimeMillis());
        
        FileDataManager.saveToFile(server, SHOP_FILE, data);
    }
    
    public static Map<String, Integer> loadShopInventory(MinecraftServer server) {
        CompoundTag data = FileDataManager.loadFromFile(server, SHOP_FILE);
        Map<String, Integer> prices = new HashMap<>();
        
        for (String key : data.getAllKeys()) {
            if (!key.equals("lastUpdate")) {
                prices.put(key, data.getInt(key));
            }
        }
        
        return prices;
    }
}
```

---

## <a id="datamanager"></a>Complete DataManager Pattern

### Centralized Data Management

```java
public class ModDataManager {
    private static final String MOD_KEY = "yourmod";
    private MinecraftServer server;
    private Map<UUID, PlayerData> cache = new HashMap<>();
    
    public ModDataManager(MinecraftServer server) {
        this.server = server;
    }
    
    // Player data with caching
    public PlayerData getPlayerData(ServerPlayer player) {
        UUID uuid = player.getUUID();
        
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }
        
        PlayerData data = loadPlayerDataFromNBT(player);
        cache.put(uuid, data);
        return data;
    }
    
    public void savePlayerData(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (cache.containsKey(uuid)) {
            PlayerData data = cache.get(uuid);
            savePlayerDataToNBT(player, data);
        }
    }
    
    private PlayerData loadPlayerDataFromNBT(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        
        if (!nbt.contains(MOD_KEY)) {
            return new PlayerData();
        }
        
        CompoundTag modData = nbt.getCompound(MOD_KEY);
        return PlayerData.fromNBT(modData);
    }
    
    private void savePlayerDataToNBT(ServerPlayer player, PlayerData data) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag modData = data.toNBT();
        nbt.put(MOD_KEY, modData);
    }
    
    // Save all cached data
    public void saveAll() {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            savePlayerData(player);
        }
    }
    
    // Clear cache for player
    public void unloadPlayer(ServerPlayer player) {
        savePlayerData(player);
        cache.remove(player.getUUID());
    }
}

// PlayerData with NBT serialization
public class PlayerData {
    public int coins;
    public int level;
    public Map<String, Integer> stats;
    
    public PlayerData() {
        this.coins = 0;
        this.level = 1;
        this.stats = new HashMap<>();
    }
    
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("coins", coins);
        nbt.putInt("level", level);
        
        CompoundTag statsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            statsTag.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("stats", statsTag);
        
        return nbt;
    }
    
    public static PlayerData fromNBT(CompoundTag nbt) {
        PlayerData data = new PlayerData();
        data.coins = nbt.getInt("coins");
        data.level = nbt.getInt("level");
        
        if (nbt.contains("stats")) {
            CompoundTag statsTag = nbt.getCompound("stats");
            for (String key : statsTag.getAllKeys()) {
                data.stats.put(key, statsTag.getInt(key));
            }
        }
        
        return data;
    }
}
```

---

## <a id="hooks"></a>Save/Load Hooks

### Auto-Save on Server Shutdown

```java
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class YourMod implements ModInitializer {
    private static ModDataManager dataManager;
    
    @Override
    public void onInitialize() {
        // Server start
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            dataManager = new ModDataManager(server);
            LOGGER.info("Data manager initialized");
        });
        
        // Server stop - save all data
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (dataManager != null) {
                dataManager.saveAll();
                LOGGER.info("All data saved");
            }
        });
    }
}
```

### Auto-Save on Player Disconnect

```java
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
    ServerPlayer player = handler.getPlayer();
    dataManager.unloadPlayer(player);
    LOGGER.info("Saved data for player: " + player.getName().getString());
});
```

### Periodic Auto-Save

```java
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class AutoSaveManager {
    private static int tickCounter = 0;
    private static final int SAVE_INTERVAL = 20 * 60 * 5;  // 5 minutes
    
    public static void register(ModDataManager dataManager) {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= SAVE_INTERVAL) {
                dataManager.saveAll();
                server.getLogger().info("Auto-saved all player data");
                tickCounter = 0;
            }
        });
    }
}
```

---

## <a id="examples"></a>Complete Examples

### Example 1: Economy System

```java
public class EconomyManager {
    private static final String BALANCE_KEY = "balance";
    
    public static int getBalance(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag modData = nbt.getCompound("economy");
        return modData.getInt(BALANCE_KEY);
    }
    
    public static void setBalance(ServerPlayer player, int amount) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag modData = nbt.getCompound("economy");
        modData.putInt(BALANCE_KEY, amount);
        nbt.put("economy", modData);
    }
    
    public static void addMoney(ServerPlayer player, int amount) {
        setBalance(player, getBalance(player) + amount);
    }
    
    public static void removeMoney(ServerPlayer player, int amount) {
        setBalance(player, getBalance(player) - amount);
    }
    
    public static boolean hasBalance(ServerPlayer player, int amount) {
        return getBalance(player) >= amount;
    }
}
```

### Example 2: Virtual Farm System

```java
public class FarmManager {
    private static final String FARM_KEY = "farm";
    
    // Save farm plots
    public static void saveFarm(ServerPlayer player, List<Plot> plots) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag farmData = new CompoundTag();
        
        ListTag plotsTag = new ListTag();
        for (Plot plot : plots) {
            CompoundTag plotTag = new CompoundTag();
            plotTag.putString("crop", plot.cropType);
            plotTag.putInt("growth", plot.growthTicks);
            plotTag.putBoolean("ready", plot.isReady);
            plotsTag.add(plotTag);
        }
        
        farmData.put("plots", plotsTag);
        farmData.putInt("totalPlots", plots.size());
        farmData.putLong("lastUpdate", System.currentTimeMillis());
        
        nbt.put(FARM_KEY, farmData);
    }
    
    // Load farm plots
    public static List<Plot> loadFarm(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        
        if (!nbt.contains(FARM_KEY)) {
            return new ArrayList<>();
        }
        
        CompoundTag farmData = nbt.getCompound(FARM_KEY);
        ListTag plotsTag = farmData.getList("plots", 10);  // 10 = COMPOUND type
        
        List<Plot> plots = new ArrayList<>();
        for (int i = 0; i < plotsTag.size(); i++) {
            CompoundTag plotTag = plotsTag.getCompound(i);
            Plot plot = new Plot();
            plot.cropType = plotTag.getString("crop");
            plot.growthTicks = plotTag.getInt("growth");
            plot.isReady = plotTag.getBoolean("ready");
            plots.add(plot);
        }
        
        return plots;
    }
}

class Plot {
    String cropType;
    int growthTicks;
    boolean isReady;
}
```

### Example 3: Quest System

```java
public class QuestManager {
    private static final String QUEST_KEY = "quests";
    
    // Save active quests
    public static void saveQuests(ServerPlayer player, Map<String, Integer> questProgress) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag questData = new CompoundTag();
        
        for (Map.Entry<String, Integer> entry : questProgress.entrySet()) {
            questData.putInt(entry.getKey(), entry.getValue());
        }
        
        nbt.put(QUEST_KEY, questData);
    }
    
    // Load active quests
    public static Map<String, Integer> loadQuests(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        Map<String, Integer> quests = new HashMap<>();
        
        if (!nbt.contains(QUEST_KEY)) {
            return quests;
        }
        
        CompoundTag questData = nbt.getCompound(QUEST_KEY);
        for (String key : questData.getAllKeys()) {
            quests.put(key, questData.getInt(key));
        }
        
        return quests;
    }
    
    // Update quest progress
    public static void updateQuestProgress(ServerPlayer player, String questId, int progress) {
        Map<String, Integer> quests = loadQuests(player);
        quests.put(questId, progress);
        saveQuests(player, quests);
    }
    
    // Complete quest
    public static void completeQuest(ServerPlayer player, String questId) {
        Map<String, Integer> quests = loadQuests(player);
        quests.remove(questId);
        saveQuests(player, quests);
        
        // Save to completed list
        saveCompletedQuest(player, questId);
    }
    
    private static void saveCompletedQuest(ServerPlayer player, String questId) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag modData = nbt.getCompound("quests_completed");
        
        ListTag completed = modData.getList("completed", 8);  // 8 = STRING
        completed.add(StringTag.valueOf(questId));
        modData.put("completed", completed);
        
        nbt.put("quests_completed", modData);
    }
}
```

---

## NBT Type Reference

| Type | ID | Method | Java Type |
|------|----|----|-----------|
| Byte | 1 | `putByte()` | `byte` |
| Short | 2 | `putShort()` | `short` |
| Int | 3 | `putInt()` | `int` |
| Long | 4 | `putLong()` | `long` |
| Float | 5 | `putFloat()` | `float` |
| Double | 6 | `putDouble()` | `double` |
| String | 8 | `putString()` | `String` |
| List | 9 | `put()` | `ListTag` |
| Compound | 10 | `put()` | `CompoundTag` |
| Boolean | 1 | `putBoolean()` | `boolean` (stored as byte) |

---

## Best Practices

✅ Always use a unique mod key (e.g., "yourmod")
✅ Cache frequently accessed data in memory
✅ Save data on server stop and player disconnect
✅ Provide default values when loading missing data
✅ Use `contains()` before `get()` to avoid errors
✅ Consider auto-save intervals for important data

---

## Next Steps

Continue to:
- [03_GUI_SYSTEMS.md](03_GUI_SYSTEMS.md) - Save GUI state
- [04_COMMANDS.md](04_COMMANDS.md) - Commands that modify data
- [08_PATTERNS.md](08_PATTERNS.md) - Data patterns

---

**Version Note:** All code is for Minecraft 1.21.11 with Mojang mappings.
