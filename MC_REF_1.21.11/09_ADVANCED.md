# 🚀 Advanced Patterns - Minecraft 1.21.11

**Complex systems and advanced techniques for 1.21.11 modding**

> ✅ **Production-Tested**: Economy systems, tick managers, and tool tier systems from Pocket Life mod.
> 
> ⚠️ **Version Specific**: These patterns are designed for 1.21.11 APIs and mappings.

---

## Table of Contents

1. [Pagination Systems](#pagination)
2. [Output Buffer Pattern](#output-buffer)
3. [Economy Integration](#economy)
4. [Auto-Save System](#autosave)
5. [Event-Driven Architecture](#events)
6. [Permission System](#permissions)
7. [Multi-GUI Navigation](#navigation)

---

## <a id="pagination"></a>Advanced Pagination Systems

### Custom Paginated GUI

Complete pagination system with dynamic content and smooth navigation.

```java
public class PaginatedGui extends SimpleGui {
    private List<GuiElementBuilder> allItems;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 21;  // 7x3 grid
    
    // Define content area (3 rows of 7 slots each)
    private final int[][] contentSlots = {
        {10, 11, 12, 13, 14, 15, 16},  // Row 2
        {19, 20, 21, 22, 23, 24, 25},  // Row 3
        {28, 29, 30, 31, 32, 33, 34}   // Row 4
    };
    
    public PaginatedGui(ServerPlayer player, List<GuiElementBuilder> items) {
        super(MenuType.GENERIC_9x6, player, false);
        this.allItems = items;
        
        setupStaticElements();
        updatePage();
    }
    
    private void setupStaticElements() {
        // Previous page button (slot 45)
        this.setSlot(45, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Component.literal("§7← Previous Page"))
            .setCallback((index, type, action) -> {
                if (currentPage > 0) {
                    currentPage--;
                    updatePage();
                }
            })
        );
        
        // Next page button (slot 53)
        this.setSlot(53, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Component.literal("§7Next Page →"))
            .setCallback((index, type, action) -> {
                int maxPage = getTotalPages() - 1;
                if (currentPage < maxPage) {
                    currentPage++;
                    updatePage();
                }
            })
        );
        
        // Page indicator (slot 49)
        updatePageIndicator();
    }
    
    private void updatePage() {
        // Clear content area
        for (int[] row : contentSlots) {
            for (int slot : row) {
                this.clearSlot(slot);
            }
        }
        
        // Calculate range
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());
        
        // Fill content area
        int itemIndex = 0;
        for (int[] row : contentSlots) {
            for (int slot : row) {
                int globalIndex = startIndex + itemIndex;
                
                if (globalIndex < endIndex) {
                    this.setSlot(slot, allItems.get(globalIndex));
                }
                
                itemIndex++;
            }
        }
        
        // Update page indicator
        updatePageIndicator();
        
        // Update title
        this.setTitle(Component.literal(String.format(
            "§6Collection - Page %d/%d", 
            currentPage + 1, 
            getTotalPages()
        )));
    }
    
    private void updatePageIndicator() {
        this.setSlot(49, new GuiElementBuilder()
            .setItem(Items.PAPER)
            .setName(Component.literal("§ePage " + (currentPage + 1) + "/" + getTotalPages()))
            .addLoreLine(Component.literal("§7Total Items: " + allItems.size()))
        );
    }
    
    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE));
    }
}
```

---

## <a id="output-buffer"></a>Output Buffer Pattern

Accumulate items/rewards and allow batch collection.

```java
public class OutputBuffer {
    private Map<Item, Integer> buffer = new HashMap<>();
    
    public void addItem(Item item, int count) {
        buffer.put(item, buffer.getOrDefault(item, 0) + count);
    }
    
    public boolean isEmpty() {
        return buffer.isEmpty();
    }
    
    public int getTotalItems() {
        return buffer.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public void collectAll(ServerPlayer player) {
        for (Map.Entry<Item, Integer> entry : buffer.entrySet()) {
            Item item = entry.getKey();
            int count = entry.getValue();
            
            // Split into stacks if needed
            while (count > 0) {
                int stackSize = Math.min(count, item.getMaxStackSize());
                ItemStack stack = new ItemStack(item, stackSize);
                player.getInventory().add(stack);
                count -= stackSize;
            }
        }
        
        buffer.clear();
    }
    
    public Map<Item, Integer> getContents() {
        return new HashMap<>(buffer);
    }
    
    // NBT Serialization
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        
        for (Map.Entry<Item, Integer> entry : buffer.entrySet()) {
            String itemId = BuiltInRegistries.ITEM.getKey(entry.getKey()).toString();
            nbt.putInt(itemId, entry.getValue());
        }
        
        return nbt;
    }
    
    public static OutputBuffer fromNBT(CompoundTag nbt) {
        OutputBuffer buffer = new OutputBuffer();
        
        for (String key : nbt.getAllKeys()) {
            ResourceLocation itemId = new ResourceLocation(key);
            Item item = BuiltInRegistries.ITEM.get(itemId);
            int count = nbt.getInt(key);
            buffer.addItem(item, count);
        }
        
        return buffer;
    }
}

// Usage in farm system
public class VirtualFarm {
    private OutputBuffer outputBuffer = new OutputBuffer();
    
    public void harvestCrop(VirtualPlot plot) {
        if (plot.isReady()) {
            // Add items to buffer instead of directly to inventory
            outputBuffer.addItem(Items.WHEAT, 3);
            plot.reset();
        }
    }
    
    public void collectOutput(ServerPlayer player) {
        if (!outputBuffer.isEmpty()) {
            outputBuffer.collectAll(player);
            player.sendSystemMessage(Component.literal("§a[FARM] Collected all output!"));
        } else {
            player.sendSystemMessage(Component.literal("§c[FARM] Output buffer is empty!"));
        }
    }
    
    // Display in GUI
    public GuiElementBuilder createOutputButton() {
        int totalItems = outputBuffer.getTotalItems();
        
        GuiElementBuilder builder = new GuiElementBuilder()
            .setItem(Items.CHEST)
            .setName(Component.literal("§eCollect Output"))
            .setCount(Math.min(64, Math.max(1, totalItems)));
        
        if (totalItems > 0) {
            builder.addLoreLine(Component.literal("§7Items ready: §e" + totalItems));
            
            for (Map.Entry<Item, Integer> entry : outputBuffer.getContents().entrySet()) {
                builder.addLoreLine(Component.literal(
                    "§7  " + entry.getValue() + "x §f" + entry.getKey().getDescription().getString()
                ));
            }
            
            builder.addLoreLine(Component.literal(""));
            builder.addLoreLine(Component.literal("§aClick to collect!"));
        } else {
            builder.addLoreLine(Component.literal("§7No items ready"));
        }
        
        return builder;
    }
}
```

---

## <a id="economy"></a>Economy Integration

Complete economy system with transactions and balance management.

```java
public class EconomyManager {
    private static final String ECONOMY_KEY = "economy";
    
    // Get player balance
    public static int getBalance(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains(ECONOMY_KEY)) {
            return 0;
        }
        return nbt.getCompound(ECONOMY_KEY).getInt("balance");
    }
    
    // Set player balance
    public static void setBalance(ServerPlayer player, int amount) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag economyData = nbt.getCompound(ECONOMY_KEY);
        economyData.putInt("balance", Math.max(0, amount));
        nbt.put(ECONOMY_KEY, economyData);
    }
    
    // Add money
    public static void addMoney(ServerPlayer player, int amount) {
        setBalance(player, getBalance(player) + amount);
        player.sendSystemMessage(Component.literal("§a+$" + amount));
    }
    
    // Remove money
    public static boolean removeMoney(ServerPlayer player, int amount) {
        if (!hasBalance(player, amount)) {
            return false;
        }
        setBalance(player, getBalance(player) - amount);
        player.sendSystemMessage(Component.literal("§c-$" + amount));
        return true;
    }
    
    // Check if player has enough money
    public static boolean hasBalance(ServerPlayer player, int amount) {
        return getBalance(player) >= amount;
    }
    
    // Transaction with confirmation
    public static boolean transaction(ServerPlayer from, ServerPlayer to, int amount, String reason) {
        if (!hasBalance(from, amount)) {
            from.sendSystemMessage(Component.literal("§cInsufficient funds!"));
            return false;
        }
        
        removeMoney(from, amount);
        addMoney(to, amount);
        
        from.sendSystemMessage(Component.literal(
            "§e[Transaction] -$" + amount + " §7(" + reason + ")"
        ));
        to.sendSystemMessage(Component.literal(
            "§e[Transaction] +$" + amount + " §7(" + reason + ")"
        ));
        
        return true;
    }
    
    // Purchase system
    public static boolean purchase(ServerPlayer player, String itemName, int price) {
        if (!hasBalance(player, price)) {
            player.sendSystemMessage(Component.literal("§cNot enough money! Need $" + price));
            return false;
        }
        
        removeMoney(player, price);
        player.sendSystemMessage(Component.literal(
            "§aPurchased " + itemName + " for $" + price
        ));
        
        // Log transaction
        logTransaction(player, "purchase", itemName, price);
        
        return true;
    }
    
    // Transaction history
    private static void logTransaction(ServerPlayer player, String type, String item, int amount) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag economyData = nbt.getCompound(ECONOMY_KEY);
        
        ListTag transactions = economyData.getList("transactions", 10);  // 10 = COMPOUND
        
        CompoundTag transaction = new CompoundTag();
        transaction.putString("type", type);
        transaction.putString("item", item);
        transaction.putInt("amount", amount);
        transaction.putLong("timestamp", System.currentTimeMillis());
        
        transactions.add(transaction);
        
        // Keep only last 100 transactions
        while (transactions.size() > 100) {
            transactions.remove(0);
        }
        
        economyData.put("transactions", transactions);
        nbt.put(ECONOMY_KEY, economyData);
    }
    
    // Get transaction history
    public static List<String> getTransactionHistory(ServerPlayer player, int count) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains(ECONOMY_KEY)) {
            return new ArrayList<>();
        }
        
        CompoundTag economyData = nbt.getCompound(ECONOMY_KEY);
        ListTag transactions = economyData.getList("transactions", 10);
        
        List<String> history = new ArrayList<>();
        int startIndex = Math.max(0, transactions.size() - count);
        
        for (int i = startIndex; i < transactions.size(); i++) {
            CompoundTag transaction = transactions.getCompound(i);
            String type = transaction.getString("type");
            String item = transaction.getString("item");
            int amount = transaction.getInt("amount");
            
            history.add(String.format("%s: %s for $%d", type, item, amount));
        }
        
        return history;
    }
}
```

---

## <a id="autosave"></a>Auto-Save System

Automatic periodic saving with graceful shutdown.

```java
public class AutoSaveManager {
    private static int tickCounter = 0;
    private static final int SAVE_INTERVAL = 20 * 60 * 5;  // 5 minutes
    private static boolean isDirty = false;
    
    public static void markDirty() {
        isDirty = true;
    }
    
    public static void register(ModDataManager dataManager) {
        // Periodic auto-save
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            
            if (tickCounter >= SAVE_INTERVAL) {
                if (isDirty) {
                    dataManager.saveAll();
                    server.getLogger().info("[AutoSave] Saved all player data");
                    isDirty = false;
                }
                tickCounter = 0;
            }
        });
        
        // Save on server stop
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            dataManager.saveAll();
            server.getLogger().info("[AutoSave] Final save complete");
        });
        
        // Save on player disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            dataManager.savePlayerData(player);
            dataManager.unloadPlayer(player);
        });
    }
}

// Usage in data manager
public class ModDataManager {
    public void modifyPlayerData(ServerPlayer player, Consumer<PlayerData> modifier) {
        PlayerData data = getPlayerData(player);
        modifier.accept(data);
        AutoSaveManager.markDirty();  // Mark for next auto-save
    }
}
```

---

## <a id="events"></a>Event-Driven Architecture

Custom event system for mod extensibility.

```java
public class ModEvents {
    // Define custom events
    public interface PlayerMoneyChanged {
        void onMoneyChanged(ServerPlayer player, int oldBalance, int newBalance);
    }
    
    public interface CropHarvested {
        void onHarvest(ServerPlayer player, String cropType, int amount);
    }
    
    // Event registries
    private static final List<PlayerMoneyChanged> moneyListeners = new ArrayList<>();
    private static final List<CropHarvested> harvestListeners = new ArrayList<>();
    
    // Register listeners
    public static void registerMoneyListener(PlayerMoneyChanged listener) {
        moneyListeners.add(listener);
    }
    
    public static void registerHarvestListener(CropHarvested listener) {
        harvestListeners.add(listener);
    }
    
    // Fire events
    public static void fireMoneyChanged(ServerPlayer player, int oldBalance, int newBalance) {
        moneyListeners.forEach(listener -> 
            listener.onMoneyChanged(player, oldBalance, newBalance)
        );
    }
    
    public static void fireHarvest(ServerPlayer player, String cropType, int amount) {
        harvestListeners.forEach(listener -> 
            listener.onHarvest(player, cropType, amount)
        );
    }
}

// Usage - Register listeners in onInitialize()
ModEvents.registerMoneyListener((player, oldBalance, newBalance) -> {
    if (newBalance > oldBalance) {
        player.sendSystemMessage(Component.literal("§a+$" + (newBalance - oldBalance)));
    }
});

ModEvents.registerHarvestListener((player, cropType, amount) -> {
    QuestManager.updateQuestProgress(player, "harvest_" + cropType, amount);
});

// Fire events when data changes
public static void addMoney(ServerPlayer player, int amount) {
    int oldBalance = getBalance(player);
    setBalance(player, oldBalance + amount);
    ModEvents.fireMoneyChanged(player, oldBalance, getBalance(player));
}
```

---

## <a id="permissions"></a>Custom Permission System

Role-based permission system.

```java
public class PermissionManager {
    private static final String PERMISSIONS_KEY = "permissions";
    
    public enum Role {
        DEFAULT(0),
        VIP(1),
        MODERATOR(2),
        ADMIN(3);
        
        private final int level;
        
        Role(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    // Get player role
    public static Role getRole(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains(PERMISSIONS_KEY)) {
            return Role.DEFAULT;
        }
        
        String roleName = nbt.getCompound(PERMISSIONS_KEY).getString("role");
        try {
            return Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            return Role.DEFAULT;
        }
    }
    
    // Set player role
    public static void setRole(ServerPlayer player, Role role) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag permData = nbt.getCompound(PERMISSIONS_KEY);
        permData.putString("role", role.name());
        nbt.put(PERMISSIONS_KEY, permData);
        
        player.sendSystemMessage(Component.literal("§eYour role has been updated to: §a" + role.name()));
    }
    
    // Check permission
    public static boolean hasPermission(ServerPlayer player, Role requiredRole) {
        return getRole(player).getLevel() >= requiredRole.getLevel();
    }
    
    // Grant temporary permission
    public static void grantTemporaryPermission(ServerPlayer player, String permission, long durationTicks) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag permData = nbt.getCompound(PERMISSIONS_KEY);
        
        permData.putLong(permission, System.currentTimeMillis() + (durationTicks * 50));
        nbt.put(PERMISSIONS_KEY, permData);
    }
    
    // Check temporary permission
    public static boolean hasTemporaryPermission(ServerPlayer player, String permission) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains(PERMISSIONS_KEY)) return false;
        
        CompoundTag permData = nbt.getCompound(PERMISSIONS_KEY);
        if (!permData.contains(permission)) return false;
        
        long expiry = permData.getLong(permission);
        return System.currentTimeMillis() < expiry;
    }
}

// Usage in commands
dispatcher.register(Commands.literal("vipcommand")
    .requires(source -> {
        try {
            ServerPlayer player = source.getPlayerOrException();
            return PermissionManager.hasPermission(player, PermissionManager.Role.VIP);
        } catch (CommandSyntaxException e) {
            return false;
        }
    })
    .executes(context -> {
        // VIP-only command
        return 1;
    })
);
```

---

## <a id="navigation"></a>Multi-GUI Navigation System

Navigate between multiple GUIs with back button support.

```java
public abstract class NavigableGui extends SimpleGui {
    private GuiHistory history;
    
    public NavigableGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots, GuiHistory history) {
        super(type, player, manipulatePlayerSlots);
        this.history = history;
        
        // Add back button
        if (history != null && history.canGoBack()) {
            addBackButton();
        }
    }
    
    private void addBackButton() {
        this.setSlot(0, new GuiElementBuilder()
            .setItem(Items.BARRIER)
            .setName(Component.literal("§cBack"))
            .setCallback((index, type, action) -> {
                history.goBack();
            })
        );
    }
    
    protected void openGui(NavigableGui newGui) {
        history.push(this);
        newGui.open();
    }
}

public class GuiHistory {
    private Stack<NavigableGui> history = new Stack<>();
    
    public void push(NavigableGui gui) {
        history.push(gui);
    }
    
    public void goBack() {
        if (!history.isEmpty()) {
            NavigableGui previous = history.pop();
            previous.open();
        }
    }
    
    public boolean canGoBack() {
        return !history.isEmpty();
    }
    
    public void clear() {
        history.clear();
    }
}

// Usage
public class MainGui extends NavigableGui {
    public MainGui(ServerPlayer player, GuiHistory history) {
        super(MenuType.GENERIC_9x3, player, false, history);
        this.setTitle(Component.literal("§6Main Menu"));
        
        this.setSlot(13, new GuiElementBuilder()
            .setItem(Items.WHEAT)
            .setName(Component.literal("§aOpen Farm"))
            .setCallback((index, type, action) -> {
                openGui(new FarmGui(player, history));
            })
        );
    }
}

public class FarmGui extends NavigableGui {
    public FarmGui(ServerPlayer player, GuiHistory history) {
        super(MenuType.GENERIC_9x6, player, false, history);
        this.setTitle(Component.literal("§6Farm"));
        
        // Back button automatically added
        // Content here...
    }
}

// Open from command
GuiHistory history = new GuiHistory();
new MainGui(player, history).open();
```

---

## Best Practices for Advanced Systems

✅ **Use pagination for large datasets** - Never display 100+ items at once
✅ **Buffer outputs** - Accumulate rewards before transferring to inventory
✅ **Auto-save critical data** - Don't rely on manual saves
✅ **Event-driven design** - Decouple systems with custom events
✅ **Permission layers** - Implement custom permissions beyond OP levels
✅ **Navigation stack** - Track GUI history for back button support
✅ **Cache frequently accessed data** - Avoid repeated NBT reads
✅ **Graceful degradation** - Handle missing/corrupted data

---

## Next Steps

Apply these patterns with:
- [03_GUI_SYSTEMS.md](03_GUI_SYSTEMS.md) - Build advanced GUIs
- [05_DATA_STORAGE.md](05_DATA_STORAGE.md) - Persist complex data
- [08_PATTERNS.md](08_PATTERNS.md) - Combine with basic patterns

---

**Version Note:** All code is for Minecraft 1.21.11 with Mojang mappings.
