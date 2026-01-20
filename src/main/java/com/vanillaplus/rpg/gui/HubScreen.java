package com.vanillaplus.rpg.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Hub Dashboard - Main menu for the RPG mod
 * Features:
 * - Blur effect background
 * - Large rectangular buttons with custom styling
 * - Navigation to: Shop, Shipping Bin, My Skills
 * - Player stats display
 * 
 * SOLUTION: We use Minecraft Button widgets for click handling, but render
 * our custom visuals ON TOP in renderContent() which is called AFTER super.render()
 */
public class HubScreen extends BaseRpgScreen {
    
    // Button dimensions
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 28;
    private static final int BUTTON_SPACING = 8;
    
    // Button bounds for custom rendering
    private int shopBtnX, shopBtnY;
    private int binBtnX, binBtnY;
    private int skillsBtnX, skillsBtnY;
    
    public HubScreen() {
        super(Component.literal("RPG Hub"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Make window larger for hub
        windowWidth = Math.min(280, width - 40);
        windowHeight = Math.min(220, height - 40);
        windowX = (width - windowWidth) / 2;
        windowY = (height - windowHeight) / 2;
        
        // Calculate button positions
        int btnX = windowX + (windowWidth - BUTTON_WIDTH) / 2;
        int startY = windowY + 50;
        
        shopBtnX = btnX;
        shopBtnY = startY;
        
        binBtnX = btnX;
        binBtnY = startY + BUTTON_HEIGHT + BUTTON_SPACING;
        
        skillsBtnX = btnX;
        skillsBtnY = startY + (BUTTON_HEIGHT + BUTTON_SPACING) * 2;
        
        // Add real Minecraft buttons for click handling
        // The buttons have empty text - we draw our own text on top
        addRenderableWidget(Button.builder(Component.empty(), button -> {
            playClickSound();
            Minecraft.getInstance().setScreen(new ShopScreen(false));
        }).bounds(shopBtnX, shopBtnY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(Component.empty(), button -> {
            playClickSound();
            Minecraft.getInstance().setScreen(new ShippingBinScreen());
        }).bounds(binBtnX, binBtnY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(Component.empty(), button -> {
            playClickSound();
            Minecraft.getInstance().setScreen(new SkillsScreen());
        }).bounds(skillsBtnX, skillsBtnY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draw player stats section
        int statsY = windowY + 28;
        int centerX = windowX + windowWidth / 2;
        drawCenteredText(graphics, "§7Your Progress", centerX, statsY, COLOR_GRAY_TEXT);
        
        // Draw custom styled buttons ON TOP of the vanilla buttons
        // This covers the gray vanilla button rendering
        drawStyledButton(graphics, shopBtnX, shopBtnY, BUTTON_WIDTH, BUTTON_HEIGHT, 
            "[ The Shop ]", isMouseOver(mouseX, mouseY, shopBtnX, shopBtnY, BUTTON_WIDTH, BUTTON_HEIGHT));
        
        drawStyledButton(graphics, binBtnX, binBtnY, BUTTON_WIDTH, BUTTON_HEIGHT,
            "[ Shipping Bin ]", isMouseOver(mouseX, mouseY, binBtnX, binBtnY, BUTTON_WIDTH, BUTTON_HEIGHT));
        
        drawStyledButton(graphics, skillsBtnX, skillsBtnY, BUTTON_WIDTH, BUTTON_HEIGHT,
            "[ My Skills ]", isMouseOver(mouseX, mouseY, skillsBtnX, skillsBtnY, BUTTON_WIDTH, BUTTON_HEIGHT));
        
        // Draw footer hint
        drawCenteredText(graphics, "§7Press ESC to close", 
            windowX + windowWidth / 2, windowY + windowHeight - 18, COLOR_GRAY_TEXT);
        
        // Check if shift is held for Black Market hint
        if (isShiftDown()) {
            drawCenteredText(graphics, "§5§o✦ Black Market available in Shop...", 
                windowX + windowWidth / 2, windowY + windowHeight - 30, 0xFFAA00AA);
        }
    }
    
    /**
     * Draw a styled button that covers the vanilla button rendering
     */
    private void drawStyledButton(GuiGraphics graphics, int x, int y, int w, int h, String label, boolean hovered) {
        // Draw background (covers vanilla button)
        int bgColor = hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON_BG;
        graphics.fill(x, y, x + w, y + h, bgColor);
        
        // Draw gold border
        drawBorder(graphics, x, y, w, h, COLOR_GOLD_BORDER);
        
        // Draw centered label
        int textWidth = font.width(label);
        int textX = x + (w - textWidth) / 2;
        int textY = y + (h - 8) / 2;
        graphics.drawString(font, label, textX, textY, hovered ? COLOR_GOLD_TEXT : COLOR_WHITE_TEXT, true);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Render darker blur for hub
        graphics.fill(0, 0, width, height, 0xA0000000);
        
        // Draw main window background FIRST
        drawWindowBackground(graphics, windowX, windowY, windowWidth, windowHeight);
        
        // Draw title
        drawCenteredTitle(graphics, "✦ RPG Hub ✦", windowX + windowWidth / 2, windowY + 10);
        
        // Render the vanilla widgets (buttons) - they will render gray
        super.render(graphics, mouseX, mouseY, delta);
        
        // Now render our content ON TOP - this covers the gray buttons with our styled ones
        renderContent(graphics, mouseX, mouseY, delta);
    }
}
