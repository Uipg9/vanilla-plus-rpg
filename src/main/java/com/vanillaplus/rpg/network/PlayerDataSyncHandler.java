package com.vanillaplus.rpg.network;

import com.vanillaplus.rpg.VanillaPlusRpg;
import com.vanillaplus.rpg.client.HudRenderer;
import com.vanillaplus.rpg.data.PlayerDataManager;
import com.vanillaplus.rpg.economy.MarketManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles syncing player data from server to client
 * Uses Fabric Networking API with 1.21.11 CustomPacketPayload
 */
public class PlayerDataSyncHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaPlusRpg.MOD_ID + "-network");
    
    // Packet ID
    public static final Identifier PLAYER_DATA_SYNC_ID = Identifier.fromNamespaceAndPath(VanillaPlusRpg.MOD_ID, "player_data_sync");
    
    // Sync interval (every 20 ticks = 1 second)
    private static final int SYNC_INTERVAL = 20;
    private static int tickCounter = 0;
    
    /**
     * Player data sync payload
     */
    public record PlayerDataSyncPayload(long money, int level, int xp, int xpRequired, String hotItem) 
            implements CustomPacketPayload {
        
        public static final CustomPacketPayload.Type<PlayerDataSyncPayload> TYPE = 
            new CustomPacketPayload.Type<>(PLAYER_DATA_SYNC_ID);
        
        public static final StreamCodec<FriendlyByteBuf, PlayerDataSyncPayload> STREAM_CODEC = 
            StreamCodec.of(PlayerDataSyncPayload::write, PlayerDataSyncPayload::read);
        
        public static PlayerDataSyncPayload read(FriendlyByteBuf buf) {
            long money = buf.readLong();
            int level = buf.readInt();
            int xp = buf.readInt();
            int xpRequired = buf.readInt();
            String hotItem = buf.readUtf(100);
            return new PlayerDataSyncPayload(money, level, xp, xpRequired, hotItem);
        }
        
        public static void write(FriendlyByteBuf buf, PlayerDataSyncPayload payload) {
            buf.writeLong(payload.money);
            buf.writeInt(payload.level);
            buf.writeInt(payload.xp);
            buf.writeInt(payload.xpRequired);
            buf.writeUtf(payload.hotItem, 100);
        }
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    /**
     * Register server-side handlers
     */
    public static void registerServer() {
        // Register payload type
        PayloadTypeRegistry.playS2C().register(PlayerDataSyncPayload.TYPE, PlayerDataSyncPayload.STREAM_CODEC);
        
        // Register tick handler to sync data periodically
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= SYNC_INTERVAL) {
                tickCounter = 0;
                
                // Sync to all players
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    syncToPlayer(player);
                }
            }
        });
        
        LOGGER.info("Server-side network handlers registered");
    }
    
    /**
     * Register client-side handlers
     */
    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        // Register receiver for player data sync
        ClientPlayNetworking.registerGlobalReceiver(PlayerDataSyncPayload.TYPE, (payload, context) -> {
            // Update HUD cache on main thread
            context.client().execute(() -> {
                HudRenderer.updateCachedValues(
                    payload.money(),
                    payload.level(),
                    payload.xp(),
                    payload.xpRequired(),
                    payload.hotItem()
                );
            });
        });
        
        LOGGER.info("Client-side network handlers registered");
    }
    
    /**
     * Sync player data to a specific player
     */
    public static void syncToPlayer(ServerPlayer player) {
        try {
            long money = PlayerDataManager.getMoney(player);
            int level = PlayerDataManager.getRpgLevel(player);
            int xp = PlayerDataManager.getRpgXp(player);
            int xpRequired = PlayerDataManager.getXpRequired(level);
            String hotItem = MarketManager.getHotItemName();
            
            PlayerDataSyncPayload payload = new PlayerDataSyncPayload(money, level, xp, xpRequired, hotItem);
            ServerPlayNetworking.send(player, payload);
        } catch (Exception e) {
            // Silently ignore if player disconnected
        }
    }
}
