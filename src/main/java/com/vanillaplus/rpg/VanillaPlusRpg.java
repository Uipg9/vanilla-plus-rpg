package com.vanillaplus.rpg;

import com.vanillaplus.rpg.command.ModCommands;
import com.vanillaplus.rpg.config.ShopConfig;
import com.vanillaplus.rpg.data.PlayerDataManager;
import com.vanillaplus.rpg.economy.MarketManager;
import com.vanillaplus.rpg.network.PlayerDataSyncHandler;
import com.vanillaplus.rpg.xp.XpEventHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vanilla+ RPG & Shop - Main Mod Initializer
 * For Minecraft 1.21.11 Fabric
 * 
 * Features:
 * - Custom DrawContext GUI system (no SGUI)
 * - Server-authoritative economy
 * - Client-server data sync via packets
 * - XP system with level progression
 * 
 * Uses Mojang mappings (ServerPlayer, Component, etc.)
 */
public class VanillaPlusRpg implements ModInitializer {
    public static final String MOD_ID = "vanillaplusrpg";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {} for Minecraft 1.21.11", MOD_ID);
        
        // Load shop configuration
        ShopConfig.init();
        
        // Register commands
        ModCommands.register();
        
        // Register XP event handlers
        XpEventHandler.register();
        
        // Register network handlers (server-side)
        PlayerDataSyncHandler.registerServer();
        
        // Player join/leave handlers
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Initialize new player data
            PlayerDataManager.initNewPlayer(handler.getPlayer());
            // Sync data immediately on join
            PlayerDataSyncHandler.syncToPlayer(handler.getPlayer());
        });
        
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // Save and unload player data
            PlayerDataManager.unloadPlayer(handler.getPlayer());
        });
        
        // Server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("{} server started!", MOD_ID);
            MarketManager.init(server);
        });
        
        // Server tick events - for market rotation
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MarketManager.tick(server);
        });
        
        LOGGER.info("{} initialized successfully!", MOD_ID);
    }
}
