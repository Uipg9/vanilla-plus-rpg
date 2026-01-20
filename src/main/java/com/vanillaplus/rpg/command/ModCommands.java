package com.vanillaplus.rpg.command;

import com.vanillaplus.rpg.VanillaPlusRpg;
import com.vanillaplus.rpg.data.PlayerDataManager;
import com.vanillaplus.rpg.economy.ItemPricing;
import com.vanillaplus.rpg.economy.MarketManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Command registration for the mod
 * 
 * UPDATED: Client-side screens, server-side buy/sell commands
 */
public class ModCommands {
    
    /**
     * Check if source has operator permission
     * Fixed for 1.21.11: CommandSourceStack.getServer() can be null during registration
     * Also CommandSourceStack.hasPermission() doesn't exist in 1.21.11
     */
    private static boolean hasOpPermission(CommandSourceStack source) {
        // During command registration, getServer() may be null
        if (source.getServer() == null) {
            return true; // Allow during registration phase
        }
        
        // In single player, always allow
        if (source.getServer().isSingleplayer()) {
            return true;
        }
        
        // Check if the source is a player
        try {
            ServerPlayer player = source.getPlayerOrException();
            // In 1.21.11, use game mode as proxy for op status
            return player.isCreative() || player.isSpectator();
        } catch (Exception e) {
            // Console/command block - check using source permission level
            // source.getEntity() being null indicates console
            return source.getEntity() == null;
        }
    }
    
    /**
     * Register all commands
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            
            // /hub - Just show a message (screen opens client-side)
            dispatcher.register(Commands.literal("hub")
                .executes(context -> {
                    // Hub screen is opened client-side via H key
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.sendSystemMessage(Component.literal("§7Press §eH §7to open the Hub menu!"));
                    return 1;
                })
            );
            
            // /shop - Just show a message (screen opens client-side)
            dispatcher.register(Commands.literal("shop")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.sendSystemMessage(Component.literal("§7Use the §eH §7key to open the Hub menu!"));
                    return 1;
                })
            );
            
            // /buy <item> [amount] - Buy items from shop
            dispatcher.register(Commands.literal("buy")
                .then(Commands.argument("item", StringArgumentType.string())
                    .executes(context -> buyItem(context.getSource(), 
                        StringArgumentType.getString(context, "item"), 1))
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                        .executes(context -> buyItem(context.getSource(),
                            StringArgumentType.getString(context, "item"),
                            IntegerArgumentType.getInteger(context, "amount")))
                    )
                )
            );
            
            // /sell [item] [amount] - Sell items
            dispatcher.register(Commands.literal("sell")
                .executes(context -> sellHeldItem(context.getSource(), -1)) // -1 = all
                .then(Commands.literal("all")
                    .executes(context -> sellAllItems(context.getSource())))
                .then(Commands.argument("item", StringArgumentType.string())
                    .executes(context -> sellItem(context.getSource(),
                        StringArgumentType.getString(context, "item"), -1))
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(context -> sellItem(context.getSource(),
                            StringArgumentType.getString(context, "item"),
                            IntegerArgumentType.getInteger(context, "amount")))
                    )
                )
            );
            
            // /balance or /bal - Show player's balance
            dispatcher.register(Commands.literal("balance")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    showBalance(player);
                    return 1;
                })
            );
            dispatcher.register(Commands.literal("bal")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    showBalance(player);
                    return 1;
                })
            );
            
            // /stats - Show player's RPG stats
            dispatcher.register(Commands.literal("stats")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    showStats(player);
                    return 1;
                })
            );
            
            // /market - Show current market status
            dispatcher.register(Commands.literal("market")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    showMarket(player);
                    return 1;
                })
            );
            
            // /daily - Show daily earnings report
            dispatcher.register(Commands.literal("daily")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    showDailyReport(player);
                    return 1;
                })
            );
            
            // Admin commands (OP only)
            dispatcher.register(Commands.literal("rpgadmin")
                .requires(ModCommands::hasOpPermission)
                .then(Commands.literal("setmoney")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            int amount = IntegerArgumentType.getInteger(context, "amount");
                            PlayerDataManager.setMoney(player, amount);
                            player.sendSystemMessage(Component.literal(
                                "§aSet your money to $" + amount
                            ));
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("setlevel")
                    .then(Commands.argument("level", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            int level = IntegerArgumentType.getInteger(context, "level");
                            PlayerDataManager.setRpgLevel(player, level);
                            PlayerDataManager.setRpgXp(player, 0);
                            player.sendSystemMessage(Component.literal(
                                "§aSet your level to " + level
                            ));
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("addxp")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            int amount = IntegerArgumentType.getInteger(context, "amount");
                            PlayerDataManager.addRpgXp(player, amount);
                            player.sendSystemMessage(Component.literal(
                                "§aAdded " + amount + " XP"
                            ));
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("addmoney")
                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            int amount = IntegerArgumentType.getInteger(context, "amount");
                            PlayerDataManager.addMoney(player, amount);
                            player.sendSystemMessage(Component.literal(
                                "§aAdded $" + amount + " to your balance"
                            ));
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("rotatemarket")
                    .executes(context -> {
                        MarketManager.rotateMarket(context.getSource().getServer());
                        context.getSource().sendSuccess(
                            () -> Component.literal("§aMarket rotated!"),
                            true
                        );
                        return 1;
                    })
                )
            );
        });
        
        VanillaPlusRpg.LOGGER.info("Commands registered");
    }
    
    /**
     * Buy item command handler
     */
    private static int buyItem(CommandSourceStack source, String itemName, int amount) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            
            // Find item by name
            Item item = findItem(itemName);
            if (item == null || item == Items.AIR) {
                player.sendSystemMessage(Component.literal("§cUnknown item: " + itemName));
                return 0;
            }
            
