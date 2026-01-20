package com.vanillaplus.rpg.economy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple item pricing system (no tiers, no upgrades)
 * Buy price is what player pays, sell price is what player receives (80% of buy)
 * Inspired by shop mod but simplified for vanilla+ feel
 */
public class ItemPricing {
    
    private static final Map<Item, PriceData> PRICES = new HashMap<>();
    
    static {
        // Initialize prices for common items
        // Format: item, buyPrice (sell = 80% of buy)
        
        // === ORES & MINERALS ===
        addItem(Items.COAL, 5);
        addItem(Items.IRON_INGOT, 25);
        addItem(Items.COPPER_INGOT, 15);
        addItem(Items.GOLD_INGOT, 100);
        addItem(Items.DIAMOND, 500);
        addItem(Items.EMERALD, 300);
        addItem(Items.LAPIS_LAZULI, 20);
        addItem(Items.REDSTONE, 10);
        addItem(Items.QUARTZ, 15);
        addItem(Items.AMETHYST_SHARD, 30);
        addItem(Items.NETHERITE_INGOT, 5000);
        addItem(Items.RAW_IRON, 20);
        addItem(Items.RAW_GOLD, 80);
        addItem(Items.RAW_COPPER, 10);
        
        // === FOOD ===
        addItem(Items.WHEAT, 5);
        addItem(Items.CARROT, 8);
        addItem(Items.POTATO, 8);
        addItem(Items.BEETROOT, 10);
        addItem(Items.APPLE, 15);
        addItem(Items.MELON_SLICE, 3);
        addItem(Items.PUMPKIN, 20);
        addItem(Items.BREAD, 15);
        addItem(Items.COOKED_BEEF, 25);
        addItem(Items.COOKED_PORKCHOP, 25);
        addItem(Items.COOKED_CHICKEN, 20);
        addItem(Items.COOKED_MUTTON, 22);
        addItem(Items.COOKED_COD, 18);
        addItem(Items.COOKED_SALMON, 22);
        addItem(Items.GOLDEN_APPLE, 500);
        addItem(Items.ENCHANTED_GOLDEN_APPLE, 10000);
        addItem(Items.GOLDEN_CARROT, 150);
        
        // === BUILDING BLOCKS ===
        addItem(Items.COBBLESTONE, 1);
        addItem(Items.STONE, 2);
        addItem(Items.DIRT, 1);
        addItem(Items.SAND, 3);
        addItem(Items.GRAVEL, 2);
        addItem(Items.OAK_LOG, 5);
        addItem(Items.SPRUCE_LOG, 5);
        addItem(Items.BIRCH_LOG, 5);
        addItem(Items.JUNGLE_LOG, 6);
        addItem(Items.ACACIA_LOG, 6);
        addItem(Items.DARK_OAK_LOG, 6);
        addItem(Items.CHERRY_LOG, 8);
        addItem(Items.MANGROVE_LOG, 7);
        addItem(Items.OAK_PLANKS, 2);
        addItem(Items.GLASS, 5);
        addItem(Items.BRICK, 8);
        addItem(Items.STONE_BRICKS, 4);
        addItem(Items.DEEPSLATE, 3);
        addItem(Items.COBBLED_DEEPSLATE, 3);
        addItem(Items.CLAY_BALL, 5);
        addItem(Items.TERRACOTTA, 10);
        
        // === TOOLS ===
        addItem(Items.WOODEN_PICKAXE, 10);
        addItem(Items.STONE_PICKAXE, 25);
        addItem(Items.IRON_PICKAXE, 150);
        addItem(Items.GOLDEN_PICKAXE, 400);
        addItem(Items.DIAMOND_PICKAXE, 2000);
        addItem(Items.NETHERITE_PICKAXE, 15000);
        addItem(Items.WOODEN_AXE, 10);
        addItem(Items.STONE_AXE, 25);
        addItem(Items.IRON_AXE, 150);
        addItem(Items.DIAMOND_AXE, 2000);
        addItem(Items.WOODEN_SHOVEL, 8);
        addItem(Items.STONE_SHOVEL, 20);
        addItem(Items.IRON_SHOVEL, 120);
        addItem(Items.DIAMOND_SHOVEL, 1500);
        addItem(Items.WOODEN_HOE, 8);
        addItem(Items.STONE_HOE, 20);
        addItem(Items.IRON_HOE, 120);
        addItem(Items.DIAMOND_HOE, 1500);
        
        // === WEAPONS ===
        addItem(Items.WOODEN_SWORD, 10);
        addItem(Items.STONE_SWORD, 25);
        addItem(Items.IRON_SWORD, 150);
        addItem(Items.GOLDEN_SWORD, 400);
        addItem(Items.DIAMOND_SWORD, 2000);
        addItem(Items.NETHERITE_SWORD, 15000);
        addItem(Items.BOW, 100);
        addItem(Items.CROSSBOW, 200);
        addItem(Items.ARROW, 3);
        addItem(Items.SPECTRAL_ARROW, 15);
        addItem(Items.SHIELD, 80);
        addItem(Items.TRIDENT, 5000);
        
        // === ARMOR ===
        addItem(Items.LEATHER_HELMET, 50);
        addItem(Items.LEATHER_CHESTPLATE, 80);
        addItem(Items.LEATHER_LEGGINGS, 70);
        addItem(Items.LEATHER_BOOTS, 40);
        addItem(Items.IRON_HELMET, 250);
        addItem(Items.IRON_CHESTPLATE, 400);
        addItem(Items.IRON_LEGGINGS, 350);
        addItem(Items.IRON_BOOTS, 200);
        addItem(Items.DIAMOND_HELMET, 2500);
        addItem(Items.DIAMOND_CHESTPLATE, 4000);
        addItem(Items.DIAMOND_LEGGINGS, 3500);
        addItem(Items.DIAMOND_BOOTS, 2000);
        addItem(Items.NETHERITE_HELMET, 18000);
        addItem(Items.NETHERITE_CHESTPLATE, 28000);
        addItem(Items.NETHERITE_LEGGINGS, 24000);
        addItem(Items.NETHERITE_BOOTS, 15000);
        
        // === FARMING ===
        addItem(Items.WHEAT_SEEDS, 2);
        addItem(Items.MELON_SEEDS, 5);
        addItem(Items.PUMPKIN_SEEDS, 5);
        addItem(Items.BEETROOT_SEEDS, 3);
        addItem(Items.BONE_MEAL, 5);
        addItem(Items.STRING, 8);
        addItem(Items.FEATHER, 5);
        addItem(Items.LEATHER, 15);
        addItem(Items.EGG, 5);
        addItem(Items.HONEY_BOTTLE, 50);
        addItem(Items.HONEYCOMB, 30);
        addItem(Items.MILK_BUCKET, 25);
        
        // === REDSTONE ===
        addItem(Items.REDSTONE_TORCH, 8);
        addItem(Items.REPEATER, 30);
        addItem(Items.COMPARATOR, 50);
        addItem(Items.PISTON, 60);
        addItem(Items.STICKY_PISTON, 100);
        addItem(Items.OBSERVER, 80);
        addItem(Items.HOPPER, 200);
        addItem(Items.DROPPER, 50);
        addItem(Items.DISPENSER, 60);
        addItem(Items.LEVER, 5);
        addItem(Items.STONE_BUTTON, 5);
        addItem(Items.STONE_PRESSURE_PLATE, 10);
        
        // === DECORATIVE ===
        addItem(Items.TORCH, 2);
        addItem(Items.LANTERN, 20);
        addItem(Items.SOUL_LANTERN, 25);
        addItem(Items.PAINTING, 30);
        addItem(Items.ITEM_FRAME, 25);
        addItem(Items.FLOWER_POT, 15);
        addItem(Items.BOOKSHELF, 100);
        addItem(Items.CHEST, 50);
        addItem(Items.BARREL, 45);
        addItem(Items.CRAFTING_TABLE, 20);
        addItem(Items.FURNACE, 40);
        addItem(Items.SMOKER, 60);
        addItem(Items.BLAST_FURNACE, 80);
        addItem(Items.ANVIL, 500);
        addItem(Items.ENCHANTING_TABLE, 3000);
        addItem(Items.BREWING_STAND, 500);
        addItem(Items.CAULDRON, 150);
        addItem(Items.WHITE_BED, 75);
        
        // === NETHER ===
        addItem(Items.NETHERRACK, 2);
        addItem(Items.NETHER_BRICKS, 10);
        addItem(Items.SOUL_SAND, 8);
        addItem(Items.SOUL_SOIL, 8);
        addItem(Items.GLOWSTONE, 50);
        addItem(Items.GLOWSTONE_DUST, 15);
        addItem(Items.NETHER_WART, 20);
        addItem(Items.BLAZE_ROD, 150);
        addItem(Items.BLAZE_POWDER, 80);
        addItem(Items.MAGMA_CREAM, 60);
        addItem(Items.GHAST_TEAR, 200);
        addItem(Items.WITHER_SKELETON_SKULL, 1000);
        addItem(Items.NETHER_STAR, 10000);
        
        // === END ===
        addItem(Items.END_STONE, 5);
        addItem(Items.END_STONE_BRICKS, 8);
        addItem(Items.CHORUS_FRUIT, 25);
        addItem(Items.POPPED_CHORUS_FRUIT, 30);
        addItem(Items.ENDER_PEARL, 100);
        addItem(Items.ENDER_EYE, 250);
        addItem(Items.SHULKER_SHELL, 500);
        addItem(Items.ELYTRA, 50000);
        addItem(Items.DRAGON_BREATH, 300);
        addItem(Items.DRAGON_EGG, 100000);
        
        // === MISCELLANEOUS ===
        addItem(Items.BOOK, 30);
        addItem(Items.PAPER, 5);
        addItem(Items.NAME_TAG, 150);
        addItem(Items.SADDLE, 200);
        addItem(Items.LEAD, 50);
        addItem(Items.CLOCK, 200);
        addItem(Items.COMPASS, 150);
        addItem(Items.MAP, 80);
        addItem(Items.BUCKET, 50);
        addItem(Items.WATER_BUCKET, 60);
        addItem(Items.LAVA_BUCKET, 200);
        addItem(Items.SLIME_BALL, 40);
        addItem(Items.GUNPOWDER, 30);
        addItem(Items.SPIDER_EYE, 20);
        addItem(Items.ROTTEN_FLESH, 3);
        addItem(Items.BONE, 10);
        addItem(Items.INK_SAC, 15);
        addItem(Items.GLOW_INK_SAC, 50);
        addItem(Items.NAUTILUS_SHELL, 300);
        addItem(Items.HEART_OF_THE_SEA, 2000);
        addItem(Items.TOTEM_OF_UNDYING, 10000);
        addItem(Items.EXPERIENCE_BOTTLE, 100);
        
        // === ADDITIONAL TOOLS ===
        addItem(Items.GOLDEN_AXE, 400);
        addItem(Items.NETHERITE_AXE, 15000);
        addItem(Items.GOLDEN_SHOVEL, 350);
        addItem(Items.NETHERITE_SHOVEL, 12000);
        addItem(Items.GOLDEN_HOE, 350);
        addItem(Items.NETHERITE_HOE, 12000);
        addItem(Items.SHEARS, 50);
        addItem(Items.FLINT_AND_STEEL, 40);
        addItem(Items.FISHING_ROD, 75);
        addItem(Items.SPYGLASS, 300);
        addItem(Items.BRUSH, 100);
        
        // === GOLDEN ARMOR ===
        addItem(Items.GOLDEN_HELMET, 500);
        addItem(Items.GOLDEN_CHESTPLATE, 800);
        addItem(Items.GOLDEN_LEGGINGS, 700);
        addItem(Items.GOLDEN_BOOTS, 400);
        
        // === CHAINMAIL ARMOR ===
        addItem(Items.CHAINMAIL_HELMET, 200);
        addItem(Items.CHAINMAIL_CHESTPLATE, 350);
        addItem(Items.CHAINMAIL_LEGGINGS, 300);
        addItem(Items.CHAINMAIL_BOOTS, 175);
        
        // === TURTLE ARMOR ===
        addItem(Items.TURTLE_HELMET, 1500);
        
        // === MORE FOOD ===
        addItem(Items.COOKED_RABBIT, 20);
        addItem(Items.BAKED_POTATO, 12);
        addItem(Items.PUMPKIN_PIE, 35);
        addItem(Items.COOKIE, 8);
        addItem(Items.CAKE, 100);
        addItem(Items.SWEET_BERRIES, 10);
        addItem(Items.GLOW_BERRIES, 20);
        addItem(Items.MUSHROOM_STEW, 30);
        addItem(Items.RABBIT_STEW, 50);
        addItem(Items.SUSPICIOUS_STEW, 100);
        addItem(Items.BEETROOT_SOUP, 25);
        
        // === MORE BUILDING BLOCKS ===
        addItem(Items.SPRUCE_PLANKS, 2);
        addItem(Items.BIRCH_PLANKS, 2);
        addItem(Items.JUNGLE_PLANKS, 3);
        addItem(Items.ACACIA_PLANKS, 3);
        addItem(Items.DARK_OAK_PLANKS, 3);
        addItem(Items.GLASS_PANE, 3);
        addItem(Items.WHITE_WOOL, 15);
        addItem(Items.WHITE_CONCRETE, 8);
        addItem(Items.BRICKS, 40);
        addItem(Items.DEEPSLATE_BRICKS, 10);
        addItem(Items.SANDSTONE, 5);
        addItem(Items.RED_SANDSTONE, 6);
        addItem(Items.QUARTZ_BLOCK, 60);
        addItem(Items.PRISMARINE, 50);
        addItem(Items.SEA_LANTERN, 100);
        addItem(Items.OAK_DOOR, 15);
        addItem(Items.IRON_DOOR, 150);
        addItem(Items.LADDER, 5);
        addItem(Items.GRINDSTONE, 75);
        addItem(Items.SMITHING_TABLE, 100);
        addItem(Items.LECTERN, 150);
        addItem(Items.RED_BED, 75);
        addItem(Items.BAMBOO, 3);
        addItem(Items.SUGAR_CANE, 5);
        addItem(Items.CACTUS, 10);
        addItem(Items.HAY_BLOCK, 50);
        addItem(Items.COMPOSTER, 25);
        
        // === ORE BLOCKS ===
        addItem(Items.IRON_BLOCK, 225);
        addItem(Items.GOLD_BLOCK, 900);
        addItem(Items.DIAMOND_BLOCK, 4500);
        addItem(Items.EMERALD_BLOCK, 2700);
        addItem(Items.LAPIS_BLOCK, 180);
        addItem(Items.REDSTONE_BLOCK, 90);
        addItem(Items.COPPER_BLOCK, 135);
        addItem(Items.NETHERITE_BLOCK, 45000);
        addItem(Items.COAL_BLOCK, 45);
        
        // === POTIONS (base) ===
        addItem(Items.GLASS_BOTTLE, 5);
        addItem(Items.FERMENTED_SPIDER_EYE, 50);
        addItem(Items.GLISTERING_MELON_SLICE, 100);
        addItem(Items.RABBIT_FOOT, 150);
        addItem(Items.PHANTOM_MEMBRANE, 200);
        
        // === MACE (new 1.21 weapon) ===
        addItem(Items.MACE, 8000);
        addItem(Items.WIND_CHARGE, 100);
        
        // === TIPPED ARROWS ===
        addItem(Items.TIPPED_ARROW, 50);
        
        // === MORE TOOLS ===
        addItem(Items.GOLDEN_AXE, 400);
        addItem(Items.NETHERITE_AXE, 15000);
        addItem(Items.GOLDEN_SHOVEL, 350);
        addItem(Items.NETHERITE_SHOVEL, 12000);
        addItem(Items.GOLDEN_HOE, 350);
        addItem(Items.NETHERITE_HOE, 12000);
        addItem(Items.BRUSH, 50);
        addItem(Items.SPYGLASS, 200);
        addItem(Items.CLOCK, 400);
        addItem(Items.COMPASS, 150);
        addItem(Items.LEAD, 50);
        addItem(Items.NAME_TAG, 250);
        addItem(Items.SADDLE, 300);
        addItem(Items.CARROT_ON_A_STICK, 80);
        addItem(Items.WARPED_FUNGUS_ON_A_STICK, 100);
        
        // === MORE MISC ===
        addItem(Items.BOOK, 30);
        addItem(Items.PAPER, 5);
        addItem(Items.INK_SAC, 15);
        addItem(Items.GLOW_INK_SAC, 50);
        addItem(Items.BONE, 10);
        addItem(Items.BONE_MEAL, 8);
        addItem(Items.STRING, 10);
        addItem(Items.SLIME_BALL, 50);
        addItem(Items.HONEY_BOTTLE, 50);
        addItem(Items.HONEYCOMB, 25);
        addItem(Items.MILK_BUCKET, 50);
        addItem(Items.WATER_BUCKET, 30);
        addItem(Items.LAVA_BUCKET, 150);
        addItem(Items.BUCKET, 75);
        addItem(Items.GUNPOWDER, 25);
        addItem(Items.BLAZE_ROD, 150);
        addItem(Items.BLAZE_POWDER, 80);
        addItem(Items.ENDER_PEARL, 200);
        addItem(Items.ENDER_EYE, 500);
        addItem(Items.GHAST_TEAR, 300);
        addItem(Items.MAGMA_CREAM, 100);
        addItem(Items.NETHER_STAR, 50000);
        addItem(Items.DRAGON_BREATH, 1000);
        addItem(Items.SHULKER_SHELL, 500);
        addItem(Items.SHULKER_BOX, 1200);
        addItem(Items.ELYTRA, 25000);
        addItem(Items.TOTEM_OF_UNDYING, 15000);
        addItem(Items.EXPERIENCE_BOTTLE, 100);
        addItem(Items.FIREWORK_ROCKET, 30);
        addItem(Items.FIREWORK_STAR, 50);
        addItem(Items.NETHERITE_SCRAP, 1500);
        
        // === DECORATION ===
        addItem(Items.PAINTING, 30);
        addItem(Items.ITEM_FRAME, 20);
        addItem(Items.GLOW_ITEM_FRAME, 70);
        addItem(Items.ARMOR_STAND, 40);
        addItem(Items.FLOWER_POT, 15);
        addItem(Items.CANDLE, 20);
        addItem(Items.BELL, 500);
        addItem(Items.IRON_BARS, 25);
        addItem(Items.LIGHTNING_ROD, 150);
        
        // === REDSTONE ===
        addItem(Items.LEVER, 5);
        addItem(Items.REDSTONE_TORCH, 15);
        addItem(Items.REPEATER, 50);
        addItem(Items.COMPARATOR, 75);
        addItem(Items.PISTON, 100);
        addItem(Items.STICKY_PISTON, 150);
        addItem(Items.OBSERVER, 100);
        addItem(Items.HOPPER, 250);
        addItem(Items.DROPPER, 80);
        addItem(Items.DISPENSER, 100);
        addItem(Items.NOTE_BLOCK, 50);
        addItem(Items.TRIPWIRE_HOOK, 30);
        addItem(Items.TARGET, 80);
        addItem(Items.DAYLIGHT_DETECTOR, 100);
        
        // === RAIL ITEMS ===
        addItem(Items.RAIL, 15);
        addItem(Items.POWERED_RAIL, 100);
        addItem(Items.DETECTOR_RAIL, 75);
        addItem(Items.ACTIVATOR_RAIL, 75);
        addItem(Items.MINECART, 100);
        addItem(Items.CHEST_MINECART, 150);
        addItem(Items.HOPPER_MINECART, 350);
        addItem(Items.TNT_MINECART, 200);
        addItem(Items.FURNACE_MINECART, 150);
        
        // === BOATS ===
        addItem(Items.OAK_BOAT, 30);
        addItem(Items.OAK_CHEST_BOAT, 80);
        addItem(Items.SPRUCE_BOAT, 30);
        addItem(Items.BIRCH_BOAT, 30);
        
        // === SEEDS & FARMING ===
        addItem(Items.WHEAT_SEEDS, 3);
        addItem(Items.BEETROOT_SEEDS, 5);
        addItem(Items.MELON_SEEDS, 8);
        addItem(Items.PUMPKIN_SEEDS, 8);
        addItem(Items.TORCHFLOWER_SEEDS, 50);
        addItem(Items.PITCHER_POD, 50);
        addItem(Items.EGG, 10);
        addItem(Items.FEATHER, 5);
        addItem(Items.LEATHER, 20);
        addItem(Items.RABBIT_HIDE, 15);
        addItem(Items.WHITE_WOOL, 15);
        
        // === SPAWN EGGS (expensive!) ===
        addItem(Items.CHICKEN_SPAWN_EGG, 500);
        addItem(Items.COW_SPAWN_EGG, 500);
        addItem(Items.PIG_SPAWN_EGG, 500);
        addItem(Items.SHEEP_SPAWN_EGG, 500);
        addItem(Items.WOLF_SPAWN_EGG, 1000);
        addItem(Items.CAT_SPAWN_EGG, 800);
        addItem(Items.HORSE_SPAWN_EGG, 1500);
        addItem(Items.VILLAGER_SPAWN_EGG, 5000);
    }
    
