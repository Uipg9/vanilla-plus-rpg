package com.vanillaplus.rpg.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Client-side overlay for showing XP/money gain notifications
 * These persist longer than vanilla action bar messages (5+ seconds)
 * Shows above the action bar with smooth fade-out animation
 */
@Environment(EnvType.CLIENT)
public class RewardOverlay {
    
    // Duration to show rewards (in ticks, 20 ticks = 1 second)
    private static final int DISPLAY_DURATION = 100; // 5 seconds
    private static final int FADE_DURATION = 20; // 1 second fade out
    
    // List of active reward notifications
    private static final List<RewardNotification> activeRewards = new ArrayList<>();
    
    // Notification record
    private record RewardNotification(String message, long createdAt, int color) {}
    
    /**
     * Register the HUD render callback
     */
    public static void register() {
        HudRenderCallback.EVENT.register((graphics, renderTickCounter) -> {
            renderRewards(graphics);
        });
    }
    
    /**
     * Show a reward notification (XP, money, or both)
     * Call this from client-side to add a notification
     */
    public static void showReward(int xp, long money) {
        if (xp <= 0 && money <= 0) return;
        
        StringBuilder msg = new StringBuilder();
        
        if (xp > 0) {
            msg.append("§b+").append(xp).append(" XP");
        }
        
        if (money > 0) {
            if (msg.length() > 0) msg.append("  ");
            msg.append("§a+$").append(formatMoney(money));
        }
        
        addNotification(msg.toString(), 0xFFFFFFFF);
    }
    
    /**
     * Show XP only reward
     */
    public static void showXpReward(int xp) {
        if (xp <= 0) return;
        addNotification("§b+" + xp + " XP", 0xFF55FFFF);
    }
    
    /**
     * Show money only reward
     */
    public static void showMoneyReward(long money) {
        if (money <= 0) return;
        addNotification("§a+$" + formatMoney(money), 0xFF55FF55);
    }
    
    /**
     * Show combat reward with symbol
     */
    public static void showCombatReward(int xp, long money, boolean isMonster) {
        String symbol = isMonster ? "⚔" : "🥩";
        StringBuilder msg = new StringBuilder("§c").append(symbol).append(" ");
        
        if (xp > 0) {
            msg.append("§b+").append(xp).append(" XP");
        }
        if (money > 0) {
            if (xp > 0) msg.append("  ");
            msg.append("§a+$").append(formatMoney(money));
        }
        
        addNotification(msg.toString(), 0xFFFFFFFF);
    }
    
    /**
     * Show level up notification (extra prominent)
     */
    public static void showLevelUp(int newLevel, long bonusMoney) {
        addNotification("§6§l⬆ LEVEL " + newLevel + "! §a+$" + formatMoney(bonusMoney), 0xFFFFD700);
    }
    
    /**
     * Show smelting reward (Smithing skill)
     */
    public static void showSmeltingReward(int xp, long money) {
        StringBuilder msg = new StringBuilder("§d🔥 ");
        
        if (xp > 0) {
            msg.append("§d+").append(xp).append(" Smithing");
        }
        if (money > 0) {
            if (xp > 0) msg.append("  ");
            msg.append("§a+$").append(formatMoney(money));
        }
        
        addNotification(msg.toString(), 0xFFAA6644);
    }
    
    /**
     * Add a custom notification
     */
    private static void addNotification(String message, int color) {
        // Remove oldest if we have too many
        if (activeRewards.size() > 5) {
            activeRewards.remove(0);
        }
        
        activeRewards.add(new RewardNotification(message, System.currentTimeMillis(), color));
    }
    
    /**
     * Render all active reward notifications
     */
    private static void renderRewards(GuiGraphics graphics) {
        if (activeRewards.isEmpty()) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // Allow rendering even when screen is open (for testing), but skip pause menu
        if (mc.isPaused()) return;
        
        long now = System.currentTimeMillis();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // Position higher above the action bar area (avoid hotbar)
        int baseY = screenHeight - 80;
        int yOffset = 0;
        
        // Iterate and render (and remove expired)
        Iterator<RewardNotification> iter = activeRewards.iterator();
        while (iter.hasNext()) {
            RewardNotification reward = iter.next();
            
            long age = now - reward.createdAt();
            long ageTicks = age / 50; // Convert to ticks (approx)
            
            // Remove if expired
            if (ageTicks > DISPLAY_DURATION + FADE_DURATION) {
                iter.remove();
                continue;
            }
            
            // Calculate alpha for fade out
            float alpha = 1.0f;
            if (ageTicks > DISPLAY_DURATION) {
                alpha = 1.0f - ((float)(ageTicks - DISPLAY_DURATION) / FADE_DURATION);
            }
            alpha = Mth.clamp(alpha, 0.0f, 1.0f);
            
            // Skip if invisible
            if (alpha < 0.05f) continue;
            
            // Render the text
            String text = reward.message();
            int textWidth = mc.font.width(text);
            int x = (screenWidth - textWidth) / 2;
            int y = baseY - yOffset;
            
            // Add a slight background for readability
            int bgAlpha = (int)(alpha * 100);
            int bgColor = (bgAlpha << 24) | 0x000000;
            graphics.fill(x - 4, y - 2, x + textWidth + 4, y + 10, bgColor);
            
            // Draw text with shadow and alpha
            int textAlpha = (int)(alpha * 255);
            // Use formatted string which handles § color codes
            graphics.drawString(mc.font, text, x, y, 0xFFFFFF | (textAlpha << 24), true);
            
            yOffset += 14;
        }
    }
    
    /**
     * Format money value for display
     */
    private static String formatMoney(long amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000.0);
        }
        return String.valueOf(amount);
    }
    
    /**
     * Clear all notifications (e.g., when leaving world)
     */
    public static void clear() {
        activeRewards.clear();
    }
}
