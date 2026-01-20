package com.vanillaplus.rpg.gui;

import com.vanillaplus.rpg.economy.ItemPricing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Shipping Bin Screen - Sell items directly from inventory
 * Features:
 * - Shows items from player's hotbar (9 slots)
 * - Click to sell one, Shift+Click to sell stack
 * - "Sell All Sellable" button for mass selling
 * - Shows held item with quick sell option
 * 
 * Uses Button widgets for click handling, custom rendering on top
 */
public class ShippingBinScreen extends BaseRpgScreen {
    
    private static final int SLOT_SIZE = 28;
    private static final int SLOT_SPACING = 4;
    
    // Grid positioning
    private int gridStartX;
    private int gridStartY;
    private int hoveredSlot = -1;
    
    // Total sellable value
    private long totalSellableValue = 0;
    
    // Button positions
    private int sellAllBtnX;
    private int sellAllBtnY;
    private int sellHandBtnX;
    private int sellHandBtnY;
    
    public ShippingBinScreen() {
        super(Component.literal("§6✦ Shipping Bin ✦"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Calculate window size
        windowWidth = Math.min(300, width - 20);
        windowHeight = Math.min(250, height - 20);
        windowX = (width - windowWidth) / 2;
        windowY = (height - windowHeight) / 2;
        
        // Calculate grid position for 9-slot hotbar display
        int gridWidth = 9 * (SLOT_SIZE + SLOT_SPACING) - SLOT_SPACING;
        gridStartX = windowX + (windowWidth - gridWidth) / 2;
        gridStartY = windowY + 85;
        
        // Button positions
        sellHandBtnX = windowX + (windowWidth - 120) / 2;
        sellHandBtnY = windowY + 55;
        
        sellAllBtnX = windowX + (windowWidth - 140) / 2;
        sellAllBtnY = windowY + windowHeight - 50;
        
        // Add back button
        addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            Minecraft.getInstance().setScreen(new HubScreen());
        }).bounds(windowX + 8, windowY + 8, 50, 16).build());
        
