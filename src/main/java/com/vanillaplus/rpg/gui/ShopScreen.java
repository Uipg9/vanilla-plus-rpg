package com.vanillaplus.rpg.gui;

import com.vanillaplus.rpg.client.HudRenderer;
import com.vanillaplus.rpg.economy.ItemPricing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;

/**
 * Shop Screen - Catalog style shop with procedural rendering
 * Features:
 * - Grid layout of items from pricing data
 * - Category tabs for better organization
 * - Balance display at top
 * - Left-click to buy, Right-click to sell
 * - Hover highlight with white box
 * - Hidden Black Market button when holding Shift
 * 
 * Uses Button widgets for click handling, custom rendering on top
 */
public class ShopScreen extends BaseRpgScreen {
    
    // Layout constants
    private static final int GRID_COLS = 6;
    private static final int VISIBLE_ROWS = 6; // Show 6 rows at a time (more than before)
    private static final int ITEM_SIZE = 24;
    private static final int ITEM_SPACING = 4;
    
    // Categories for organizing items
    public enum ShopCategory {
        ALL("All", 0xFFFFD700),
        TOOLS("Tools", 0xFF888888),
        WEAPONS("Weapons", 0xFFFF5555),
        ARMOR("Armor", 0xFF5555FF),
        FOOD("Food", 0xFF55FF55),
        BUILDING("Building", 0xFFAA8844),
        FARMING("Farming", 0xFF44AA44),
        ORES("Ores", 0xFFAAFFFF),
        MISC("Misc", 0xFFAAAAAA);
        
        public final String name;
        public final int color;
        
        ShopCategory(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }
    
    private final boolean isBlackMarket;
    private int scrollOffset = 0; // Scroll offset in rows
    private ShopCategory currentCategory = ShopCategory.ALL;
    private List<Item> shopItems;
    private List<Item> filteredItems;
    
    // Grid positioning
    private int gridStartX;
    private int gridStartY;
    
    // Hovered item
    private Item hoveredItem = null;
    private int hoveredSlot = -1;
    
    // Button references
    private Button backButton;
    private List<Button> itemButtons = new ArrayList<>();
    private List<Button> categoryButtons = new ArrayList<>();
    
    // Category item mappings
    private static final Map<Item, ShopCategory> ITEM_CATEGORIES = new HashMap<>();
    
    static {
        // Tools
        for (Item item : List.of(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE,
                Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE,
                Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL,
                Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE,
                Items.FISHING_ROD, Items.SHEARS, Items.FLINT_AND_STEEL, Items.COMPASS, Items.CLOCK, Items.SPYGLASS,
                Items.BRUSH, Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET)) {
            ITEM_CATEGORIES.put(item, ShopCategory.TOOLS);
        }
        
        // Weapons
        for (Item item : List.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
                Items.BOW, Items.CROSSBOW, Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW, Items.TRIDENT, Items.MACE,
                Items.SHIELD)) {
            ITEM_CATEGORIES.put(item, ShopCategory.WEAPONS);
        }
        
        // Armor
        for (Item item : List.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
                Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
                Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
                Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
                Items.TURTLE_HELMET, Items.ELYTRA)) {
            ITEM_CATEGORIES.put(item, ShopCategory.ARMOR);
        }
        
        // Food
        for (Item item : List.of(Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.BREAD, Items.COOKED_BEEF, Items.COOKED_PORKCHOP,
                Items.COOKED_CHICKEN, Items.COOKED_MUTTON, Items.COOKED_SALMON, Items.COOKED_COD, Items.COOKED_RABBIT,
                Items.BAKED_POTATO, Items.PUMPKIN_PIE, Items.COOKIE, Items.CAKE, Items.MELON_SLICE, Items.CARROT,
                Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.HONEY_BOTTLE, Items.MILK_BUCKET, Items.MUSHROOM_STEW,
                Items.RABBIT_STEW, Items.SUSPICIOUS_STEW, Items.BEETROOT_SOUP)) {
            ITEM_CATEGORIES.put(item, ShopCategory.FOOD);
        }
        
