package com.vanillaplus.rpg.client;

import com.vanillaplus.rpg.VanillaPlusRpg;
import com.vanillaplus.rpg.gui.HubScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keybinding registration for the mod
 * H key opens the Hub GUI (client-side screen)
 * 
 * UPDATED FOR 1.21.11: 
 * - Using direct GLFW input since KeyMapping API changed significantly
 * - Custom DrawContext screens instead of SGUI
 * 
 * Note: We use GLFW directly because the KeyMapping.Category enum was removed
 * in 1.21.11 and the new API has breaking changes. GLFW works reliably.
 */
@Environment(EnvType.CLIENT)
public class KeyBindings {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaPlusRpg.MOD_ID + "-keys");
    
    // Key tracking for edge detection (press, not hold)
    private static boolean wasHubKeyPressed = false;
    
    /**
     * Register all keybindings using GLFW direct input
     * This bypasses KeyMapping API issues in 1.21.11
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(KeyBindings::handleKeyPress);
        LOGGER.info("Keybindings registered - Press H to open Hub");
    }
    
    /**
     * Handle key presses using GLFW directly
     * Checks for H key to open the Hub screen
     */
    private static void handleKeyPress(Minecraft client) {
        // Skip if not in game or a screen is already open
        if (client.player == null) return;
        if (client.screen != null) return;
        
        // Get the GLFW window handle
        long window = GLFW.glfwGetCurrentContext();
        if (window == 0) return;
        
        // Check H key state
        boolean isHubKeyPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_H) == GLFW.GLFW_PRESS;
        
        // Edge detection - trigger on press, not hold
        if (isHubKeyPressed && !wasHubKeyPressed) {
            client.setScreen(new HubScreen());
        }
        wasHubKeyPressed = isHubKeyPressed;
    }
}