    /**
     * Add item with buy price (sell = 80% of buy)
     */
    private static void addItem(Item item, long buyPrice) {
        long sellPrice = (long) (buyPrice * 0.8);
        PRICES.put(item, new PriceData(buyPrice, sellPrice));
    }
    
    /**
     * Get price data for an item
     */
    public static PriceData getPrice(Item item) {
        return PRICES.get(item);
    }
    
    /**
     * Get buy price for an item
     */
    public static long getBuyPrice(Item item) {
        PriceData data = PRICES.get(item);
        return data != null ? data.buyPrice() : 0;
    }
    
    /**
     * Get sell price for an item
     */
    public static long getSellPrice(Item item) {
        PriceData data = PRICES.get(item);
        return data != null ? data.sellPrice() : 0;
    }
    
    /**
     * Check if item can be sold
     */
    public static boolean canSell(Item item) {
        return PRICES.containsKey(item) && getSellPrice(item) > 0;
    }
    
    /**
     * Check if item can be bought
     */
    public static boolean canBuy(Item item) {
        return PRICES.containsKey(item) && getBuyPrice(item) > 0;
    }
    
    /**
     * Get all priced items
     */
    public static Map<Item, PriceData> getAllPrices() {
        return new HashMap<>(PRICES);
    }
    
    /**
     * Price data record
     */
    public record PriceData(long buyPrice, long sellPrice) {}
}