        // Building
        for (Item item : List.of(Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS, Items.JUNGLE_PLANKS, Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS,
                Items.COBBLESTONE, Items.STONE, Items.STONE_BRICKS, Items.BRICKS, Items.DEEPSLATE_BRICKS,
                Items.GLASS, Items.GLASS_PANE, Items.WHITE_WOOL, Items.WHITE_CONCRETE, Items.TERRACOTTA,
                Items.SANDSTONE, Items.RED_SANDSTONE, Items.QUARTZ_BLOCK, Items.PRISMARINE, Items.SEA_LANTERN,
                Items.GLOWSTONE, Items.TORCH, Items.LANTERN, Items.OAK_DOOR, Items.IRON_DOOR, Items.LADDER,
                Items.CHEST, Items.BARREL, Items.CRAFTING_TABLE, Items.FURNACE, Items.BLAST_FURNACE, Items.SMOKER,
                Items.ANVIL, Items.GRINDSTONE, Items.SMITHING_TABLE, Items.LECTERN, Items.BOOKSHELF,
                Items.ENCHANTING_TABLE, Items.BREWING_STAND, Items.CAULDRON, Items.RED_BED)) {
            ITEM_CATEGORIES.put(item, ShopCategory.BUILDING);
        }
        
        // Farming
        for (Item item : List.of(Items.WHEAT_SEEDS, Items.WHEAT, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.BEETROOT_SEEDS,
                Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.SUGAR_CANE, Items.BAMBOO, Items.CACTUS,
                Items.BONE_MEAL, Items.LEAD, Items.NAME_TAG, Items.SADDLE, Items.EGG,
                Items.HAY_BLOCK, Items.COMPOSTER)) {
            ITEM_CATEGORIES.put(item, ShopCategory.FARMING);
        }
        
        // Ores and Materials
        for (Item item : List.of(Items.COAL, Items.RAW_IRON, Items.RAW_GOLD, Items.RAW_COPPER, Items.IRON_INGOT, Items.GOLD_INGOT, Items.COPPER_INGOT,
                Items.DIAMOND, Items.EMERALD, Items.LAPIS_LAZULI, Items.REDSTONE, Items.QUARTZ,
                Items.NETHERITE_SCRAP, Items.NETHERITE_INGOT, Items.AMETHYST_SHARD,
                Items.IRON_BLOCK, Items.GOLD_BLOCK, Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK, Items.LAPIS_BLOCK,
                Items.REDSTONE_BLOCK, Items.COPPER_BLOCK, Items.NETHERITE_BLOCK, Items.COAL_BLOCK,
                Items.BLAZE_ROD, Items.BLAZE_POWDER, Items.ENDER_PEARL, Items.ENDER_EYE, Items.GHAST_TEAR,
                Items.MAGMA_CREAM, Items.NETHER_STAR, Items.DRAGON_BREATH, Items.GUNPOWDER, Items.SLIME_BALL)) {
            ITEM_CATEGORIES.put(item, ShopCategory.ORES);
        }
        
