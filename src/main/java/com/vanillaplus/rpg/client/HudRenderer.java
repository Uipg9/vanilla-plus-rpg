package com.vanillaplus.rpg.client;

import com.vanillaplus.rpg.VanillaPlusRpg;
import com.vanillaplus.rpg.data.PlayerDataManager;
import com.vanillaplus.rpg.economy.MarketManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HUD Renderer for the Action Bar
 * Shows: [ Lv X ] $XXX | XP: [||||....] | Hot: ItemName
 * 
 * UPDATED FOR 1.21.11: Uses Component API with proper ternary parenthesization
 */
@Environment(EnvType.CLIENT)
public class HudRenderer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaPlusRpg.MOD_ID + "-hud");
    private static int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 10; // Update every 10 ticks (0.5 seconds)
    
    // Cached values (synced from server)
    private static long cachedMoney = 0;
    private static int cachedLevel = 1;
    private static int cachedXp = 0;
    private static int cachedXpRequired = 100;
    private static String cachedHotItem = "None";
    private static int cachedSkillPoints = 0;
    
    /**
     * Register the HUD renderer
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(HudRenderer::onClientTick);
        LOGGER.info("HUD renderer registered");
    }
    
    /**
     * Client tick handler
     */
    private static void onClientTick(Minecraft client) {
        if (client.player == null) return;
        if (client.isPaused()) return;
        
        tickCounter++;
        
        // Only update HUD at intervals to avoid spam
        if (tickCounter >= UPDATE_INTERVAL) {
            tickCounter = 0;
            updateActionBar(client);
        }
    }
    
    /**
     * Update the action bar with player stats
     */
    private static void updateActionBar(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;
        
        // Build the action bar text
        // Format: [ Lv X ] $XXX | XP: [||||....] | Hot: ItemName
        
        MutableComponent actionBar = Component.empty();
        
        // Level section - Gold color
        actionBar.append(Component.literal("§6[ Lv" + cachedLevel + " ] "));
        
        // Money section - Green color
        actionBar.append(Component.literal("§a$" + formatMoney(cachedMoney) + " "));
        
        // Separator
        actionBar.append(Component.literal("§f| "));
        
        // XP Bar section
        String xpBar = buildXpBar(cachedXp, cachedXpRequired);
        actionBar.append(Component.literal("§bXP: " + xpBar + " "));
        
        // Separator
        actionBar.append(Component.literal("§f| "));
        
        // Hot Item section - Yellow color
        actionBar.append(Component.literal("§7Hot: §e" + cachedHotItem));
        
        // Send to action bar
        // 1.21.11: Use displayClientMessage(Component, true) for action bar
        player.displayClientMessage(actionBar, true);
    }
    
    /**
     * Build the XP progress bar
     * Format: [||||....] (10 characters total)
     */
    private static String buildXpBar(int currentXp, int requiredXp) {
        int barLength = 10;
        int filledLength = 0;
        
        if (requiredXp > 0) {
            filledLength = (int) ((float) currentXp / requiredXp * barLength);
            filledLength = Math.min(barLength, Math.max(0, filledLength));
        }
        
        StringBuilder bar = new StringBuilder("§a[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledLength) {
                bar.append("§a|"); // Filled (green)
            } else {
                bar.append("§7."); // Empty (gray)
            }
        }
        bar.append("§a]");
        
        return bar.toString();
    }
    
    /**
     * Format money with commas
     */
    private static String formatMoney(long amount) {
        return String.format("%,d", amount);
    }
    
    /**
     * Update cached values from server
     * Called when receiving sync packets or from server-side
     */
    public static void updateCachedValues(long money, int level, int xp, int xpRequired, String hotItem, int skillPoints) {
        cachedMoney = money;
        cachedLevel = level;
        cachedXp = xp;
        cachedXpRequired = xpRequired;
        cachedHotItem = hotItem;
        cachedSkillPoints = skillPoints;
    }
    
    /**
     * Update money cache
     */
    public static void setCachedMoney(long money) {
        cachedMoney = money;
    }
    
    /**
     * Update level cache
     */
    public static void setCachedLevel(int level) {
        cachedLevel = level;
    }
    
    /**
     * Update XP cache
     */
    public static void setCachedXp(int xp, int xpRequired) {
        cachedXp = xp;
        cachedXpRequired = xpRequired;
    }
    
    /**
     * Update hot item cache
     */
    public static void setCachedHotItem(String hotItem) {
        cachedHotItem = hotItem;
    }
    
    /**
     * Get cached money value
     */
    public static long getCachedMoney() {
        return cachedMoney;
    }
    
    /**
     * Get cached level
     */
    public static int getCachedLevel() {
        return cachedLevel;
    }
    
    /**
     * Get cached XP
     */
    public static int getCachedXp() {
        return cachedXp;
    }
    
    /**
     * Get cached XP required
     */
    public static int getCachedXpRequired() {
        return cachedXpRequired;
    }
    
    /**
     * Get XP progress as percentage (0-100)
     */
    public static int getXpProgressPercent() {
        if (cachedXpRequired <= 0) return 0;
        return Math.min(100, (int) ((float) cachedXp / cachedXpRequired * 100));
    }
    
    /**
     * Get cached skill points
     */
    public static int getCachedSkillPoints() {
        return cachedSkillPoints;
    }
    
    /**
     * Set cached skill points
     */
    public static void setCachedSkillPoints(int skillPoints) {
        cachedSkillPoints = skillPoints;
    }
}
