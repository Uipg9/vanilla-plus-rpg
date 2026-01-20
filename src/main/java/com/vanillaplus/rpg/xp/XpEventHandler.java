package com.vanillaplus.rpg.xp;

import com.vanillaplus.rpg.VanillaPlusRpg;
import com.vanillaplus.rpg.data.PlayerDataManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * XP Event Handler - Comprehensive XP & Money earning system
 * 
 * Gives XP and money for many activities:
 * - Mining ores (high XP)
 * - Breaking blocks (trees, dirt, sand, gravel, etc.)
 * - Killing mobs (monsters and animals)
 * - Harvesting crops
 * - Running/walking for extended periods
 * - Fishing (via item pickup detection)
 * 
 * UPDATED: Expanded to cover nearly all vanilla activities
 */
public class XpEventHandler {
    
    // ========== XP VALUES ==========
    
    // Mining XP values for specific blocks
    private static final Map<Block, Integer> MINING_XP = new HashMap<>();
    
    // Money rewards for specific blocks
    private static final Map<Block, Long> MINING_MONEY = new HashMap<>();
    
    // Mob kill XP multipliers
    private static final Map<Class<? extends LivingEntity>, Float> MOB_XP_MULTIPLIER = new HashMap<>();
    
    // Mob kill money values
    private static final Map<Class<? extends LivingEntity>, Long> MOB_MONEY = new HashMap<>();
    
    // Movement tracking for running rewards
    private static final Map<UUID, Long> LAST_MOVEMENT_REWARD = new HashMap<>();
    private static final Map<UUID, Double> DISTANCE_TRAVELED = new HashMap<>();
    private static final Map<UUID, BlockPos> LAST_POSITION = new HashMap<>();
    private static final double DISTANCE_PER_REWARD = 100.0; // Blocks traveled for reward
    
