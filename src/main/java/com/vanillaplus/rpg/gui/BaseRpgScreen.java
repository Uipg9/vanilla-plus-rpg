package com.vanillaplus.rpg.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * Base screen class for all RPG mod screens
 * Features:
 * - Vertical gradient background (transparent black to midnight blue)
 * - 1px gold border
 * - Centered shadowed text utilities
 * - Procedural rendering with DrawContext
 */
public abstract class BaseRpgScreen extends Screen {
    
    // Color constants
    protected static final int COLOR_BACKGROUND_TOP = 0xE6000000;     // 90% transparent black
    protected static final int COLOR_BACKGROUND_BOTTOM = 0xE6001428;  // Deep midnight blue
    protected static final int COLOR_GOLD_BORDER = 0xFFFFD700;        // Gold #FFD700
    protected static final int COLOR_GOLD_TEXT = 0xFFFFD700;          // Gold for titles
    protected static final int COLOR_WHITE_TEXT = 0xFFFFFFFF;         // White for body
    protected static final int COLOR_GRAY_TEXT = 0xFF888888;          // Gray for descriptions
    protected static final int COLOR_GREEN_TEXT = 0xFF55FF55;         // Green for positive
    protected static final int COLOR_RED_TEXT = 0xFFFF5555;           // Red for negative
    protected static final int COLOR_HIGHLIGHT = 0x40FFFFFF;          // White highlight for hover
    protected static final int COLOR_BUTTON_BG = 0xCC222222;          // Button background
    protected static final int COLOR_BUTTON_HOVER = 0xCC444444;       // Button hover
    
    // Layout constants
    protected int windowX;
    protected int windowY;
    protected int windowWidth;
    protected int windowHeight;
    
    protected BaseRpgScreen(Component title) {
        super(title);
    }
    
    /**
     * Helper method to check if shift is held down (1.21.11 compatible)
     * Uses GLFW directly to check keyboard state
     */
    protected boolean isShiftDown() {
        // Use glfwGetCurrentContext to get the window handle
        long window = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
        return org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS ||
               org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }
    
    @Override
    protected void init() {
        super.init();
        // Calculate window bounds - centered, with padding
        windowWidth = Math.min(320, width - 40);
        windowHeight = Math.min(240, height - 40);
        windowX = (width - windowWidth) / 2;
        windowY = (height - windowHeight) / 2;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Apply blur effect (darken background)
        renderDarkBackground(graphics);
        
        // Draw main window with gradient and border
        drawWindowBackground(graphics, windowX, windowY, windowWidth, windowHeight);
        
        // Draw title centered at top
        drawCenteredTitle(graphics, getTitle().getString(), windowX + windowWidth / 2, windowY + 8);
        
        // Let subclasses render their content
        renderContent(graphics, mouseX, mouseY, delta);
        
        // Render widgets (buttons, etc.)
        super.render(graphics, mouseX, mouseY, delta);
    }
    
    /**
     * Render the dark blur effect behind the window
     */
    protected void renderDarkBackground(GuiGraphics graphics) {
        graphics.fill(0, 0, width, height, 0x80000000);
    }
    
    /**
     * Draw a window with gradient background and gold border
     */
    protected void drawWindowBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        // Draw gradient background
        drawGradient(graphics, x, y, w, h, COLOR_BACKGROUND_TOP, COLOR_BACKGROUND_BOTTOM);
        
        // Draw 1px gold border
        drawBorder(graphics, x, y, w, h, COLOR_GOLD_BORDER);
    }
    
    /**
     * Draw vertical gradient
     */
    protected void drawGradient(GuiGraphics graphics, int x, int y, int w, int h, int colorTop, int colorBottom) {
        graphics.fillGradient(x, y, x + w, y + h, colorTop, colorBottom);
    }
    
    /**
     * Draw a 1-pixel border
     */
    protected void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        // Top
        graphics.fill(x, y, x + w, y + 1, color);
        // Bottom
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        // Left
        graphics.fill(x, y, x + 1, y + h, color);
        // Right
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }
    
    /**
     * Draw centered title text with shadow (gold color)
     */
    protected void drawCenteredTitle(GuiGraphics graphics, String text, int centerX, int y) {
        int textWidth = font.width(text);
        graphics.drawString(font, text, centerX - textWidth / 2, y, COLOR_GOLD_TEXT, true);
    }
    
    /**
     * Draw centered text with shadow
     */
    protected void drawCenteredText(GuiGraphics graphics, String text, int centerX, int y, int color) {
        int textWidth = font.width(text);
        graphics.drawString(font, text, centerX - textWidth / 2, y, color, true);
    }
    
    /**
     * Draw left-aligned text with shadow
     */
    protected void drawText(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }
    
    /**
     * Draw a styled button (rectangle with border)
     */
    protected void drawButton(GuiGraphics graphics, int x, int y, int w, int h, String label, boolean hovered) {
        int bgColor = hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON_BG;
        
        // Background
        graphics.fill(x, y, x + w, y + h, bgColor);
        
        // Border
        drawBorder(graphics, x, y, w, h, COLOR_GOLD_BORDER);
        
        // Centered label
        int textWidth = font.width(label);
        int textX = x + (w - textWidth) / 2;
        int textY = y + (h - 8) / 2;
        graphics.drawString(font, label, textX, textY, hovered ? COLOR_GOLD_TEXT : COLOR_WHITE_TEXT, true);
    }
    
    /**
     * Check if mouse is within bounds
     */
    protected boolean isMouseOver(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
    
    /**
     * Play UI click sound
     */
    protected void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
            SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
        );
    }
    
    /**
     * Abstract method for subclasses to render their content
     */
    protected abstract void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta);
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
