package com.vanillaplus.rpg;

import com.vanillaplus.rpg.client.HudRenderer;
import com.vanillaplus.rpg.client.KeyBindings;
import com.vanillaplus.rpg.client.RewardOverlay;
import com.vanillaplus.rpg.network.PlayerDataSyncHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side initializer for Vanilla+ RPG & Shop
 * 
 * Handles:
 * - HUD rendering (action bar stats)
 * - Keybindings (H to open Hub)
 * - Network receivers for data sync
 * - Custom GUI screens (via DrawContext API)
 * - Reward overlay for XP/money notifications
 */
@Environment(EnvType.CLIENT)
public class VanillaPlusRpgClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(VanillaPlusRpg.MOD_ID + "-client");
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {} client for Minecraft 1.21.11", VanillaPlusRpg.MOD_ID);
        
        // Register keybindings
        KeyBindings.register();
        
        // Register HUD renderer
        HudRenderer.register();
        
        // Register reward notification overlay
        RewardOverlay.register();
        
        // Register network handlers (client-side)
        PlayerDataSyncHandler.registerClient();
        
        LOGGER.info("{} client initialized successfully!", VanillaPlusRpg.MOD_ID);
    }
}