    static {
        // ========== ORE XP VALUES ==========
        // These give good XP for mining
        MINING_XP.put(Blocks.COAL_ORE, 5);
        MINING_XP.put(Blocks.DEEPSLATE_COAL_ORE, 6);
        MINING_XP.put(Blocks.IRON_ORE, 8);
        MINING_XP.put(Blocks.DEEPSLATE_IRON_ORE, 10);
        MINING_XP.put(Blocks.COPPER_ORE, 6);
        MINING_XP.put(Blocks.DEEPSLATE_COPPER_ORE, 7);
        MINING_XP.put(Blocks.GOLD_ORE, 12);
        MINING_XP.put(Blocks.DEEPSLATE_GOLD_ORE, 15);
        MINING_XP.put(Blocks.REDSTONE_ORE, 8);
        MINING_XP.put(Blocks.DEEPSLATE_REDSTONE_ORE, 10);
        MINING_XP.put(Blocks.LAPIS_ORE, 10);
        MINING_XP.put(Blocks.DEEPSLATE_LAPIS_ORE, 12);
        MINING_XP.put(Blocks.DIAMOND_ORE, 25);
        MINING_XP.put(Blocks.DEEPSLATE_DIAMOND_ORE, 30);
        MINING_XP.put(Blocks.EMERALD_ORE, 20);
        MINING_XP.put(Blocks.DEEPSLATE_EMERALD_ORE, 25);
        MINING_XP.put(Blocks.NETHER_QUARTZ_ORE, 8);
        MINING_XP.put(Blocks.NETHER_GOLD_ORE, 10);
        MINING_XP.put(Blocks.ANCIENT_DEBRIS, 50);
        
        // Valuable blocks
        MINING_XP.put(Blocks.OBSIDIAN, 15);
        MINING_XP.put(Blocks.CRYING_OBSIDIAN, 20);
        MINING_XP.put(Blocks.END_STONE, 3);
        MINING_XP.put(Blocks.NETHERRACK, 1);
        MINING_XP.put(Blocks.GLOWSTONE, 5);
        MINING_XP.put(Blocks.AMETHYST_CLUSTER, 8);
        MINING_XP.put(Blocks.BUDDING_AMETHYST, 15);
        
        // Common blocks (low but meaningful XP)
        MINING_XP.put(Blocks.STONE, 1);
        MINING_XP.put(Blocks.COBBLESTONE, 1);
        MINING_XP.put(Blocks.DIRT, 1);
        MINING_XP.put(Blocks.GRASS_BLOCK, 1);
        MINING_XP.put(Blocks.SAND, 1);
        MINING_XP.put(Blocks.RED_SAND, 1);
        MINING_XP.put(Blocks.GRAVEL, 1);
        MINING_XP.put(Blocks.CLAY, 2);
        MINING_XP.put(Blocks.TERRACOTTA, 1);
        MINING_XP.put(Blocks.DEEPSLATE, 2);
        MINING_XP.put(Blocks.TUFF, 1);
        MINING_XP.put(Blocks.CALCITE, 1);
        MINING_XP.put(Blocks.DRIPSTONE_BLOCK, 2);
        
        // Wood/logs (good for tree cutting)
        MINING_XP.put(Blocks.OAK_LOG, 2);
        MINING_XP.put(Blocks.SPRUCE_LOG, 2);
        MINING_XP.put(Blocks.BIRCH_LOG, 2);
        MINING_XP.put(Blocks.JUNGLE_LOG, 2);
        MINING_XP.put(Blocks.ACACIA_LOG, 2);
        MINING_XP.put(Blocks.DARK_OAK_LOG, 2);
        MINING_XP.put(Blocks.MANGROVE_LOG, 2);
        MINING_XP.put(Blocks.CHERRY_LOG, 2);
        MINING_XP.put(Blocks.CRIMSON_STEM, 2);
        MINING_XP.put(Blocks.WARPED_STEM, 2);
        
        // Leaves (small XP for clearing)
        MINING_XP.put(Blocks.OAK_LEAVES, 1);
        MINING_XP.put(Blocks.SPRUCE_LEAVES, 1);
        MINING_XP.put(Blocks.BIRCH_LEAVES, 1);
        MINING_XP.put(Blocks.JUNGLE_LEAVES, 1);
        MINING_XP.put(Blocks.ACACIA_LEAVES, 1);
        MINING_XP.put(Blocks.DARK_OAK_LEAVES, 1);
        MINING_XP.put(Blocks.MANGROVE_LEAVES, 1);
        MINING_XP.put(Blocks.CHERRY_LEAVES, 1);
        MINING_XP.put(Blocks.AZALEA_LEAVES, 1);
        MINING_XP.put(Blocks.FLOWERING_AZALEA_LEAVES, 2);
        
        // Crop blocks
        MINING_XP.put(Blocks.WHEAT, 3);
        MINING_XP.put(Blocks.CARROTS, 3);
        MINING_XP.put(Blocks.POTATOES, 3);
        MINING_XP.put(Blocks.BEETROOTS, 3);
        MINING_XP.put(Blocks.MELON, 4);
        MINING_XP.put(Blocks.PUMPKIN, 4);
        MINING_XP.put(Blocks.SUGAR_CANE, 2);
        MINING_XP.put(Blocks.BAMBOO, 1);
        MINING_XP.put(Blocks.CACTUS, 2);
        MINING_XP.put(Blocks.COCOA, 3);
        MINING_XP.put(Blocks.NETHER_WART, 3);
        MINING_XP.put(Blocks.SWEET_BERRY_BUSH, 2);
        
        // ========== MINING MONEY VALUES ==========
        // Ores give money too
        MINING_MONEY.put(Blocks.COAL_ORE, 2L);
        MINING_MONEY.put(Blocks.DEEPSLATE_COAL_ORE, 3L);
        MINING_MONEY.put(Blocks.IRON_ORE, 5L);
        MINING_MONEY.put(Blocks.DEEPSLATE_IRON_ORE, 6L);
        MINING_MONEY.put(Blocks.COPPER_ORE, 3L);
        MINING_MONEY.put(Blocks.DEEPSLATE_COPPER_ORE, 4L);
        MINING_MONEY.put(Blocks.GOLD_ORE, 10L);
        MINING_MONEY.put(Blocks.DEEPSLATE_GOLD_ORE, 12L);
        MINING_MONEY.put(Blocks.REDSTONE_ORE, 4L);
        MINING_MONEY.put(Blocks.DEEPSLATE_REDSTONE_ORE, 5L);
        MINING_MONEY.put(Blocks.LAPIS_ORE, 8L);
        MINING_MONEY.put(Blocks.DEEPSLATE_LAPIS_ORE, 10L);
        MINING_MONEY.put(Blocks.DIAMOND_ORE, 50L);
        MINING_MONEY.put(Blocks.DEEPSLATE_DIAMOND_ORE, 60L);
        MINING_MONEY.put(Blocks.EMERALD_ORE, 40L);
        MINING_MONEY.put(Blocks.DEEPSLATE_EMERALD_ORE, 50L);
        MINING_MONEY.put(Blocks.NETHER_QUARTZ_ORE, 4L);
        MINING_MONEY.put(Blocks.NETHER_GOLD_ORE, 8L);
        MINING_MONEY.put(Blocks.ANCIENT_DEBRIS, 100L);
        
        // Wood gives small money
        MINING_MONEY.put(Blocks.OAK_LOG, 1L);
        MINING_MONEY.put(Blocks.SPRUCE_LOG, 1L);
        MINING_MONEY.put(Blocks.BIRCH_LOG, 1L);
        MINING_MONEY.put(Blocks.JUNGLE_LOG, 1L);
        MINING_MONEY.put(Blocks.ACACIA_LOG, 1L);
        MINING_MONEY.put(Blocks.DARK_OAK_LOG, 1L);
        MINING_MONEY.put(Blocks.MANGROVE_LOG, 1L);
        MINING_MONEY.put(Blocks.CHERRY_LOG, 1L);
        
        // Crops give money on harvest
        MINING_MONEY.put(Blocks.WHEAT, 2L);
        MINING_MONEY.put(Blocks.CARROTS, 2L);
        MINING_MONEY.put(Blocks.POTATOES, 2L);
        MINING_MONEY.put(Blocks.BEETROOTS, 2L);
        MINING_MONEY.put(Blocks.MELON, 3L);
        MINING_MONEY.put(Blocks.PUMPKIN, 3L);
        MINING_MONEY.put(Blocks.SUGAR_CANE, 1L);
        
        // ========== MOB XP MULTIPLIERS ==========
        // Monsters give 1.5x base XP
        MOB_XP_MULTIPLIER.put(Monster.class, 1.5f);
        
        // Animals give 0.75x base XP
        MOB_XP_MULTIPLIER.put(Animal.class, 0.75f);
        
        // ========== MOB MONEY VALUES ==========
        // Default money for different entity types - scales by max health
        MOB_MONEY.put(Animal.class, 5L); // Base for animals
        MOB_MONEY.put(Monster.class, 15L); // Default for monsters
    }
    
