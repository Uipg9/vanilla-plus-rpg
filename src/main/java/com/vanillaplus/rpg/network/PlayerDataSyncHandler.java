package com.vanillaplus.rpg.network;

import com.vanillaplus.rpg.VanillaPlusRpg;
import com.vanillaplus.rpg.client.ClientSkillCache;
import com.vanillaplus.rpg.client.HudRenderer;
import com.vanillaplus.rpg.client.RewardOverlay;
import com.vanillaplus.rpg.data.PlayerDataManager;
import com.vanillaplus.rpg.economy.MarketManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
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
    
    // Packet IDs
    public static final Identifier PLAYER_DATA_SYNC_ID = Identifier.fromNamespaceAndPath(VanillaPlusRpg.MOD_ID, "player_data_sync");
    public static final Identifier REWARD_NOTIFICATION_ID = Identifier.fromNamespaceAndPath(VanillaPlusRpg.MOD_ID, "reward_notification");
    public static final Identifier SKILL_DATA_SYNC_ID = Identifier.fromNamespaceAndPath(VanillaPlusRpg.MOD_ID, "skill_data_sync");
    public static final Identifier SKILL_UPGRADE_ID = Identifier.fromNamespaceAndPath(VanillaPlusRpg.MOD_ID, "skill_upgrade");
    
    // Sync interval (every 20 ticks = 1 second)
    private static final int SYNC_INTERVAL = 20;
    private static int tickCounter = 0;
    
    /**
     * Player data sync payload
     */
    public record PlayerDataSyncPayload(long money, int level, int xp, int xpRequired, String hotItem, int skillPoints) 
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
            int skillPoints = buf.readInt();
            return new PlayerDataSyncPayload(money, level, xp, xpRequired, hotItem, skillPoints);
        }
        
        public static void write(FriendlyByteBuf buf, PlayerDataSyncPayload payload) {
            buf.writeLong(payload.money);
            buf.writeInt(payload.level);
            buf.writeInt(payload.xp);
            buf.writeInt(payload.xpRequired);
            buf.writeUtf(payload.hotItem, 100);
            buf.writeInt(payload.skillPoints);
        }
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    /**
     * Reward notification payload - shows XP/money gains on client
     * RewardType: 0 = general, 1 = combat monster, 2 = combat animal, 3 = level up
     */
    public record RewardNotificationPayload(int xp, long money, int vanillaXp, int rewardType) 
            implements CustomPacketPayload {
        
        public static final CustomPacketPayload.Type<RewardNotificationPayload> TYPE = 
            new CustomPacketPayload.Type<>(REWARD_NOTIFICATION_ID);
        
        public static final StreamCodec<FriendlyByteBuf, RewardNotificationPayload> STREAM_CODEC = 
            StreamCodec.of(RewardNotificationPayload::write, RewardNotificationPayload::read);
        
        public static RewardNotificationPayload read(FriendlyByteBuf buf) {
            int xp = buf.readInt();
            long money = buf.readLong();
            int vanillaXp = buf.readInt();
            int rewardType = buf.readInt();
            return new RewardNotificationPayload(xp, money, vanillaXp, rewardType);
        }
        
        public static void write(FriendlyByteBuf buf, RewardNotificationPayload payload) {
            buf.writeInt(payload.xp);
            buf.writeLong(payload.money);
            buf.writeInt(payload.vanillaXp);
            buf.writeInt(payload.rewardType);
        }
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    /**
     * Skill data sync payload - sends all skill levels to client
     * Order: farming, combat, defense, smithing, woodcutting, mining
     */
    public record SkillDataSyncPayload(int farming, int combat, int defense, int smithing, int woodcutting, int mining) 
            implements CustomPacketPayload {
        
        public static final CustomPacketPayload.Type<SkillDataSyncPayload> TYPE = 
            new CustomPacketPayload.Type<>(SKILL_DATA_SYNC_ID);
        
        public static final StreamCodec<FriendlyByteBuf, SkillDataSyncPayload> STREAM_CODEC = 
            StreamCodec.of(SkillDataSyncPayload::write, SkillDataSyncPayload::read);
        
        public static SkillDataSyncPayload read(FriendlyByteBuf buf) {
            int farming = buf.readInt();
            int combat = buf.readInt();
            int defense = buf.readInt();
            int smithing = buf.readInt();
            int woodcutting = buf.readInt();
            int mining = buf.readInt();
            return new SkillDataSyncPayload(farming, combat, defense, smithing, woodcutting, mining);
        }
        
        public static void write(FriendlyByteBuf buf, SkillDataSyncPayload payload) {
            buf.writeInt(payload.farming);
            buf.writeInt(payload.combat);
            buf.writeInt(payload.defense);
            buf.writeInt(payload.smithing);
            buf.writeInt(payload.woodcutting);
            buf.writeInt(payload.mining);
        }
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    /**
     * Skill upgrade request payload - sent from client to server
     * skillIndex: 0=Farming, 1=Combat, 2=Defense, 3=Smithing, 4=Woodcutting, 5=Mining
     */
    public record SkillUpgradePayload(int skillIndex) implements CustomPacketPayload {
        
        public static final CustomPacketPayload.Type<SkillUpgradePayload> TYPE = 
            new CustomPacketPayload.Type<>(SKILL_UPGRADE_ID);
        
        public static final StreamCodec<FriendlyByteBuf, SkillUpgradePayload> STREAM_CODEC = 
            StreamCodec.of(SkillUpgradePayload::write, SkillUpgradePayload::read);
        
        public static SkillUpgradePayload read(FriendlyByteBuf buf) {
            return new SkillUpgradePayload(buf.readInt());
        }
        
        public static void write(FriendlyByteBuf buf, SkillUpgradePayload payload) {
            buf.writeInt(payload.skillIndex);
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
        // Register payload types (S2C = Server to Client)
        PayloadTypeRegistry.playS2C().register(PlayerDataSyncPayload.TYPE, PlayerDataSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(RewardNotificationPayload.TYPE, RewardNotificationPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SkillDataSyncPayload.TYPE, SkillDataSyncPayload.STREAM_CODEC);
        
        // Register payload types (C2S = Client to Server)
        PayloadTypeRegistry.playC2S().register(SkillUpgradePayload.TYPE, SkillUpgradePayload.STREAM_CODEC);
        
        // Register server receiver for skill upgrade requests
        ServerPlayNetworking.registerGlobalReceiver(SkillUpgradePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                // Get the skill by index
                PlayerDataManager.Skill[] skills = PlayerDataManager.Skill.values();
                if (payload.skillIndex >= 0 && payload.skillIndex < skills.length) {
                    PlayerDataManager.Skill skill = skills[payload.skillIndex];
                    
                    // Try to upgrade
                    if (PlayerDataManager.upgradeSkill(player, skill)) {
                        LOGGER.debug("Player {} upgraded {} to level {}", 
                            player.getName().getString(), 
                            skill.displayName,
                            PlayerDataManager.getSkillLevel(player, skill));
                        
                        // Sync data back to client immediately
                        syncToPlayer(player);
                    }
                }
            });
        });
        
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
                    payload.hotItem(),
                    payload.skillPoints()
                );
            });
        });
        
        // Register receiver for reward notifications
        ClientPlayNetworking.registerGlobalReceiver(RewardNotificationPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                switch (payload.rewardType()) {
                    case 1 -> RewardOverlay.showCombatReward(payload.xp(), payload.money(), true);  // Monster
                    case 2 -> RewardOverlay.showCombatReward(payload.xp(), payload.money(), false); // Animal
                    case 3 -> RewardOverlay.showLevelUp(payload.xp(), payload.money()); // Level up (xp field = new level)
                    case 4 -> showSmeltingActionBar(context.client(), payload.xp(), payload.money()); // Smelting - action bar
                    default -> RewardOverlay.showReward(payload.xp(), payload.money()); // General reward
                }
            });
        });
        
        // Register receiver for skill data sync
        ClientPlayNetworking.registerGlobalReceiver(SkillDataSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientSkillCache.updateSkillLevels(
                    payload.farming(),
                    payload.combat(),
                    payload.defense(),
                    payload.smithing(),
                    payload.woodcutting(),
                    payload.mining()
                );
            });
        });
        
        LOGGER.info("Client-side network handlers registered");
    }
    
    /**
     * Show smelting reward in action bar (more visible than overlay)
     */
    private static void showSmeltingActionBar(Minecraft minecraft, int xp, long money) {
        if (minecraft.player == null) return;
        
        StringBuilder msg = new StringBuilder("§d🔥 ");
        
        if (xp > 0) {
            msg.append("§d+").append(xp).append(" XP");
        }
        if (money > 0) {
            if (xp > 0) msg.append("  ");
            msg.append("§a+$").append(money);
        }
        
        minecraft.player.displayClientMessage(net.minecraft.network.chat.Component.literal(msg.toString()), true);
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
            int skillPoints = PlayerDataManager.getSkillPoints(player);
            
            // Send main data payload
            PlayerDataSyncPayload payload = new PlayerDataSyncPayload(money, level, xp, xpRequired, hotItem, skillPoints);
            ServerPlayNetworking.send(player, payload);
            
            // Send skill data payload
            SkillDataSyncPayload skillPayload = new SkillDataSyncPayload(
                PlayerDataManager.getSkillLevel(player, PlayerDataManager.Skill.FARMING),
                PlayerDataManager.getSkillLevel(player, PlayerDataManager.Skill.COMBAT),
                PlayerDataManager.getSkillLevel(player, PlayerDataManager.Skill.DEFENSE),
                PlayerDataManager.getSkillLevel(player, PlayerDataManager.Skill.SMITHING),
                PlayerDataManager.getSkillLevel(player, PlayerDataManager.Skill.WOODCUTTING),
                PlayerDataManager.getSkillLevel(player, PlayerDataManager.Skill.MINING)
            );
            ServerPlayNetworking.send(player, skillPayload);
        } catch (Exception e) {
            // Silently ignore if player disconnected
        }
    }
    
    /**
     * Send a general reward notification to the player
     */
    public static void sendRewardNotification(ServerPlayer player, int xp, long money) {
        sendRewardNotification(player, xp, money, 0, 0);
    }
    
    /**
     * Send a reward notification with vanilla XP
     */
    public static void sendRewardNotification(ServerPlayer player, int xp, long money, int vanillaXp) {
        sendRewardNotification(player, xp, money, vanillaXp, 0);
    }
    
    /**
     * Send a combat reward notification (monster or animal)
     */
    public static void sendCombatRewardNotification(ServerPlayer player, int xp, long money, int vanillaXp, boolean isMonster) {
        sendRewardNotification(player, xp, money, vanillaXp, isMonster ? 1 : 2);
    }
    
    /**
     * Send a level up notification
     */
    public static void sendLevelUpNotification(ServerPlayer player, int newLevel, long bonusMoney) {
        sendRewardNotification(player, newLevel, bonusMoney, 0, 3);
    }
    
    /**
     * Send a reward notification packet to the player
     * @param player The player to notify
     * @param xp Mod XP gained (or new level for level up)
     * @param money Money gained
     * @param vanillaXp Vanilla XP points gained
     * @param rewardType 0=general, 1=combat monster, 2=combat animal, 3=level up
     */
    public static void sendRewardNotification(ServerPlayer player, int xp, long money, int vanillaXp, int rewardType) {
        try {
            RewardNotificationPayload payload = new RewardNotificationPayload(xp, money, vanillaXp, rewardType);
            ServerPlayNetworking.send(player, payload);
        } catch (Exception e) {
            // Silently ignore if player disconnected
        }
    }
}
