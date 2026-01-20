package com.vanillaplus.rpg.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vanillaplus.rpg.VanillaPlusRpg;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Shop configuration loader
 * Reads shop.json and creates a HashMap<Item, ShopEntry> for fast lookup
 */
public class ShopConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Item, ShopEntry> SHOP_DATA = new HashMap<>();
    
    /**
     * Shop entry containing buy/sell prices and black market cost
     */
    public static class ShopEntry {
        public long buy = -1;      // -1 means not buyable
        public long sell = 0;      // 0 means not sellable
        public int blackMarketCost = 0;  // Level cost for black market items
        
        public boolean isBuyable() {
            return buy > 0;
        }
        
        public boolean isSellable() {
            return sell > 0;
        }
        
        public boolean isBlackMarket() {
            return blackMarketCost > 0;
        }
    }
    
    /**
     * Initialize the shop configuration
     */
    public static void init() {
        loadShopConfig();
        VanillaPlusRpg.LOGGER.info("Loaded {} shop entries", SHOP_DATA.size());
    }
    
    /**
     * Load shop configuration from JSON
     */
    private static void loadShopConfig() {
        try {
            // Load from resources
            InputStream stream = ShopConfig.class.getResourceAsStream("/data/vanillaplusrpg/shop.json");
            if (stream == null) {
                VanillaPlusRpg.LOGGER.warn("Could not find shop.json, using defaults");
                loadDefaults();
                return;
            }
            
            InputStreamReader reader = new InputStreamReader(stream);
            Type type = new TypeToken<Map<String, ShopEntry>>(){}.getType();
            Map<String, ShopEntry> rawData = GSON.fromJson(reader, type);
            reader.close();
            
            // Convert string keys to Item objects
            for (Map.Entry<String, ShopEntry> entry : rawData.entrySet()) {
                String itemId = entry.getKey();
                
                // 1.21.11: Use Identifier.fromNamespaceAndPath() or .of()
                Identifier id;
                if (itemId.contains(":")) {
                    String[] parts = itemId.split(":");
                    id = Identifier.fromNamespaceAndPath(parts[0], parts[1]);
                } else {
                    // Default to minecraft namespace
                    id = Identifier.fromNamespaceAndPath("minecraft", itemId);
                }
                
                Item item = BuiltInRegistries.ITEM.getValue(id);
                if (item != null) {
                    SHOP_DATA.put(item, entry.getValue());
                } else {
                    VanillaPlusRpg.LOGGER.warn("Unknown item in shop.json: {}", itemId);
                }
            }
            
        } catch (Exception e) {
            VanillaPlusRpg.LOGGER.error("Failed to load shop.json", e);
            loadDefaults();
        }
    }
    
    /**
     * Load default shop entries if JSON fails
     */
    private static void loadDefaults() {
        // Diamond
        ShopEntry diamond = new ShopEntry();
        diamond.buy = 500;
        diamond.sell = 100;
        SHOP_DATA.put(net.minecraft.world.item.Items.DIAMOND, diamond);
        
        // Emerald
        ShopEntry emerald = new ShopEntry();
        emerald.buy = 200;
        emerald.sell = 40;
        SHOP_DATA.put(net.minecraft.world.item.Items.EMERALD, emerald);
        
        // Gold Ingot
        ShopEntry gold = new ShopEntry();
        gold.buy = 50;
        gold.sell = 10;
        SHOP_DATA.put(net.minecraft.world.item.Items.GOLD_INGOT, gold);
        
        // Iron Ingot
        ShopEntry iron = new ShopEntry();
        iron.buy = 25;
        iron.sell = 5;
        SHOP_DATA.put(net.minecraft.world.item.Items.IRON_INGOT, iron);
        
        // Cobblestone
        ShopEntry cobble = new ShopEntry();
        cobble.buy = 5;
        cobble.sell = 1;
        SHOP_DATA.put(net.minecraft.world.item.Items.COBBLESTONE, cobble);
    }
    
    /**
     * Get the buy price for an item
     * @return buy price or -1 if not buyable
     */
    public static long getBuyPrice(Item item) {
        ShopEntry entry = SHOP_DATA.get(item);
        return entry != null ? entry.buy : -1;
    }
    
    /**
     * Get the sell price for an item
     * @return sell price or 0 if not sellable
     */
    public static long getSellPrice(Item item) {
        ShopEntry entry = SHOP_DATA.get(item);
        return entry != null ? entry.sell : 0;
    }
    
    /**
     * Get the black market cost (in levels) for an item
     * @return level cost or 0 if not a black market item
     */
    public static int getBlackMarketCost(Item item) {
        ShopEntry entry = SHOP_DATA.get(item);
        return entry != null ? entry.blackMarketCost : 0;
    }
    
    /**
     * Check if an item is in the shop
     */
    public static boolean isInShop(Item item) {
        return SHOP_DATA.containsKey(item);
    }
    
    /**
     * Check if an item is a black market item
     */
    public static boolean isBlackMarket(Item item) {
        ShopEntry entry = SHOP_DATA.get(item);
        return entry != null && entry.isBlackMarket();
    }
    
    /**
     * Get all shop entries
     */
    public static Map<Item, ShopEntry> getAllEntries() {
        return new HashMap<>(SHOP_DATA);
    }
    
    /**
     * Get the shop entry for an item
     */
    public static ShopEntry getEntry(Item item) {
        return SHOP_DATA.get(item);
    }
}
