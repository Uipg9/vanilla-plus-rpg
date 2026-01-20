package com.vanillaplus.rpg.gui;

import com.vanillaplus.rpg.client.ClientSkillCache;
import com.vanillaplus.rpg.client.HudRenderer;
import com.vanillaplus.rpg.network.PlayerDataSyncHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Skills Screen - Display and upgrade player's RPG skills
 * Fixed layout with scroll support for 6 skills
 */
public class SkillsScreen extends BaseRpgScreen {
    
    // Skill data - matches PlayerDataManager.Skill enum order
    private static final String[] SKILL_NAMES = {
        "Farming", "Combat", "Defense", "Smithing", "Woodcutting", "Mining"
    };
    
    private static final String[] SKILL_DESCRIPTIONS = {
        "Double crop yield", "Critical hit bonus", "Extra defense",
        "Double smelt output", "Extra wood drops", "Extra ore drops"
    };
    
    private static final int[] SKILL_COLORS = {
        0xFF44AA44, 0xFFFF5555, 0xFF5555FF, 0xFFAA6644, 0xFF44AA44, 0xFFAAFFFF
    };
    
    // Scroll offset for skill list
    private int scrollOffset = 0;
    private static final int VISIBLE_SKILLS = 4; // Show 4 skills at a time
    private static final int ROW_HEIGHT = 40;
    
    public SkillsScreen() {
        super(Component.literal("§6✦ Skills ✦"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Fixed size window
        windowWidth = 300;
        windowHeight = 260;
        windowX = (width - windowWidth) / 2;
        windowY = (height - windowHeight) / 2;
        
        // Add back button
        addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            Minecraft.getInstance().setScreen(new HubScreen());
        }).bounds(windowX + 8, windowY + 8, 50, 16).build());
        
