package com.vanillaplus.rpg.gui;

import com.vanillaplus.rpg.economy.ItemPricing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shop Screen - Catalog style shop with procedural rendering
 * Features:
 * - Grid layout of items from pricing data
 * - Hover highlight with white box
 * - Click to buy 1, Shift+Click to buy stack
 * - Hidden Black Market button when holding Shift
 * 
 * Uses Button widgets for click handling, custom rendering on top
 */
public class ShopScreen extends BaseRpgScreen {
    
    private static final int GRID_COLS = 6;
    private static final int GRID_ROWS = 4;
    private static final int ITEM_SIZE = 24;
    private static final int ITEM_SPACING = 4;
    
    private final boolean isBlackMarket;
    private int currentPage = 0;
    private List<Item> shopItems;
    
    // Grid positioning
    private int gridStartX;
    private int gridStartY;
    
    // Hovered item
    private Item hoveredItem = null;
    private int hoveredSlot = -1;
    
    // Button references
    private Button backButton;
    private Button prevButton;
    private Button nextButton;
    private Button blackMarketButton;
    private List<Button> itemButtons = new ArrayList<>();
    
    public ShopScreen(boolean blackMarket) {
        super(Component.literal(blackMarket ? "§5§l✦ Black Market ✦" : "§6✦ Shop ✦"));
        this.isBlackMarket = blackMarket;
        loadShopItems();
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
    
    @Override
    protected void init() {
        super.init();
        
        // Larger window for shop
        windowWidth = Math.min(320, width - 20);
        windowHeight = Math.min(280, height - 20);
        windowX = (width - windowWidth) / 2;
        windowY = (height - windowHeight) / 2;
        
        // Calculate grid position
        int gridWidth = GRID_COLS * (ITEM_SIZE + ITEM_SPACING) - ITEM_SPACING;
        gridStartX = windowX + (windowWidth - gridWidth) / 2;
        gridStartY = windowY + 45;
        
        // Add buttons
        addButtons();
    }
    
    private void addButtons() {
        // Clear old buttons
        itemButtons.clear();
        clearWidgets();
        
        // Back button
        backButton = addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            Minecraft.getInstance().setScreen(new HubScreen());
        }).bounds(windowX + 8, windowY + 8, 50, 16).build());
        
        // Previous page button
        int totalPages = (int) Math.ceil((double) shopItems.size() / (GRID_COLS * GRID_ROWS));
        if (currentPage > 0) {
            prevButton = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                playClickSound();
                currentPage--;
                rebuildWidgets();
            }).bounds(windowX + 10, windowY + 25, 20, 16).build());
        }
        
        // Next page button
        if (currentPage < totalPages - 1) {
            nextButton = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                playClickSound();
                currentPage++;
                rebuildWidgets();
            }).bounds(windowX + windowWidth - 30, windowY + 25, 20, 16).build());
        }
        
        // Black market button - only add if shift is held (checked on rebuild)
        // Note: We don't add a permanent button widget since it would show when not needed
        
        // Item grid buttons
        int startIndex = currentPage * GRID_COLS * GRID_ROWS;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = startIndex + row * GRID_COLS + col;
                if (index >= shopItems.size()) break;
                
                Item item = shopItems.get(index);
                int slotX = gridStartX + col * (ITEM_SIZE + ITEM_SPACING);
                int slotY = gridStartY + row * (ITEM_SIZE + ITEM_SPACING);
                
                Button itemBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                    playClickSound();
                    int amount = isShiftDown() ? (isBlackMarket ? 10 : 64) : 1;
                    sendBuyCommand(item, amount);
                }).bounds(slotX, slotY, ITEM_SIZE, ITEM_SIZE).build());
                itemButtons.add(itemBtn);
            }
        }
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        hoveredItem = null;
        hoveredSlot = -1;
        
        // Draw back button (over vanilla button)
        int backBtnX = windowX + 8;
        int backBtnY = windowY + 8;
        boolean backHovered = isMouseOver(mouseX, mouseY, backBtnX, backBtnY, 50, 16);
        drawStyledButton(graphics, backBtnX, backBtnY, 50, 16, "< Back", backHovered);
        
        // Draw page navigation
        int totalPages = (int) Math.ceil((double) shopItems.size() / (GRID_COLS * GRID_ROWS));
        drawCenteredText(graphics, "Page " + (currentPage + 1) + "/" + totalPages, 
            windowX + windowWidth / 2, windowY + 28, COLOR_GRAY_TEXT);
        
        // Previous/Next arrows (cover vanilla buttons)
        if (currentPage > 0) {
            int prevX = windowX + 10;
            int prevY = windowY + 25;
            boolean prevHover = isMouseOver(mouseX, mouseY, prevX, prevY, 20, 16);
            graphics.fill(prevX, prevY, prevX + 20, prevY + 16, prevHover ? COLOR_BUTTON_HOVER : COLOR_BUTTON_BG);
            drawText(graphics, "<<", prevX + 2, prevY + 4, prevHover ? COLOR_GOLD_TEXT : COLOR_WHITE_TEXT);
        }
        if (currentPage < totalPages - 1) {
            int nextX = windowX + windowWidth - 30;
            int nextY = windowY + 25;
            boolean nextHover = isMouseOver(mouseX, mouseY, nextX, nextY, 20, 16);
            graphics.fill(nextX, nextY, nextX + 20, nextY + 16, nextHover ? COLOR_BUTTON_HOVER : COLOR_BUTTON_BG);
            drawText(graphics, ">>", nextX + 2, nextY + 4, nextHover ? COLOR_GOLD_TEXT : COLOR_WHITE_TEXT);
        }
        
        // Draw item grid (over vanilla buttons)
        int startIndex = currentPage * GRID_COLS * GRID_ROWS;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = startIndex + row * GRID_COLS + col;
                if (index >= shopItems.size()) break;
                
                Item item = shopItems.get(index);
                int slotX = gridStartX + col * (ITEM_SIZE + ITEM_SPACING);
                int slotY = gridStartY + row * (ITEM_SIZE + ITEM_SPACING);
                
                boolean isHovered = isMouseOver(mouseX, mouseY, slotX, slotY, ITEM_SIZE, ITEM_SIZE);
                
                // Draw slot background (covers vanilla button)
                graphics.fill(slotX, slotY, slotX + ITEM_SIZE, slotY + ITEM_SIZE, 
                    isHovered ? 0x60FFFFFF : 0x40000000);
                
                // Draw border
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
        
        // Draw footer
        String footerText = isBlackMarket 
            ? "§5Click = Buy with XP Levels | Shift+Click = Buy 10"
            : "§7Click = Buy 1 | Shift+Click = Buy 64";
        drawCenteredText(graphics, footerText, windowX + windowWidth / 2, windowY + windowHeight - 30, COLOR_GRAY_TEXT);
        
        // Black Market button (only visible when shift held in normal shop)
        if (!isBlackMarket && isShiftDown()) {
            int bmBtnX = windowX + windowWidth - 100;
            int bmBtnY = windowY + windowHeight - 22;
            boolean bmHovered = isMouseOver(mouseX, mouseY, bmBtnX, bmBtnY, 90, 16);
            
            // Red themed button (covers vanilla button)
            graphics.fill(bmBtnX, bmBtnY, bmBtnX + 90, bmBtnY + 16, 
                bmHovered ? 0xCC660000 : 0xCC440000);
            drawBorder(graphics, bmBtnX, bmBtnY, 90, 16, 0xFFAA0000);
            drawCenteredText(graphics, "§c⚠ Illegal", bmBtnX + 45, bmBtnY + 4, 0xFFFF5555);
        }
    }
    
    /**
     * Draw a styled button covering the vanilla button
     */
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
            lines.add("§aBuy: §6$" + formatMoney(priceData.buyPrice()));
            lines.add("§cSell: §6$" + formatMoney(priceData.sellPrice()));
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
        
        // 1.21.11: Use registry to get item path
        String itemId = BuiltInRegistries.ITEM.getKey(item).getPath();
        
        if (isBlackMarket) {
            // Send black market buy command
            mc.player.connection.sendCommand("rpgadmin blackmarket " + itemId + " " + amount);
        } else {
            // Send regular buy command
            mc.player.connection.sendCommand("buy " + itemId + " " + amount);
        }
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
        graphics.drawString(font, title, windowX + windowWidth / 2 - textWidth / 2, windowY + 10, titleColor, true);
        
        // Render the vanilla widgets (buttons) - they will render gray
        super.render(graphics, mouseX, mouseY, delta);
        
        // Now render our content ON TOP - this covers the gray buttons
        renderContent(graphics, mouseX, mouseY, delta);
    }
}