            // Check if item is buyable
            if (!ItemPricing.canBuy(item)) {
                player.sendSystemMessage(Component.literal("§cThat item is not for sale!"));
                return 0;
            }
            
            long price = ItemPricing.getBuyPrice(item);
            long totalCost = price * amount;
            long playerMoney = PlayerDataManager.getMoney(player);
            
            // Check if player can afford
            if (playerMoney < totalCost) {
                player.sendSystemMessage(Component.literal(
                    "§cNot enough money! Need §6$" + totalCost + " §c(you have §6$" + playerMoney + "§c)"
                ));
                return 0;
            }
            
            // Deduct money and give items
            PlayerDataManager.addMoney(player, -totalCost);
            player.getInventory().add(new ItemStack(item, amount));
            
            String itemDisplayName = item.getName(item.getDefaultInstance()).getString();
            player.sendSystemMessage(Component.literal(
                "§a✓ Purchased " + amount + "x " + itemDisplayName + " for §6$" + totalCost
            ));
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError processing purchase"));
            return 0;
        }
    }
    
    /**
     * Sell item command handler
     */
    private static int sellItem(CommandSourceStack source, String itemName, int amount) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            
            // Find item by name
            Item item = findItem(itemName);
            if (item == null || item == Items.AIR) {
                player.sendSystemMessage(Component.literal("§cUnknown item: " + itemName));
                return 0;
            }
            
            // Count how many player has
            int available = countItemInInventory(player, item);
            if (available == 0) {
                player.sendSystemMessage(Component.literal("§cYou don't have any of that item!"));
                return 0;
            }
            
            int toSell = amount == -1 ? available : Math.min(amount, available);
            
            // Get sell price
            long price = ItemPricing.getSellPrice(item);
            if (price <= 0) {
                // Default sell price for items not in the list
                price = 1;
            }
            
            long totalEarned = price * toSell;
            
            // Remove items and add money
            removeItemFromInventory(player, item, toSell);
            PlayerDataManager.addMoney(player, totalEarned);
            PlayerDataManager.addToDailyEarnings(player, totalEarned);
            
            String itemDisplayName = item.getName(item.getDefaultInstance()).getString();
            player.sendSystemMessage(Component.literal(
                "§a✓ Sold " + toSell + "x " + itemDisplayName + " for §6$" + totalEarned
            ));
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError processing sale"));
            return 0;
        }
    }
    
    /**
     * Sell item in hand
     */
    private static int sellHeldItem(CommandSourceStack source, int amount) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.isEmpty()) {
                player.sendSystemMessage(Component.literal("§cYou're not holding anything!"));
                return 0;
            }
            
            Item item = heldItem.getItem();
            int available = heldItem.getCount();
            int toSell = amount == -1 ? available : Math.min(amount, available);
            
            // Get sell price
            long price = ItemPricing.getSellPrice(item);
            if (price <= 0) {
                price = 1; // Default price
            }
            
            long totalEarned = price * toSell;
            
            // Remove from hand
            heldItem.shrink(toSell);
            
            // Add money
            PlayerDataManager.addMoney(player, totalEarned);
            PlayerDataManager.addToDailyEarnings(player, totalEarned);
            
            String itemDisplayName = item.getName(item.getDefaultInstance()).getString();
            player.sendSystemMessage(Component.literal(
                "§a✓ Sold " + toSell + "x " + itemDisplayName + " for §6$" + totalEarned
            ));
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError processing sale"));
            return 0;
        }
    }
    
    /**
     * Sell all sellable items in inventory
     */
    private static int sellAllItems(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            
            long totalEarned = 0;
            int totalItems = 0;
            
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                
                Item item = stack.getItem();
                long price = ItemPricing.getSellPrice(item);
                
                if (price > 0) {
                    int count = stack.getCount();
                    totalEarned += price * count;
                    totalItems += count;
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
            
            if (totalItems == 0) {
                player.sendSystemMessage(Component.literal("§cYou don't have any sellable items!"));
                return 0;
            }
            
            PlayerDataManager.addMoney(player, totalEarned);
            PlayerDataManager.addToDailyEarnings(player, totalEarned);
            
            player.sendSystemMessage(Component.literal(
                "§a§l✓ SOLD ALL! §aSold " + totalItems + " items for §6$" + totalEarned
            ));
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError processing sale"));
            return 0;
        }
    }
    
    /**
     * Find item by name (supports minecraft: prefix and partial names)
     */
    private static Item findItem(String name) {
        for (Item item : BuiltInRegistries.ITEM) {
            String key = BuiltInRegistries.ITEM.getKey(item).toString();
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            
            if (key.equals(name) || key.equals("minecraft:" + name) || path.equals(name)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Count how many of an item the player has
     */
    private static int countItemInInventory(ServerPlayer player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }
    
    /**
     * Remove items from inventory
     */
    private static int removeItemFromInventory(ServerPlayer player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
        return amount - remaining;
    }
    
    /**
     * Show player's balance
     */
    private static void showBalance(ServerPlayer player) {
        long money = PlayerDataManager.getMoney(player);
        player.sendSystemMessage(Component.literal(
            "§6[Balance] §aYou have §f$%,d".formatted(money)
        ));
    }
    
    /**
     * Show player's RPG stats
     */
    private static void showStats(ServerPlayer player) {
        int level = PlayerDataManager.getRpgLevel(player);
        int xp = PlayerDataManager.getRpgXp(player);
        int xpRequired = PlayerDataManager.getXpRequired(level);
        long money = PlayerDataManager.getMoney(player);
        
        player.sendSystemMessage(Component.literal("§6═══ Your Stats ═══"));
        player.sendSystemMessage(Component.literal("§eLevel: §f" + level));
        player.sendSystemMessage(Component.literal("§eXP: §f%d/%d §7(%.1f%%)".formatted(
            xp, xpRequired, (float)xp/xpRequired * 100
        )));
        player.sendSystemMessage(Component.literal("§eMoney: §a$%,d".formatted(money)));
        player.sendSystemMessage(Component.literal("§6═════════════════"));
    }
    
    /**
     * Show current market status
     */
    private static void showMarket(ServerPlayer player) {
        String hotItem = MarketManager.getHotItemName();
        String cheapItem = MarketManager.getCheapItemName();
        
        player.sendSystemMessage(Component.literal("§6═══ Market Status ═══"));
        player.sendSystemMessage(Component.literal(
            "§e🔥 Hot Item: §f" + hotItem + " §7(2x sell price)"
        ));
        player.sendSystemMessage(Component.literal(
            "§a💰 On Sale: §f" + cheapItem + " §7(50% off buy)"
        ));
        player.sendSystemMessage(Component.literal("§6═══════════════════"));
    }
    
    /**
     * Show daily earnings report
     */
    private static void showDailyReport(ServerPlayer player) {
        long dailyEarnings = PlayerDataManager.getDailyEarnings(player);
        
        player.sendSystemMessage(Component.literal("§6═══ Daily Report ═══"));
        if (dailyEarnings > 0) {
            player.sendSystemMessage(Component.literal(
                "§eToday's Earnings: §a$%,d".formatted(dailyEarnings)
            ));
        } else {
            player.sendSystemMessage(Component.literal(
                "§7You haven't earned anything today."
            ));
            player.sendSystemMessage(Component.literal(
                "§7Use §f/sell §7to sell items!"
            ));
        }
        player.sendSystemMessage(Component.literal("§6═══════════════════"));
    }
}
