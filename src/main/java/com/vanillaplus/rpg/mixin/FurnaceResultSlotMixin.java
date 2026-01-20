package com.vanillaplus.rpg.mixin;

import com.vanillaplus.rpg.data.PlayerDataManager;
import com.vanillaplus.rpg.network.PlayerDataSyncHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixin to give Smithing skill XP and money when player takes items from furnace output
 */
@Mixin(FurnaceResultSlot.class)
public abstract class FurnaceResultSlotMixin {
    
    @Shadow @Final
    private Player player;
    
    // Smelting XP rewards (Smithing skill)
    @Unique
    private static final Map<Item, Integer> vanillaplusrpg$smeltingXp = new HashMap<>();
    
    // Smelting money rewards
    @Unique
    private static final Map<Item, Long> vanillaplusrpg$smeltingMoney = new HashMap<>();
    
    static {
        // Iron/Copper/Gold ingots from smelting ores
        vanillaplusrpg$smeltingXp.put(Items.IRON_INGOT, 3);
        vanillaplusrpg$smeltingXp.put(Items.COPPER_INGOT, 2);
        vanillaplusrpg$smeltingXp.put(Items.GOLD_INGOT, 5);
        vanillaplusrpg$smeltingXp.put(Items.NETHERITE_SCRAP, 15);
        
        // Glass, bricks, and stone products
        vanillaplusrpg$smeltingXp.put(Items.GLASS, 1);
        vanillaplusrpg$smeltingXp.put(Items.BRICK, 1);
        vanillaplusrpg$smeltingXp.put(Items.NETHER_BRICK, 1);
        vanillaplusrpg$smeltingXp.put(Items.SMOOTH_STONE, 1);
        vanillaplusrpg$smeltingXp.put(Items.STONE, 1);
        vanillaplusrpg$smeltingXp.put(Items.CHARCOAL, 1);
        vanillaplusrpg$smeltingXp.put(Items.TERRACOTTA, 1);
        vanillaplusrpg$smeltingXp.put(Items.DEEPSLATE, 1);
        vanillaplusrpg$smeltingXp.put(Items.SMOOTH_SANDSTONE, 1);
        vanillaplusrpg$smeltingXp.put(Items.SMOOTH_RED_SANDSTONE, 1);
        vanillaplusrpg$smeltingXp.put(Items.SMOOTH_QUARTZ, 1);
        vanillaplusrpg$smeltingXp.put(Items.SMOOTH_BASALT, 1);
        vanillaplusrpg$smeltingXp.put(Items.CRACKED_STONE_BRICKS, 1);
        vanillaplusrpg$smeltingXp.put(Items.CRACKED_NETHER_BRICKS, 1);
        vanillaplusrpg$smeltingXp.put(Items.CRACKED_DEEPSLATE_BRICKS, 1);
        vanillaplusrpg$smeltingXp.put(Items.CRACKED_DEEPSLATE_TILES, 1);
        vanillaplusrpg$smeltingXp.put(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS, 1);
        vanillaplusrpg$smeltingXp.put(Items.SPONGE, 2);
        vanillaplusrpg$smeltingXp.put(Items.LIME_DYE, 1); // from sea pickle
        vanillaplusrpg$smeltingXp.put(Items.GREEN_DYE, 1); // from cactus
        vanillaplusrpg$smeltingXp.put(Items.DRIED_KELP, 1);
        vanillaplusrpg$smeltingXp.put(Items.POPPED_CHORUS_FRUIT, 2);
        
        // Money rewards - only for valuable smelted items
        vanillaplusrpg$smeltingMoney.put(Items.IRON_INGOT, 2L);
        vanillaplusrpg$smeltingMoney.put(Items.COPPER_INGOT, 1L);
        vanillaplusrpg$smeltingMoney.put(Items.GOLD_INGOT, 5L);
        vanillaplusrpg$smeltingMoney.put(Items.NETHERITE_SCRAP, 50L);
        vanillaplusrpg$smeltingMoney.put(Items.GLASS, 1L);
        vanillaplusrpg$smeltingMoney.put(Items.BRICK, 1L);
        vanillaplusrpg$smeltingMoney.put(Items.SMOOTH_STONE, 1L);
        vanillaplusrpg$smeltingMoney.put(Items.CHARCOAL, 1L);
    }
    
    /**
     * Called when player takes result from furnace
     */
    @Inject(method = "onTake", at = @At("HEAD"))
    private void vanillaplusrpg$onTakeResult(Player player, ItemStack stack, CallbackInfo ci) {
        if (player.level().isClientSide() || stack.isEmpty()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        Item item = stack.getItem();
        int count = stack.getCount();
        
        // Check if this item gives smithing XP
        Integer xpPerItem = vanillaplusrpg$smeltingXp.get(item);
        Long moneyPerItem = vanillaplusrpg$smeltingMoney.get(item);
        
        if (xpPerItem == null && moneyPerItem == null) return;
        
        // Calculate rewards based on count
        int baseXp = xpPerItem != null ? xpPerItem * count : 0;
        long baseMoney = moneyPerItem != null ? moneyPerItem * count : 0;
        
        // Apply smithing level bonus (+5% per level)
        int smithingLevel = PlayerDataManager.getSkillLevel(serverPlayer, PlayerDataManager.Skill.SMITHING);
        double bonus = 1.0 + (smithingLevel * 0.05);
        
        int finalXp = (int) Math.ceil(baseXp * bonus);
        long finalMoney = (long) Math.ceil(baseMoney * bonus);
        
        // Grant money
        if (finalMoney > 0) {
            PlayerDataManager.addMoney(serverPlayer, finalMoney);
        }
        
        // Grant vanilla Minecraft XP (used for enchanting, displayed in notification)
        if (finalXp > 0) {
            serverPlayer.giveExperiencePoints(finalXp);
        }
        
        // Send notification - use reward type 4 for smelting
        if (finalXp > 0 || finalMoney > 0) {
            try {
                PlayerDataSyncHandler.RewardNotificationPayload payload = 
                    new PlayerDataSyncHandler.RewardNotificationPayload(finalXp, finalMoney, 0, 4);
                ServerPlayNetworking.send(serverPlayer, payload);
                
                // Also send to chat for longer visibility
                String itemName = BuiltInRegistries.ITEM.getKey(item).getPath();
                String msg = String.format("§d🔥 Smelted %dx %s: §d+%d XP §a+$%d", count, itemName, finalXp, finalMoney);
                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(msg));
            } catch (Exception e) {
                // Silently fail if networking fails
            }
        }
    }
}