        // Add scroll buttons
        addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            if (scrollOffset > 0) scrollOffset--;
            rebuildSkillButtons();
        }).bounds(windowX + windowWidth - 30, windowY + 55, 20, 16).build());
        
        addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            if (scrollOffset < 6 - VISIBLE_SKILLS) scrollOffset++;
            rebuildSkillButtons();
        }).bounds(windowX + windowWidth - 30, windowY + windowHeight - 55, 20, 16).build());
        
        rebuildSkillButtons();
    }
    
    private void rebuildSkillButtons() {
        // Remove old skill buttons (keep first 3 buttons: back, scroll up, scroll down)
        while (children().size() > 3) {
            removeWidget(children().get(3));
        }
        
        // Add upgrade buttons for visible skills
        int startY = windowY + 75;
        int buttonX = windowX + windowWidth - 55;
        
        for (int i = 0; i < VISIBLE_SKILLS && (scrollOffset + i) < 6; i++) {
            final int skillIndex = scrollOffset + i;
            int btnY = startY + (i * ROW_HEIGHT);
            
            addRenderableWidget(Button.builder(Component.empty(), btn -> {
                playClickSound();
                requestSkillUpgrade(skillIndex);
            }).bounds(buttonX, btnY + 5, 45, 16).build());
        }
    }
    
    private void requestSkillUpgrade(int skillIndex) {
        ClientPlayNetworking.send(new PlayerDataSyncHandler.SkillUpgradePayload(skillIndex));
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draw back button
        int backBtnX = windowX + 8;
        int backBtnY = windowY + 8;
        boolean backHovered = isMouseOver(mouseX, mouseY, backBtnX, backBtnY, 50, 16);
        drawStyledButton(graphics, backBtnX, backBtnY, 50, 16, "< Back", backHovered);
        
        int centerX = windowX + windowWidth / 2;
        int y = windowY + 30;
        
        // Get data
        int level = HudRenderer.getCachedLevel();
        int skillPoints = HudRenderer.getCachedSkillPoints();
        
        // Header
        drawCenteredText(graphics, "§6Level §f" + level + "  §e|  §aSkill Points: §f" + skillPoints, centerX, y, COLOR_WHITE_TEXT);
        y += 15;
        
        // Divider
        graphics.fill(windowX + 15, y, windowX + windowWidth - 15, y + 1, COLOR_GOLD_BORDER);
        y += 10;
        
        // Draw scroll up button
        int scrollUpX = windowX + windowWidth - 30;
        int scrollUpY = windowY + 55;
        boolean canScrollUp = scrollOffset > 0;
        boolean upHovered = isMouseOver(mouseX, mouseY, scrollUpX, scrollUpY, 20, 16);
        drawScrollButton(graphics, scrollUpX, scrollUpY, 20, 16, "▲", upHovered && canScrollUp, canScrollUp);
        
        // Draw scroll down button
        int scrollDownY = windowY + windowHeight - 55;
        boolean canScrollDown = scrollOffset < 6 - VISIBLE_SKILLS;
        boolean downHovered = isMouseOver(mouseX, mouseY, scrollUpX, scrollDownY, 20, 16);
        drawScrollButton(graphics, scrollUpX, scrollDownY, 20, 16, "▼", downHovered && canScrollDown, canScrollDown);
        
        // Draw visible skills
        int startY = windowY + 75;
        int skillNameX = windowX + 15;
        int buttonX = windowX + windowWidth - 55;
        
        for (int i = 0; i < VISIBLE_SKILLS && (scrollOffset + i) < 6; i++) {
            int skillIndex = scrollOffset + i;
            int rowY = startY + (i * ROW_HEIGHT);
            
            int skillLevel = ClientSkillCache.getSkillLevel(skillIndex);
            int bonusPercent = skillLevel * 5;
            int skillPoints2 = HudRenderer.getCachedSkillPoints();
            boolean canUpgrade = skillPoints2 > 0 && skillLevel < 10;
            
            // Row highlight on hover
            boolean rowHovered = mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 5 && mouseX >= skillNameX && mouseX < buttonX - 10;
            if (rowHovered) {
                graphics.fill(windowX + 10, rowY - 2, windowX + windowWidth - 60, rowY + ROW_HEIGHT - 7, 0x20FFFFFF);
            }
            
            // Skill name
            int skillColor = SKILL_COLORS[skillIndex];
            graphics.drawString(font, SKILL_NAMES[skillIndex], skillNameX, rowY, skillColor, true);
            
            // Description
            graphics.drawString(font, "§7" + SKILL_DESCRIPTIONS[skillIndex], skillNameX + 5, rowY + 12, 0xFF888888, true);
            
            // Level and bonus on same line
            String lvlText = skillLevel == 10 ? "§aMAX" : "§fLv " + skillLevel;
            String bonusText = skillLevel == 0 ? "" : " §e(+" + bonusPercent + "%)";
            graphics.drawString(font, lvlText + bonusText, skillNameX + 5, rowY + 24, 0xFFFFFFFF, true);
            
            // Draw upgrade button
            int btnY = rowY + 5;
            boolean btnHovered = isMouseOver(mouseX, mouseY, buttonX, btnY, 45, 16);
            if (skillLevel >= 10) {
                drawDisabledButton(graphics, buttonX, btnY, 45, 16, "MAX");
            } else if (!canUpgrade) {
                drawDisabledButton(graphics, buttonX, btnY, 45, 16, "+1");
            } else {
                drawUpgradeButton(graphics, buttonX, btnY, 45, 16, "+1", btnHovered);
            }
        }
        
        // Scroll indicator
        String scrollText = "§7(" + (scrollOffset + 1) + "-" + Math.min(scrollOffset + VISIBLE_SKILLS, 6) + " of 6)";
        drawCenteredText(graphics, scrollText, centerX, windowY + windowHeight - 35, 0xFF888888);
        
        // Footer
        drawCenteredText(graphics, "§7Each level = +5% bonus (max 50%)", centerX, windowY + windowHeight - 20, 0xFF666666);
    }
    
    private void drawStyledButton(GuiGraphics graphics, int x, int y, int w, int h, String label, boolean hovered) {
        int bgColor = hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON_BG;
        graphics.fill(x, y, x + w, y + h, bgColor);
        drawBorder(graphics, x, y, w, h, COLOR_GOLD_BORDER);
        int textX = x + (w - font.width(label)) / 2;
        int textY = y + (h - 8) / 2;
        graphics.drawString(font, label, textX, textY, hovered ? COLOR_GOLD_TEXT : COLOR_WHITE_TEXT, true);
    }
    
    private void drawScrollButton(GuiGraphics graphics, int x, int y, int w, int h, String label, boolean hovered, boolean enabled) {
        int bgColor = enabled ? (hovered ? 0xFF444444 : 0xFF333333) : 0xFF222222;
        int borderColor = enabled ? 0xFF666666 : 0xFF444444;
        int textColor = enabled ? (hovered ? 0xFFFFFFFF : 0xFFAAAAAA) : 0xFF555555;
        graphics.fill(x, y, x + w, y + h, bgColor);
        drawBorder(graphics, x, y, w, h, borderColor);
        int textX = x + (w - font.width(label)) / 2;
        int textY = y + (h - 8) / 2;
        graphics.drawString(font, label, textX, textY, textColor, true);
    }
    
    private void drawUpgradeButton(GuiGraphics graphics, int x, int y, int w, int h, String label, boolean hovered) {
        int bgColor = hovered ? 0xFF336633 : 0xFF224422;
        int borderColor = hovered ? 0xFF66FF66 : 0xFF44AA44;
        graphics.fill(x, y, x + w, y + h, bgColor);
        drawBorder(graphics, x, y, w, h, borderColor);
        int textX = x + (w - font.width(label)) / 2;
        int textY = y + (h - 8) / 2;
        graphics.drawString(font, label, textX, textY, hovered ? 0xFFFFFFFF : 0xFFAAFFAA, true);
    }
    
    private void drawDisabledButton(GuiGraphics graphics, int x, int y, int w, int h, String label) {
        graphics.fill(x, y, x + w, y + h, 0xFF333333);
        drawBorder(graphics, x, y, w, h, 0xFF555555);
        int textX = x + (w - font.width(label)) / 2;
        int textY = y + (h - 8) / 2;
        graphics.drawString(font, label, textX, textY, 0xFF888888, true);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll with mouse wheel
        if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
            rebuildSkillButtons();
            return true;
        } else if (scrollY < 0 && scrollOffset < 6 - VISIBLE_SKILLS) {
            scrollOffset++;
            rebuildSkillButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xA0000000);
        drawWindowBackground(graphics, windowX, windowY, windowWidth, windowHeight);
        drawCenteredTitle(graphics, "✦ Skills ✦", windowX + windowWidth / 2, windowY + 10);
        super.render(graphics, mouseX, mouseY, delta);
        renderContent(graphics, mouseX, mouseY, delta);
    }
}
