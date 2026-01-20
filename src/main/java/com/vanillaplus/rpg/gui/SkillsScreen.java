package com.vanillaplus.rpg.gui;

import com.vanillaplus.rpg.client.HudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Skills Screen - Display player's RPG skills and progression
 * Uses Button widgets for click handling, custom rendering on top
 */
public class SkillsScreen extends BaseRpgScreen {
    
    public SkillsScreen() {
        super(Component.literal("§6✦ My Skills ✦"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        windowWidth = Math.min(260, width - 40);
        windowHeight = Math.min(200, height - 40);
        windowX = (width - windowWidth) / 2;
        windowY = (height - windowHeight) / 2;
        
        // Add back button
        addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            Minecraft.getInstance().setScreen(new HubScreen());
        }).bounds(windowX + 8, windowY + 8, 50, 16).build());
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draw back button (cover vanilla button)
        int backBtnX = windowX + 8;
        int backBtnY = windowY + 8;
        boolean backHovered = isMouseOver(mouseX, mouseY, backBtnX, backBtnY, 50, 16);
        drawStyledButton(graphics, backBtnX, backBtnY, 50, 16, "< Back", backHovered);
        
        int centerX = windowX + windowWidth / 2;
        int y = windowY + 45;
        
        // Get real data from server sync
        int level = HudRenderer.getCachedLevel();
        int xp = HudRenderer.getCachedXp();
        int xpRequired = HudRenderer.getCachedXpRequired();
        int xpPercent = HudRenderer.getXpProgressPercent();
        long money = HudRenderer.getCachedMoney();
        
        // Title
        drawCenteredText(graphics, "§e§lRPG Progress", centerX, y, COLOR_GOLD_TEXT);
        y += 25;
        
        // Level display
        drawCenteredText(graphics, "§6Level §f" + level, centerX, y, COLOR_WHITE_TEXT);
        y += 20;
        
        // XP Bar - main progress
        drawSkillBar(graphics, centerX - 80, y, "XP", xpPercent);
        y += 15;
        
        // XP numbers
        String xpText = "§7" + xp + " / " + xpRequired;
        drawCenteredText(graphics, xpText, centerX, y, COLOR_GRAY_TEXT);
        y += 25;
        
        // Money display
        String moneyText = "§aBalance: §6$" + formatMoney(money);
        drawCenteredText(graphics, moneyText, centerX, y, COLOR_GREEN_TEXT);
        
        // Footer
        drawCenteredText(graphics, "§7Earn XP by playing!", 
            centerX, windowY + windowHeight - 20, COLOR_GRAY_TEXT);
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
    
    private void drawSkillBar(GuiGraphics graphics, int x, int y, String name, int percent) {
        int barWidth = 160;
        int barHeight = 14;
        
        // Label
        drawText(graphics, "§f" + name, x, y, COLOR_WHITE_TEXT);
        
        // Bar background
        int barY = y + 2;
        graphics.fill(x + 30, barY, x + 30 + barWidth, barY + barHeight, 0xFF222222);
        
        // Progress fill
        int fillWidth = (int) (barWidth * percent / 100.0);
        int fillColor = percent >= 75 ? 0xFF44AA44 : (percent >= 50 ? 0xFFAAAA44 : 0xFFAA6644);
        graphics.fill(x + 30, barY, x + 30 + fillWidth, barY + barHeight, fillColor);
        
        // Border
        drawBorder(graphics, x + 30, barY, barWidth, barHeight, 0xFF666666);
        
        // Percent text
        String percentText = percent + "%";
        int textX = x + 30 + (barWidth - font.width(percentText)) / 2;
        graphics.drawString(font, percentText, textX, barY + 3, COLOR_WHITE_TEXT, true);
    }
    
    private String formatMoney(long amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000.0);
        }
        return String.format("%,d", amount);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Background
        graphics.fill(0, 0, width, height, 0xA0000000);
        
        // Draw window
        drawWindowBackground(graphics, windowX, windowY, windowWidth, windowHeight);
        
        // Draw title
        drawCenteredTitle(graphics, "✦ My Skills ✦", windowX + windowWidth / 2, windowY + 10);
        
        // Render vanilla widgets (buttons)
        super.render(graphics, mouseX, mouseY, delta);
        
        // Render our content ON TOP
        renderContent(graphics, mouseX, mouseY, delta);
    }
}