        // Add "Sell Item in Hand" button
        addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            sellHeldItem(isShiftDown());
        }).bounds(sellHandBtnX, sellHandBtnY, 120, 20).build());
        
        // Add "Sell All Sellable" button
        addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            sellAllItems();
        }).bounds(sellAllBtnX, sellAllBtnY, 140, 24).build());
        
        // Add hotbar slot buttons
        for (int i = 0; i < 9; i++) {
            final int slot = i;
            int slotX = gridStartX + i * (SLOT_SIZE + SLOT_SPACING);
            addRenderableWidget(Button.builder(Component.empty(), btn -> {
                playClickSound();
                sellFromSlot(slot, isShiftDown());
            }).bounds(slotX, gridStartY, SLOT_SIZE, SLOT_SIZE).build());
        }
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        hoveredSlot = -1;
        totalSellableValue = 0;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        Inventory inventory = mc.player.getInventory();
        ItemStack heldItem = mc.player.getMainHandItem();
        
        // Draw back button
        int backBtnX = windowX + 8;
        int backBtnY = windowY + 8;
        boolean backHovered = isMouseOver(mouseX, mouseY, backBtnX, backBtnY, 50, 16);
        drawStyledButton(graphics, backBtnX, backBtnY, 50, 16, "< Back", backHovered);
        
        // Draw held item section
        drawCenteredText(graphics, "§7Held Item:", windowX + windowWidth / 2, windowY + 35, COLOR_GRAY_TEXT);
        
        // Draw "Sell Item in Hand" button
        boolean sellHandHovered = isMouseOver(mouseX, mouseY, sellHandBtnX, sellHandBtnY, 120, 20);
        if (!heldItem.isEmpty()) {
            ItemPricing.PriceData priceData = ItemPricing.getPrice(heldItem.getItem());
            if (priceData != null) {
                long value = priceData.sellPrice() * (isShiftDown() ? heldItem.getCount() : 1);
                String label = isShiftDown() ? "Sell All ($" + formatMoney(value) + ")" : "Sell 1 ($" + priceData.sellPrice() + ")";
                graphics.fill(sellHandBtnX, sellHandBtnY, sellHandBtnX + 120, sellHandBtnY + 20, 
                    sellHandHovered ? 0xCC228822 : 0xCC115511);
                drawBorder(graphics, sellHandBtnX, sellHandBtnY, 120, 20, 0xFF44AA44);
                drawCenteredText(graphics, "§a" + label, sellHandBtnX + 60, sellHandBtnY + 6, COLOR_GREEN_TEXT);
            } else {
                graphics.fill(sellHandBtnX, sellHandBtnY, sellHandBtnX + 120, sellHandBtnY + 20, 0xCC444444);
                drawBorder(graphics, sellHandBtnX, sellHandBtnY, 120, 20, 0xFF666666);
                drawCenteredText(graphics, "§8Can't Sell", sellHandBtnX + 60, sellHandBtnY + 6, 0xFF888888);
            }
        } else {
            graphics.fill(sellHandBtnX, sellHandBtnY, sellHandBtnX + 120, sellHandBtnY + 20, 0xCC333333);
            drawBorder(graphics, sellHandBtnX, sellHandBtnY, 120, 20, 0xFF555555);
            drawCenteredText(graphics, "§8No Item Held", sellHandBtnX + 60, sellHandBtnY + 6, 0xFF888888);
        }
        
        // Draw "Your Hotbar" label
        drawCenteredText(graphics, "§eYour Hotbar §7(Click to sell)", windowX + windowWidth / 2, gridStartY - 15, COLOR_GOLD_TEXT);
        
        // Draw hotbar slots
        for (int i = 0; i < 9; i++) {
            int slotX = gridStartX + i * (SLOT_SIZE + SLOT_SPACING);
            boolean isHovered = isMouseOver(mouseX, mouseY, slotX, gridStartY, SLOT_SIZE, SLOT_SIZE);
            
            // Slot background (hover highlight only)
            int bgColor = isHovered ? 0xFF404040 : 0xFF202020;
            graphics.fill(slotX, gridStartY, slotX + SLOT_SIZE, gridStartY + SLOT_SIZE, bgColor);
            
            // Slot border (hover highlight only)
            int borderColor = isHovered ? COLOR_GOLD_BORDER : 0xFF606060;
            drawBorder(graphics, slotX, gridStartY, SLOT_SIZE, SLOT_SIZE, borderColor);
            
            // Draw item
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, slotX + 6, gridStartY + 6);
                if (stack.getCount() > 1) {
                    graphics.drawString(font, String.valueOf(stack.getCount()), 
                        slotX + SLOT_SIZE - 8, gridStartY + SLOT_SIZE - 10, COLOR_WHITE_TEXT, true);
                }
                
                // Calculate total sellable value
                ItemPricing.PriceData priceData = ItemPricing.getPrice(stack.getItem());
                if (priceData != null) {
                    totalSellableValue += priceData.sellPrice() * stack.getCount();
                }
            }
            
            if (isHovered) {
                hoveredSlot = i;
            }
        }
        
        // Draw total value
        String valueText = "§6Total Sellable: §e$" + formatMoney(totalSellableValue);
        drawCenteredText(graphics, valueText, windowX + windowWidth / 2, gridStartY + SLOT_SIZE + 15, COLOR_GOLD_TEXT);
        
        // Draw "Sell All" button
        boolean sellAllHovered = isMouseOver(mouseX, mouseY, sellAllBtnX, sellAllBtnY, 140, 24);
        if (totalSellableValue > 0) {
            graphics.fill(sellAllBtnX, sellAllBtnY, sellAllBtnX + 140, sellAllBtnY + 24, 
                sellAllHovered ? 0xCC228822 : 0xCC115511);
            drawBorder(graphics, sellAllBtnX, sellAllBtnY, 140, 24, 0xFF44AA44);
            drawCenteredText(graphics, "§a§lSELL ALL HOTBAR", sellAllBtnX + 70, sellAllBtnY + 8, COLOR_GREEN_TEXT);
        } else {
            graphics.fill(sellAllBtnX, sellAllBtnY, sellAllBtnX + 140, sellAllBtnY + 24, 0xCC333333);
            drawBorder(graphics, sellAllBtnX, sellAllBtnY, 140, 24, 0xFF555555);
            drawCenteredText(graphics, "§8Nothing to Sell", sellAllBtnX + 70, sellAllBtnY + 8, 0xFF888888);
        }
        
        // Draw tooltip for hovered slot
        if (hoveredSlot >= 0) {
            ItemStack hoveredStack = inventory.getItem(hoveredSlot);
            if (!hoveredStack.isEmpty()) {
                drawSlotTooltip(graphics, mouseX, mouseY, hoveredStack);
            }
        }
        
        // Instructions
        drawCenteredText(graphics, "§7Click = Sell 1 | Shift+Click = Sell Stack", 
            windowX + windowWidth / 2, windowY + windowHeight - 18, COLOR_GRAY_TEXT);
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
    
    private void drawSlotTooltip(GuiGraphics graphics, int mouseX, int mouseY, ItemStack stack) {
        Item item = stack.getItem();
        String itemName = item.getName(item.getDefaultInstance()).getString();
        ItemPricing.PriceData priceData = ItemPricing.getPrice(item);
        
        List<String> lines = new ArrayList<>();
        lines.add("§f" + itemName);
        if (priceData != null) {
            lines.add("§6Sell Price: §e$" + priceData.sellPrice() + " each");
            if (stack.getCount() > 1) {
                long totalValue = priceData.sellPrice() * stack.getCount();
                lines.add("§6Stack Value: §e$" + formatMoney(totalValue));
            }
        } else {
            lines.add("§cCannot be sold");
        }
        
        // Draw tooltip
        int tooltipWidth = 0;
        for (String line : lines) {
            tooltipWidth = Math.max(tooltipWidth, font.width(line));
        }
        tooltipWidth += 8;
        int tooltipHeight = lines.size() * 10 + 6;
        
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;
        
        if (tooltipX + tooltipWidth > width) tooltipX = mouseX - tooltipWidth - 4;
        if (tooltipY < 0) tooltipY = 0;
        
        graphics.fill(tooltipX - 2, tooltipY - 2, tooltipX + tooltipWidth + 2, tooltipY + tooltipHeight + 2, 0xF0100010);
        drawBorder(graphics, tooltipX - 2, tooltipY - 2, tooltipWidth + 4, tooltipHeight + 4, 0xFF505000);
        
        int y = tooltipY;
        for (String line : lines) {
            graphics.drawString(font, line, tooltipX, y, 0xFFFFFFFF, true);
            y += 10;
        }
    }
    
    private String formatMoney(long amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000.0);
        }
        return String.format("%,d", amount);
    }
    
    private void sellHeldItem(boolean sellStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) return;
        
        String itemId = BuiltInRegistries.ITEM.getKey(held.getItem()).getPath();
        int amount = sellStack ? held.getCount() : 1;
        
        mc.player.connection.sendCommand("sell " + itemId + " " + amount);
        mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
    }
    
    private void sellFromSlot(int slot, boolean sellStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        ItemStack stack = mc.player.getInventory().getItem(slot);
        if (stack.isEmpty()) return;
        
        ItemPricing.PriceData priceData = ItemPricing.getPrice(stack.getItem());
        if (priceData == null) return; // Can't sell this item
        
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        int amount = sellStack ? stack.getCount() : 1;
        
        mc.player.connection.sendCommand("sell " + itemId + " " + amount);
        mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
    }
    
    private void sellAllItems() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        // Sell all items in hotbar
        mc.player.connection.sendCommand("sell all");
        mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Background
        graphics.fill(0, 0, width, height, 0xA0000000);
        
        // Draw window
        drawWindowBackground(graphics, windowX, windowY, windowWidth, windowHeight);
        
        // Draw title
        drawCenteredTitle(graphics, "✦ Shipping Bin ✦", windowX + windowWidth / 2, windowY + 10);
        
        // Render vanilla widgets (buttons)
        super.render(graphics, mouseX, mouseY, delta);
        
        // Render our content ON TOP
        renderContent(graphics, mouseX, mouseY, delta);
    }
}