        // Misc items (anything else)
        for (Item item : List.of(Items.BOOK, Items.PAPER, Items.INK_SAC, Items.GLOW_INK_SAC, Items.BONE, Items.STRING,
                Items.HONEYCOMB, Items.LEATHER, Items.RABBIT_HIDE, Items.FEATHER, Items.EXPERIENCE_BOTTLE,
                Items.FIREWORK_ROCKET, Items.FIREWORK_STAR, Items.SHULKER_SHELL, Items.SHULKER_BOX,
                Items.TOTEM_OF_UNDYING, Items.PAINTING, Items.ITEM_FRAME, Items.GLOW_ITEM_FRAME, Items.ARMOR_STAND,
                Items.BELL, Items.IRON_BARS, Items.LIGHTNING_ROD, Items.RAIL, Items.POWERED_RAIL, Items.MINECART,
                Items.CHEST_MINECART, Items.OAK_BOAT, Items.CHICKEN_SPAWN_EGG, Items.COW_SPAWN_EGG,
                Items.PIG_SPAWN_EGG, Items.SHEEP_SPAWN_EGG, Items.WOLF_SPAWN_EGG, Items.HORSE_SPAWN_EGG,
                Items.VILLAGER_SPAWN_EGG, Items.LEVER, Items.REDSTONE_TORCH, Items.REPEATER, Items.COMPARATOR,
                Items.PISTON, Items.STICKY_PISTON, Items.OBSERVER, Items.HOPPER, Items.DROPPER, Items.DISPENSER)) {
            ITEM_CATEGORIES.put(item, ShopCategory.MISC);
        }
    }
    
    public ShopScreen(boolean blackMarket) {
        super(Component.literal(blackMarket ? "§5§l✦ Black Market ✦" : "§6✦ Shop ✦"));
        this.isBlackMarket = blackMarket;
        loadShopItems();
        applyFilter();
    }
    
    private void loadShopItems() {
        shopItems = new ArrayList<>();
        Map<Item, ItemPricing.PriceData> prices = ItemPricing.getAllPrices();
        
        if (isBlackMarket) {
            // Black market sells special/rare items for XP levels
            shopItems.add(Items.DIAMOND);
            shopItems.add(Items.EMERALD);
            shopItems.add(Items.NETHERITE_INGOT);
            shopItems.add(Items.ENCHANTED_GOLDEN_APPLE);
            shopItems.add(Items.TOTEM_OF_UNDYING);
            shopItems.add(Items.ELYTRA);
            shopItems.add(Items.NETHER_STAR);
            shopItems.add(Items.DRAGON_EGG);
        } else {
            // Regular shop items
            shopItems.addAll(prices.keySet());
        }
    }
    
    private void applyFilter() {
        if (currentCategory == ShopCategory.ALL || isBlackMarket) {
            filteredItems = new ArrayList<>(shopItems);
        } else {
            filteredItems = new ArrayList<>();
            for (Item item : shopItems) {
                ShopCategory cat = ITEM_CATEGORIES.getOrDefault(item, ShopCategory.MISC);
                if (cat == currentCategory) {
                    filteredItems.add(item);
                }
            }
        }
        // Sort items within the filter
        sortItemsLogically();
        scrollOffset = 0; // Reset scroll when changing category
    }
    
    /**
     * Sort items logically within their category
     * Groups similar items together (all pickaxes, then all axes, etc.)
     */
    private void sortItemsLogically() {
        // Define item order priorities for logical grouping
        Map<Item, Integer> itemOrder = new HashMap<>();
        int order = 0;
        
        // Tools - by type, then by tier
        // Pickaxes
        for (Item item : List.of(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, 
                Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE)) {
            itemOrder.put(item, order++);
        }
        // Axes
        for (Item item : List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, 
                Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE)) {
            itemOrder.put(item, order++);
        }
        // Shovels
        for (Item item : List.of(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, 
                Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL)) {
            itemOrder.put(item, order++);
        }
        // Hoes
        for (Item item : List.of(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, 
                Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE)) {
            itemOrder.put(item, order++);
        }
        // Misc tools
        for (Item item : List.of(Items.FISHING_ROD, Items.SHEARS, Items.FLINT_AND_STEEL, 
                Items.COMPASS, Items.CLOCK, Items.SPYGLASS, Items.BRUSH,
                Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET)) {
            itemOrder.put(item, order++);
        }
        
        // Weapons - Swords by tier, then ranged
        for (Item item : List.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, 
                Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD)) {
            itemOrder.put(item, order++);
        }
        for (Item item : List.of(Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.MACE, Items.SHIELD,
                Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW)) {
            itemOrder.put(item, order++);
        }
        
        // Armor - by tier, then by slot
        // Leather
        for (Item item : List.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, 
                Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS)) {
            itemOrder.put(item, order++);
        }
        // Chainmail
        for (Item item : List.of(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, 
                Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS)) {
            itemOrder.put(item, order++);
        }
        // Iron
        for (Item item : List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, 
                Items.IRON_LEGGINGS, Items.IRON_BOOTS)) {
            itemOrder.put(item, order++);
        }
        // Golden
        for (Item item : List.of(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, 
                Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS)) {
            itemOrder.put(item, order++);
        }
        // Diamond
        for (Item item : List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, 
                Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)) {
            itemOrder.put(item, order++);
        }
        // Netherite
        for (Item item : List.of(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, 
                Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)) {
            itemOrder.put(item, order++);
        }
        // Special armor
        for (Item item : List.of(Items.TURTLE_HELMET, Items.ELYTRA)) {
            itemOrder.put(item, order++);
        }
        
        // Food - cooked meats, then other food
        for (Item item : List.of(Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN,
                Items.COOKED_MUTTON, Items.COOKED_RABBIT, Items.COOKED_SALMON, Items.COOKED_COD,
                Items.BREAD, Items.BAKED_POTATO, Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE,
                Items.CARROT, Items.GOLDEN_CARROT, Items.MELON_SLICE, Items.SWEET_BERRIES, Items.GLOW_BERRIES,
                Items.COOKIE, Items.PUMPKIN_PIE, Items.CAKE, Items.HONEY_BOTTLE, Items.MILK_BUCKET,
                Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.SUSPICIOUS_STEW, Items.BEETROOT_SOUP)) {
            itemOrder.put(item, order++);
        }
        
        // Ores - raw, then ingots, then blocks
        for (Item item : List.of(Items.COAL, Items.RAW_COPPER, Items.COPPER_INGOT, Items.COPPER_BLOCK,
                Items.RAW_IRON, Items.IRON_INGOT, Items.IRON_BLOCK,
                Items.RAW_GOLD, Items.GOLD_INGOT, Items.GOLD_BLOCK,
                Items.DIAMOND, Items.DIAMOND_BLOCK,
                Items.EMERALD, Items.EMERALD_BLOCK,
                Items.LAPIS_LAZULI, Items.LAPIS_BLOCK,
                Items.REDSTONE, Items.REDSTONE_BLOCK,
                Items.QUARTZ, Items.AMETHYST_SHARD,
                Items.NETHERITE_SCRAP, Items.NETHERITE_INGOT, Items.NETHERITE_BLOCK, Items.COAL_BLOCK)) {
            itemOrder.put(item, order++);
        }
        // Mob drops
        for (Item item : List.of(Items.BLAZE_ROD, Items.BLAZE_POWDER, Items.ENDER_PEARL, Items.ENDER_EYE,
                Items.GHAST_TEAR, Items.MAGMA_CREAM, Items.GUNPOWDER, Items.SLIME_BALL,
                Items.NETHER_STAR, Items.DRAGON_BREATH)) {
            itemOrder.put(item, order++);
        }
        
        // Sort items
        filteredItems.sort((a, b) -> {
            int orderA = itemOrder.getOrDefault(a, 9999);
            int orderB = itemOrder.getOrDefault(b, 9999);
            if (orderA != orderB) {
                return Integer.compare(orderA, orderB);
            }
            // Fall back to alphabetical by item name
            return a.getDescriptionId().compareTo(b.getDescriptionId());
        });
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Wider and taller window for scrollable shop
        windowWidth = Math.min(450, width - 20);
        windowHeight = Math.min(420, height - 20);
        windowX = (width - windowWidth) / 2;
        windowY = (height - windowHeight) / 2;
        
        // Calculate grid position (shifted down for category tabs and balance)
        int gridWidth = GRID_COLS * (ITEM_SIZE + ITEM_SPACING) - ITEM_SPACING;
        gridStartX = windowX + (windowWidth - gridWidth) / 2;
        gridStartY = windowY + 90; // Space for categories and balance
        
        // Add buttons
        addButtons();
    }
    
    private void addButtons() {
        // Clear old buttons
        itemButtons.clear();
        categoryButtons.clear();
        clearWidgets();
        
        // Back button
        backButton = addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            Minecraft.getInstance().setScreen(new HubScreen());
        }).bounds(windowX + 8, windowY + 42, 50, 16).build());
        
        // Category tabs (only for non-black market)
        if (!isBlackMarket) {
            int tabY = windowY + 64;
            int tabWidth = 38;
            int tabSpacing = 2;
            int totalTabsWidth = ShopCategory.values().length * (tabWidth + tabSpacing) - tabSpacing;
            int tabX = windowX + (windowWidth - totalTabsWidth) / 2; // Center categories
            
            for (ShopCategory cat : ShopCategory.values()) {
                final ShopCategory category = cat;
                Button catBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                    playClickSound();
                    currentCategory = category;
                    applyFilter();
                    refreshScreen();
                }).bounds(tabX, tabY, tabWidth, 14).build());
                categoryButtons.add(catBtn);
                tabX += tabWidth + tabSpacing;
            }
        }
        
        // Scroll buttons on the right side
        int scrollX = windowX + windowWidth - 30;
        int scrollUpY = gridStartY;
        int scrollDownY = gridStartY + (VISIBLE_ROWS * (ITEM_SIZE + ITEM_SPACING)) - 20;
        
        // Scroll up button
        addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            if (scrollOffset > 0) {
                scrollOffset--;
                refreshScreen();
            }
        }).bounds(scrollX, scrollUpY, 20, 16).build());
        
        // Scroll down button
        addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            int maxRows = (int) Math.ceil((double) filteredItems.size() / GRID_COLS);
            if (scrollOffset < maxRows - VISIBLE_ROWS) {
                scrollOffset++;
                refreshScreen();
            }
        }).bounds(scrollX, scrollDownY, 20, 16).build());
        
        // Item grid buttons
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = (scrollOffset + row) * GRID_COLS + col;
                if (index >= filteredItems.size()) break;
                
                Item item = filteredItems.get(index);
                int slotX = gridStartX + col * (ITEM_SIZE + ITEM_SPACING);
                int slotY = gridStartY + row * (ITEM_SIZE + ITEM_SPACING);
                
                Button itemBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                    playClickSound();
                    // Left click = buy
                    int amount = isShiftDown() ? (isBlackMarket ? 10 : 64) : 1;
                    sendBuyCommand(item, amount);
                }).bounds(slotX, slotY, ITEM_SIZE, ITEM_SIZE).build());
                itemButtons.add(itemBtn);
            }
        }
    }
    
    /**
     * Refresh screen when page or category changes
     */
    private void refreshScreen() {
        init();
    }
    
    // Note: No @Override - method signature changed in 1.21.11
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Right-click to sell
        if (button == 1 && hoveredItem != null && !isBlackMarket) {
            playClickSound();
            int amount = isShiftDown() ? 64 : 1;
            sendSellCommand(hoveredItem, amount);
            return true;
        }
        // Let buttons handle click
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Scroll the shop list
        int maxRows = (int) Math.ceil((double) filteredItems.size() / GRID_COLS);
        int maxScroll = Math.max(0, maxRows - VISIBLE_ROWS);
        
        if (verticalAmount > 0) {
            // Scroll up
            if (scrollOffset > 0) {
                scrollOffset--;
                refreshScreen();
                return true;
            }
        } else if (verticalAmount < 0) {
            // Scroll down
            if (scrollOffset < maxScroll) {
                scrollOffset++;
                refreshScreen();
                return true;
            }
        }
        
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        hoveredItem = null;
        hoveredSlot = -1;
        
        // Draw balance at top (moved down to avoid title overlap)
        long balance = HudRenderer.getCachedMoney();
        String balanceText = "§6Balance: §e$" + formatMoney(balance);
        drawCenteredText(graphics, balanceText, windowX + windowWidth / 2, windowY + 28, COLOR_GOLD_TEXT);
        
        // Draw back button
        int backBtnX = windowX + 8;
        int backBtnY = windowY + 42;
        boolean backHovered = isMouseOver(mouseX, mouseY, backBtnX, backBtnY, 50, 16);
        drawStyledButton(graphics, backBtnX, backBtnY, 50, 16, "< Back", backHovered);
        
        // Draw category tabs (only for non-black market)
        if (!isBlackMarket) {
            int tabWidth = 38;
            int tabSpacing = 2;
            int totalTabsWidth = ShopCategory.values().length * (tabWidth + tabSpacing) - tabSpacing;
            int tabX = windowX + (windowWidth - totalTabsWidth) / 2; // Center categories
            int tabY = windowY + 64;
            
            for (ShopCategory cat : ShopCategory.values()) {
                boolean isSelected = (cat == currentCategory);
                boolean isHovered = isMouseOver(mouseX, mouseY, tabX, tabY, tabWidth, 14);
                
                // Tab background
                int bgColor = isSelected ? 0xCC444444 : (isHovered ? 0xCC333333 : 0xCC222222);
                graphics.fill(tabX, tabY, tabX + tabWidth, tabY + 14, bgColor);
                
                // Tab border (highlight if selected)
                int borderColor = isSelected ? cat.color : 0xFF555555;
                drawBorder(graphics, tabX, tabY, tabWidth, 14, borderColor);
                
                // Tab text
                int textColor = isSelected ? cat.color : (isHovered ? 0xFFFFFFFF : 0xFFAAAAAA);
                int textWidth = font.width(cat.name);
                graphics.drawString(font, cat.name, tabX + (tabWidth - textWidth) / 2, tabY + 3, textColor, true);
                
                tabX += tabWidth + tabSpacing;
            }
        }
        
        // Draw scroll indicators and buttons
        int scrollX = windowX + windowWidth - 30;
        int scrollUpY = gridStartY;
        int scrollDownY = gridStartY + (VISIBLE_ROWS * (ITEM_SIZE + ITEM_SPACING)) - 20;
        int maxRows = (int) Math.ceil((double) filteredItems.size() / GRID_COLS);
        
        boolean canScrollUp = scrollOffset > 0;
        boolean canScrollDown = scrollOffset < maxRows - VISIBLE_ROWS;
        boolean upHovered = isMouseOver(mouseX, mouseY, scrollX, scrollUpY, 20, 16);
        boolean downHovered = isMouseOver(mouseX, mouseY, scrollX, scrollDownY, 20, 16);
        
        // Scroll up button
        if (canScrollUp || upHovered) {
            graphics.fill(scrollX, scrollUpY, scrollX + 20, scrollUpY + 16, 
                upHovered && canScrollUp ? COLOR_BUTTON_HOVER : (canScrollUp ? COLOR_BUTTON_BG : 0x40000000));
            drawBorder(graphics, scrollX, scrollUpY, 20, 16, canScrollUp ? COLOR_GOLD_BORDER : 0x40888888);
            drawCenteredText(graphics, "▲", scrollX + 10, scrollUpY + 4, canScrollUp ? COLOR_WHITE_TEXT : 0x60FFFFFF);
        }
        
        // Scroll down button
        if (canScrollDown || downHovered) {
            graphics.fill(scrollX, scrollDownY, scrollX + 20, scrollDownY + 16, 
                downHovered && canScrollDown ? COLOR_BUTTON_HOVER : (canScrollDown ? COLOR_BUTTON_BG : 0x40000000));
            drawBorder(graphics, scrollX, scrollDownY, 20, 16, canScrollDown ? COLOR_GOLD_BORDER : 0x40888888);
            drawCenteredText(graphics, "▼", scrollX + 10, scrollDownY + 4, canScrollDown ? COLOR_WHITE_TEXT : 0x60FFFFFF);
        }
        
        // Scroll position indicator
        int totalItems = filteredItems.size();
        int firstVisible = scrollOffset * GRID_COLS + 1;
        int lastVisible = Math.min((scrollOffset + VISIBLE_ROWS) * GRID_COLS, totalItems);
        String scrollText = "§7(" + firstVisible + "-" + lastVisible + " of " + totalItems + ")";
        drawCenteredText(graphics, scrollText, windowX + windowWidth / 2, windowY + windowHeight - 20, COLOR_GRAY_TEXT);
        
        // Draw item grid
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = (scrollOffset + row) * GRID_COLS + col;
                if (index >= filteredItems.size()) break;
                
                Item item = filteredItems.get(index);
                int slotX = gridStartX + col * (ITEM_SIZE + ITEM_SPACING);
                int slotY = gridStartY + row * (ITEM_SIZE + ITEM_SPACING);
                
                boolean isHovered = isMouseOver(mouseX, mouseY, slotX, slotY, ITEM_SIZE, ITEM_SIZE);
                
                // Slot background
                graphics.fill(slotX, slotY, slotX + ITEM_SIZE, slotY + ITEM_SIZE, 
                    isHovered ? 0x60FFFFFF : 0x40000000);
                
                // Border
                drawBorder(graphics, slotX, slotY, ITEM_SIZE, ITEM_SIZE, 
                    isHovered ? COLOR_GOLD_BORDER : 0x80888888);
                
                // Draw item
                ItemStack stack = new ItemStack(item);
                graphics.renderItem(stack, slotX + 4, slotY + 4);
                
                if (isHovered) {
                    hoveredItem = item;
                    hoveredSlot = index;
                }
            }
        }
        
        // Draw tooltip for hovered item
        if (hoveredItem != null) {
            drawItemTooltip(graphics, mouseX, mouseY);
        }
        
        // Draw footer with instructions
        String footerText;
        if (isBlackMarket) {
            footerText = "§5Click = Buy with XP Levels | Shift+Click = Buy 10";
        } else {
            footerText = "§aLeft-Click = Buy §7| §cRight-Click = Sell §7| §eShift = x64";
        }
        drawCenteredText(graphics, footerText, windowX + windowWidth / 2, windowY + windowHeight - 20, COLOR_GRAY_TEXT);
        
        // Black Market button (only visible when shift held in normal shop)
        if (!isBlackMarket && isShiftDown()) {
            int bmBtnX = windowX + windowWidth - 100;
            int bmBtnY = windowY + windowHeight - 38;
            boolean bmHovered = isMouseOver(mouseX, mouseY, bmBtnX, bmBtnY, 90, 16);
            
            graphics.fill(bmBtnX, bmBtnY, bmBtnX + 90, bmBtnY + 16, 
                bmHovered ? 0xCC660000 : 0xCC440000);
            drawBorder(graphics, bmBtnX, bmBtnY, 90, 16, 0xFFAA0000);
            drawCenteredText(graphics, "§c⚠ Illegal", bmBtnX + 45, bmBtnY + 4, 0xFFFF5555);
        }
    }
    
    private void drawStyledButton(GuiGraphics graphics, int x, int y, int w, int h, String label, boolean hovered) {
        int bgColor = hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON_BG;
        graphics.fill(x, y, x + w, y + h, bgColor);
        drawBorder(graphics, x, y, w, h, COLOR_GOLD_BORDER);
        int textWidth = font.width(label);
        int textX = x + (w - textWidth) / 2;
        int textY = y + (h - 8) / 2;
        graphics.drawString(font, label, textX, textY, hovered ? COLOR_GOLD_TEXT : COLOR_WHITE_TEXT, true);
    }
    
    private void drawItemTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredItem == null) return;
        
        ItemPricing.PriceData priceData = ItemPricing.getPrice(hoveredItem);
        String itemName = hoveredItem.getName(hoveredItem.getDefaultInstance()).getString();
        
        List<String> lines = new ArrayList<>();
        lines.add("§f" + itemName);
        
        if (isBlackMarket) {
            int levelCost = getBlackMarketCost(hoveredItem);
            lines.add("§5Cost: §d" + levelCost + " XP Levels");
        } else if (priceData != null) {
            lines.add("§aLeft-Click Buy: §6$" + formatMoney(priceData.buyPrice()));
            lines.add("§cRight-Click Sell: §6$" + formatMoney(priceData.sellPrice()));
            if (isShiftDown()) {
                lines.add("§7(Shift: Buy/Sell x64)");
            }
        }
        
        // Calculate tooltip dimensions
        int tooltipWidth = 0;
        for (String line : lines) {
            tooltipWidth = Math.max(tooltipWidth, font.width(line));
        }
        tooltipWidth += 8;
        int tooltipHeight = lines.size() * 10 + 6;
        
        // Position tooltip
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;
        
        // Keep on screen
        if (tooltipX + tooltipWidth > width) tooltipX = mouseX - tooltipWidth - 4;
        if (tooltipY + tooltipHeight > height) tooltipY = height - tooltipHeight;
        if (tooltipY < 0) tooltipY = 0;
        
        // Draw tooltip background
        graphics.fill(tooltipX - 2, tooltipY - 2, tooltipX + tooltipWidth + 2, tooltipY + tooltipHeight + 2, 0xF0100010);
        drawBorder(graphics, tooltipX - 2, tooltipY - 2, tooltipWidth + 4, tooltipHeight + 4, 0xFF5000AA);
        
        // Draw text
        int y = tooltipY;
        for (String line : lines) {
            graphics.drawString(font, line, tooltipX, y, 0xFFFFFFFF, true);
            y += 10;
        }
    }
    
    private int getBlackMarketCost(Item item) {
        if (item == Items.DIAMOND) return 5;
        if (item == Items.EMERALD) return 3;
        if (item == Items.NETHERITE_INGOT) return 20;
        if (item == Items.ENCHANTED_GOLDEN_APPLE) return 30;
        if (item == Items.TOTEM_OF_UNDYING) return 50;
        if (item == Items.ELYTRA) return 100;
        if (item == Items.NETHER_STAR) return 75;
        if (item == Items.DRAGON_EGG) return 200;
        return 10;
    }
    
    private String formatMoney(long amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000.0);
        }
        return String.valueOf(amount);
    }
    
    private void sendBuyCommand(Item item, int amount) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        String itemId = BuiltInRegistries.ITEM.getKey(item).getPath();
        
        if (isBlackMarket) {
            mc.player.connection.sendCommand("rpgadmin blackmarket " + itemId + " " + amount);
        } else {
            mc.player.connection.sendCommand("buy " + itemId + " " + amount);
        }
    }
    
    private void sendSellCommand(Item item, int amount) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        String itemId = BuiltInRegistries.ITEM.getKey(item).getPath();
        mc.player.connection.sendCommand("sell " + itemId + " " + amount);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Background with color based on market type
        if (isBlackMarket) {
            graphics.fill(0, 0, width, height, 0xB0200010);
        } else {
            graphics.fill(0, 0, width, height, 0xA0000000);
        }
        
        // Draw window with different colors for black market
        if (isBlackMarket) {
            drawGradient(graphics, windowX, windowY, windowWidth, windowHeight, 0xE6100010, 0xE6200820);
            drawBorder(graphics, windowX, windowY, windowWidth, windowHeight, 0xFFAA0000);
        } else {
            drawWindowBackground(graphics, windowX, windowY, windowWidth, windowHeight);
        }
        
        // Draw title
        String title = isBlackMarket ? "✦ Black Market ✦" : "✦ Shop ✦";
        int titleColor = isBlackMarket ? 0xFFAA00AA : COLOR_GOLD_TEXT;
        int textWidth = font.width(title);
        graphics.drawString(font, title, windowX + windowWidth / 2 - textWidth / 2, windowY + 4, titleColor, true);
        
        // Render vanilla widgets
        super.render(graphics, mouseX, mouseY, delta);
        
        // Render our content on top
        renderContent(graphics, mouseX, mouseY, delta);
    }
}
