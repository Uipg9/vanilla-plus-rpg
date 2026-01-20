package com.vanillaplus.rpg.economy;

import com.vanillaplus.rpg.VanillaPlusRpg;
import com.vanillaplus.rpg.config.ShopConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dynamic Market Manager
 * Handles the "hot" and "cheap" items that rotate daily
 */
public class MarketManager {
    private static final Random RANDOM = new Random();
    
    // Current market status
    private static Item hotItem = null;       // 2x sell price
    private static Item cheapItem = null;     // 50% buy price
    
    // Multipliers
    public static final double HOT_SELL_MULTIPLIER = 2.0;
    public static final double CHEAP_BUY_MULTIPLIER = 0.5;
    
    // Last rotation time (in game ticks)
    private static long lastRotationTime = -1;
    
    // Rotation interval (24000 ticks = 1 Minecraft day)
    private static final long ROTATION_INTERVAL = 24000;
    
    /**
     * Initialize the market manager
     */
    public static void init(MinecraftServer server) {
        VanillaPlusRpg.LOGGER.info("Initializing Market Manager");
        rotateMarket(server);
    }
    
    /**
     * Tick handler - checks if it's time to rotate the market
     */
    public static void tick(MinecraftServer server) {
        if (server.overworld() == null) return;
        
        long worldTime = server.overworld().getDayTime();
        long dayTick = worldTime % ROTATION_INTERVAL;
        
        // Rotate at dawn (tick 0 of each day)
        if (dayTick == 0 && lastRotationTime != worldTime / ROTATION_INTERVAL) {
            lastRotationTime = worldTime / ROTATION_INTERVAL;
            rotateMarket(server);
        }
    }
    
    /**
     * Rotate the market - pick new hot and cheap items
     */
    public static void rotateMarket(MinecraftServer server) {
        List<Item> sellableItems = new ArrayList<>();
        List<Item> buyableItems = new ArrayList<>();
        
        // Get all sellable and buyable items (excluding black market)
        for (var entry : ShopConfig.getAllEntries().entrySet()) {
            Item item = entry.getKey();
            var shopEntry = entry.getValue();
            
            if (!shopEntry.isBlackMarket()) {
                if (shopEntry.isSellable()) {
                    sellableItems.add(item);
                }
                if (shopEntry.isBuyable()) {
                    buyableItems.add(item);
                }
            }
        }
        
        // Pick random hot item (high sell price)
        if (!sellableItems.isEmpty()) {
            Item newHot = sellableItems.get(RANDOM.nextInt(sellableItems.size()));
            // Avoid same item as before if possible
            if (sellableItems.size() > 1 && newHot == hotItem) {
                sellableItems.remove(newHot);
                newHot = sellableItems.get(RANDOM.nextInt(sellableItems.size()));
            }
            hotItem = newHot;
        }
        
        // Pick random cheap item (low buy price)
        if (!buyableItems.isEmpty()) {
            Item newCheap = buyableItems.get(RANDOM.nextInt(buyableItems.size()));
            // Avoid same as hot item and previous cheap item
            if (buyableItems.size() > 1) {
                while (newCheap == cheapItem || newCheap == hotItem) {
                    buyableItems.remove(newCheap);
                    if (buyableItems.isEmpty()) break;
                    newCheap = buyableItems.get(RANDOM.nextInt(buyableItems.size()));
                }
            }
            cheapItem = newCheap;
        }
        
        // Broadcast market update
        if (server != null && hotItem != null) {
            broadcastMarketUpdate(server);
        }
        
        VanillaPlusRpg.LOGGER.info("Market rotated - Hot: {}, Cheap: {}", 
            hotItem != null ? hotItem.getName(hotItem.getDefaultInstance()).getString() : "none",
            cheapItem != null ? cheapItem.getName(cheapItem.getDefaultInstance()).getString() : "none");
    }
    
    /**
     * Broadcast market update to all players
     */
    private static void broadcastMarketUpdate(MinecraftServer server) {
        if (hotItem == null) return;
        
        String hotName = hotItem.getName(hotItem.getDefaultInstance()).getString();
        String cheapName = cheapItem != null ? 
            cheapItem.getName(cheapItem.getDefaultInstance()).getString() : "nothing special";
        
        // 1.21.11: Use Component API
        Component message = Component.literal("§6[Market] §eDemand for §f" + hotName + 
            " §ehas skyrocketed! §7(2x sell price)");
        Component message2 = Component.literal("§6[Market] §a" + cheapName + 
            " §ais on sale! §7(50% off buy price)");
        
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
            player.sendSystemMessage(message2);
        }
    }
    
    /**
     * Get the current hot item
     */
    public static Item getHotItem() {
        return hotItem;
    }
    
    /**
     * Get the current cheap item
     */
    public static Item getCheapItem() {
        return cheapItem;
    }
    
    /**
     * Check if an item is the current hot item
     */
    public static boolean isHotItem(Item item) {
        return hotItem != null && hotItem == item;
    }
    
    /**
     * Check if an item is the current cheap item
     */
    public static boolean isCheapItem(Item item) {
        return cheapItem != null && cheapItem == item;
    }
    
    /**
     * Get the effective sell price for an item (with hot item bonus)
     */
    public static long getEffectiveSellPrice(Item item) {
        long basePrice = ShopConfig.getSellPrice(item);
        if (basePrice <= 0) return 0;
        
        if (isHotItem(item)) {
            return (long) (basePrice * HOT_SELL_MULTIPLIER);
        }
        return basePrice;
    }
    
    /**
     * Get the effective buy price for an item (with cheap item discount)
     */
    public static long getEffectiveBuyPrice(Item item) {
        long basePrice = ShopConfig.getBuyPrice(item);
        if (basePrice <= 0) return -1;
        
        if (isCheapItem(item)) {
            return (long) (basePrice * CHEAP_BUY_MULTIPLIER);
        }
        return basePrice;
    }
    
    /**
     * Get the name of the hot item (for HUD display)
     */
    public static String getHotItemName() {
        if (hotItem == null) return "None";
        return hotItem.getName(hotItem.getDefaultInstance()).getString();
    }
    
    /**
     * Get the name of the cheap item (for HUD display)
     */
    public static String getCheapItemName() {
        if (cheapItem == null) return "None";
        return cheapItem.getName(cheapItem.getDefaultInstance()).getString();
    }
}