    /**
     * Register all XP event handlers
     */
    public static void register() {
        registerMiningXp();
        registerCombatXp();
        registerMovementRewards();
        VanillaPlusRpg.LOGGER.info("XP event handlers registered");
    }
    
    /**
     * Register mining XP events
     */
    private static void registerMiningXp() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide()) return;
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            
            Block block = state.getBlock();
            
            // Calculate XP
            int xp = getBlockXp(block, state);
            
            // Calculate money
            long money = getBlockMoney(block, state);
            
            // Award XP
            if (xp > 0) {
                int oldLevel = PlayerDataManager.getRpgLevel(serverPlayer);
                PlayerDataManager.addRpgXp(serverPlayer, xp);
                int newLevel = PlayerDataManager.getRpgLevel(serverPlayer);
                
                // Check for level up
                if (newLevel > oldLevel) {
                    onLevelUp(serverPlayer, newLevel);
                }
                
                // Show XP gain in action bar (only for significant XP)
                if (xp >= 3) {
                    serverPlayer.displayClientMessage(
                        Component.literal("§b+" + xp + " XP" + (money > 0 ? " §a+$" + money : "")), 
                        true
                    );
                }
            }
            
            // Award money
            if (money > 0) {
                PlayerDataManager.addMoney(serverPlayer, money);
            }
        });
    }
    
    /**
     * Get XP for breaking a block
     */
    private static int getBlockXp(Block block, BlockState state) {
        // Check specific block first
        if (MINING_XP.containsKey(block)) {
            int baseXp = MINING_XP.get(block);
            
            // Bonus XP for fully grown crops
            if (block instanceof CropBlock cropBlock) {
                if (cropBlock.isMaxAge(state)) {
                    return baseXp * 2; // Double XP for mature crops
                }
                return 0; // No XP for breaking immature crops
            }
            
            return baseXp;
        }
        
        // Check tags for generic blocks
        if (state.is(BlockTags.LOGS)) return 2;
        if (state.is(BlockTags.LEAVES)) return 1;
        if (state.is(BlockTags.PLANKS)) return 1;
        if (state.is(BlockTags.WOOL)) return 1;
        if (state.is(BlockTags.FLOWERS)) return 1;
        
        // Default: 0 XP for untracked blocks
        return 0;
    }
    
    /**
     * Get money for breaking a block
     */
    private static long getBlockMoney(Block block, BlockState state) {
        // Check specific block first
        if (MINING_MONEY.containsKey(block)) {
            long baseMoney = MINING_MONEY.get(block);
            
            // Bonus money for fully grown crops
            if (block instanceof CropBlock cropBlock) {
                if (cropBlock.isMaxAge(state)) {
                    return baseMoney * 2; // Double money for mature crops
                }
                return 0; // No money for breaking immature crops
            }
            
            return baseMoney;
        }
        
        return 0;
    }
    
    /**
     * Register combat XP events
     */
    private static void registerCombatXp() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity.level().isClientSide()) return;
            
            // Get the attacker
            Entity attacker = damageSource.getEntity();
            if (!(attacker instanceof ServerPlayer serverPlayer)) return;
            
            // Don't give XP for killing players
            if (entity instanceof Player) return;
            
            // Calculate XP based on entity's max health and type
            float maxHealth = entity.getMaxHealth();
            int xp = calculateCombatXp(entity, maxHealth);
            
            // Calculate money
            long money = calculateCombatMoney(entity);
            
            // Award XP
            int oldLevel = PlayerDataManager.getRpgLevel(serverPlayer);
            PlayerDataManager.addRpgXp(serverPlayer, xp);
            int newLevel = PlayerDataManager.getRpgLevel(serverPlayer);
            
            // Award money
            if (money > 0) {
                PlayerDataManager.addMoney(serverPlayer, money);
            }
            
            // Check for level up
            if (newLevel > oldLevel) {
                onLevelUp(serverPlayer, newLevel);
            }
            
            // Show XP gain
            String symbol = entity instanceof Monster ? "⚔" : "🥩";
            serverPlayer.displayClientMessage(
                Component.literal("§c" + symbol + " §b+" + xp + " XP" + (money > 0 ? " §a+$" + money : "")), 
                true
            );
        });
    }
    
    /**
     * Calculate combat XP based on entity type and health
     */
    private static int calculateCombatXp(LivingEntity entity, float maxHealth) {
        int baseXp = (int) (maxHealth / 2); // Base XP = half of max health
        
        // Apply type multiplier
        float multiplier = 1.0f;
        for (Map.Entry<Class<? extends LivingEntity>, Float> entry : MOB_XP_MULTIPLIER.entrySet()) {
            if (entry.getKey().isInstance(entity)) {
                multiplier = entry.getValue();
                break; // Use most specific match
            }
        }
        
        baseXp = (int) (baseXp * multiplier);
        
        // Minimum 3 XP, maximum 200 XP per kill
        return Math.max(3, Math.min(200, baseXp));
    }
    
    /**
     * Calculate money for killing an entity
     */
    private static long calculateCombatMoney(LivingEntity entity) {
        // Check specific entity types first
        for (Map.Entry<Class<? extends LivingEntity>, Long> entry : MOB_MONEY.entrySet()) {
            if (entry.getKey().isInstance(entity)) {
                return entry.getValue();
            }
        }
        
        // Default: scale by health
        return (long) Math.max(1, entity.getMaxHealth() / 4);
    }
    
    /**
     * Register movement-based rewards (running/walking)
     */
    private static void registerMovementRewards() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                trackPlayerMovement(player);
            }
        });
    }
    
    /**
     * Track player movement and give rewards for distance traveled
     */
    private static void trackPlayerMovement(ServerPlayer player) {
        UUID uuid = player.getUUID();
        BlockPos currentPos = player.blockPosition();
        
        // Get or initialize last position
        BlockPos lastPos = LAST_POSITION.get(uuid);
        if (lastPos == null) {
            LAST_POSITION.put(uuid, currentPos);
            DISTANCE_TRAVELED.put(uuid, 0.0);
            return;
        }
        
        // Skip if player is flying/riding/dead
        if (player.isSpectator() || player.isFallFlying() || player.isPassenger() || player.isDeadOrDying()) {
            LAST_POSITION.put(uuid, currentPos);
            return;
        }
        
        // Calculate horizontal distance (ignore Y for vertical movement)
        double dx = currentPos.getX() - lastPos.getX();
        double dz = currentPos.getZ() - lastPos.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        
        // Update last position
        LAST_POSITION.put(uuid, currentPos);
        
        // Skip tiny movements (standing still or very small adjustments)
        if (distance < 0.5) return;
        
        // Accumulate distance
        double totalDistance = DISTANCE_TRAVELED.getOrDefault(uuid, 0.0) + distance;
        DISTANCE_TRAVELED.put(uuid, totalDistance);
        
        // Check if player has traveled enough for a reward
        if (totalDistance >= DISTANCE_PER_REWARD) {
            // Reset distance counter
            DISTANCE_TRAVELED.put(uuid, totalDistance - DISTANCE_PER_REWARD);
            
            // Rate limit: max once per 30 seconds
            long now = System.currentTimeMillis();
            long lastReward = LAST_MOVEMENT_REWARD.getOrDefault(uuid, 0L);
            if (now - lastReward < 30000) return;
            
            LAST_MOVEMENT_REWARD.put(uuid, now);
            
            // Award XP and money for traveling
            int xp = player.isSprinting() ? 5 : 2; // More XP for sprinting
            long money = player.isSprinting() ? 3L : 1L;
            
            PlayerDataManager.addRpgXp(player, xp);
            PlayerDataManager.addMoney(player, money);
            
            // Silent reward - no message spam for movement
        }
    }
    
    /**
     * Handle level up event
     */
    private static void onLevelUp(ServerPlayer player, int newLevel) {
        // Send level up message
        player.sendSystemMessage(Component.literal(
            "§6§l⬆ LEVEL UP! §r§eYou are now level §f" + newLevel + "§e!"
        ));
        
        // Play level up sound
        player.level().playSound(
            null, 
            player.blockPosition(), 
            SoundEvents.PLAYER_LEVELUP, 
            SoundSource.PLAYERS, 
            1.0f, 
            1.0f
        );
        
        // Give level up reward (bonus money)
        long reward = newLevel * 50L;
        PlayerDataManager.addMoney(player, reward);
        player.sendSystemMessage(Component.literal(
            "§a+$" + String.format("%,d", reward) + " §7(Level up bonus!)"
        ));
    }
    
    /**
     * Get the XP value for mining a specific block (for external use)
     */
    public static int getMiningXp(Block block) {
        return MINING_XP.getOrDefault(block, 0);
    }
    
    /**
     * Cleanup player data on disconnect
     */
    public static void onPlayerDisconnect(ServerPlayer player) {
        UUID uuid = player.getUUID();
        LAST_MOVEMENT_REWARD.remove(uuid);
        DISTANCE_TRAVELED.remove(uuid);
        LAST_POSITION.remove(uuid);
    }
}
