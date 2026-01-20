# ⚡ Commands - Minecraft 1.21.11 Fabric

**Complete command registration guide using Brigadier with 1.21.11 API**

> ✅ **Production-Tested**: Command patterns from Pocket Life mod (/pocket, /p aliases).
> 
> ⚠️ **API Change in 1.21.11**: Uses `CommandSourceStack` in some contexts.

---

## Table of Contents

1. [Basic Command Registration](#basic)
2. [Command Arguments](#arguments)
3. [Permissions & Requirements](#permissions)
4. [Subcommands](#subcommands)
5. [Player Targeting](#targeting)
6. [Error Handling](#errors)
7. [Complete Examples](#examples)

---

## <a id="basic"></a>Basic Command Registration

### Essential Imports

```java
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
```

### Simple Command (No Arguments)

```java
public class ModInitializer implements ModInitializer {
    
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            dispatcher.register(Commands.literal("hello")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.sendSystemMessage(Component.literal("§aHello, world!"));
                    return 1;  // Success
                })
            );
        });
    }
}
```

### Command with Description (for `/help`)

```java
dispatcher.register(Commands.literal("mymod")
    .executes(context -> {
        ServerPlayer player = context.getSource().getPlayerOrException();
        player.sendSystemMessage(Component.literal("§6MyMod v1.0.0"));
        player.sendSystemMessage(Component.literal("§7Use /mymod help for commands"));
        return 1;
    })
);
```

---

## <a id="arguments"></a>Command Arguments

### Integer Arguments

```java
import com.mojang.brigadier.arguments.IntegerArgumentType;

dispatcher.register(Commands.literal("setlevel")
    .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
        .executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            int level = IntegerArgumentType.getInteger(context, "level");
            
            player.sendSystemMessage(Component.literal("§aLevel set to: " + level));
            return 1;
        })
    )
);

// Usage: /setlevel 50
```

### String Arguments

```java
import com.mojang.brigadier.arguments.StringArgumentType;

// Single word
dispatcher.register(Commands.literal("greet")
    .then(Commands.argument("name", StringArgumentType.word())
        .executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(context, "name");
            
            player.sendSystemMessage(Component.literal("§aHello, " + name + "!"));
            return 1;
        })
    )
);

// Usage: /greet Steve

// Multiple words (greedy string - rest of command)
dispatcher.register(Commands.literal("announce")
    .then(Commands.argument("message", StringArgumentType.greedyString())
        .executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String message = StringArgumentType.getString(context, "message");
            
            context.getSource().getServer().getPlayerList().getPlayers()
                .forEach(p -> p.sendSystemMessage(
                    Component.literal("§6[Announcement] §f" + message)
                ));
            return 1;
        })
    )
);

// Usage: /announce This is a long message with spaces
```

### Boolean Arguments

```java
import com.mojang.brigadier.arguments.BoolArgumentType;

dispatcher.register(Commands.literal("toggle")
    .then(Commands.argument("enabled", BoolArgumentType.bool())
        .executes(context -> {
            boolean enabled = BoolArgumentType.getBool(context, "enabled");
            // Use enabled value...
            return 1;
        })
    )
);

// Usage: /toggle true
```

### Double/Float Arguments

```java
import com.mojang.brigadier.arguments.DoubleArgumentType;

dispatcher.register(Commands.literal("setspeed")
    .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.1, 10.0))
        .executes(context -> {
            double speed = DoubleArgumentType.getDouble(context, "multiplier");
            // Apply speed multiplier...
            return 1;
        })
    )
);
```

### Multiple Arguments

```java
dispatcher.register(Commands.literal("give")
    .then(Commands.argument("item", StringArgumentType.word())
        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                String item = StringArgumentType.getString(context, "item");
                int amount = IntegerArgumentType.getInteger(context, "amount");
                
                player.sendSystemMessage(Component.literal(
                    "§aGiving " + amount + "x " + item
                ));
                return 1;
            })
        )
    )
);

// Usage: /give diamond 32
```

---

## <a id="permissions"></a>Permissions & Requirements

### Require Permission Level

```java
dispatcher.register(Commands.literal("admin")
    .requires(source -> source.hasPermission(2))  // OP level 2
    .executes(context -> {
        // Only ops can run this
        return 1;
    })
);

// Permission levels:
// 0 = Everyone
// 1 = Bypass spawn protection (default for ops)
// 2 = /clear, /effect, /gamemode, /tp
// 3 = /ban, /kick, /op
// 4 = /stop
```

### Require Player (Not Console)

```java
dispatcher.register(Commands.literal("playeronly")
    .requires(source -> source.isPlayer())
    .executes(context -> {
        ServerPlayer player = context.getSource().getPlayerOrException();
        // This cannot be run from console
        return 1;
    })
);
```

### Custom Requirements

```java
dispatcher.register(Commands.literal("vip")
    .requires(source -> {
        if (!source.isPlayer()) return false;
        
        try {
            ServerPlayer player = source.getPlayerOrException();
            // Check custom permission
            return hasVIPPermission(player);
        } catch (CommandSyntaxException e) {
            return false;
        }
    })
    .executes(context -> {
        // VIP-only command
        return 1;
    })
);
```

---

## <a id="subcommands"></a>Subcommands

### Multiple Subcommands

```java
dispatcher.register(Commands.literal("shop")
    .then(Commands.literal("buy")
        .then(Commands.argument("item", StringArgumentType.word())
            .executes(context -> {
                String item = StringArgumentType.getString(context, "item");
                // Handle purchase
                return 1;
            })
        )
    )
    .then(Commands.literal("sell")
        .then(Commands.argument("item", StringArgumentType.word())
            .executes(context -> {
                String item = StringArgumentType.getString(context, "item");
                // Handle sale
                return 1;
            })
        )
    )
    .then(Commands.literal("list")
        .executes(context -> {
            // Show shop inventory
            return 1;
        })
    )
    .executes(context -> {
        // Default: show help
        ServerPlayer player = context.getSource().getPlayerOrException();
        player.sendSystemMessage(Component.literal("§6=== Shop Commands ==="));
        player.sendSystemMessage(Component.literal("§7/shop buy <item>"));
        player.sendSystemMessage(Component.literal("§7/shop sell <item>"));
        player.sendSystemMessage(Component.literal("§7/shop list"));
        return 1;
    })
);

// Usage:
// /shop           -> Shows help
// /shop buy diamond
// /shop sell gold
// /shop list
```

### Deeply Nested Subcommands

```java
dispatcher.register(Commands.literal("farm")
    .then(Commands.literal("plots")
        .then(Commands.literal("buy")
            .executes(context -> {
                // Buy a plot
                return 1;
            })
        )
        .then(Commands.literal("list")
            .executes(context -> {
                // List plots
                return 1;
            })
        )
    )
    .then(Commands.literal("crops")
        .then(Commands.literal("plant")
            .then(Commands.argument("crop", StringArgumentType.word())
                .executes(context -> {
                    String crop = StringArgumentType.getString(context, "crop");
                    // Plant crop
                    return 1;
                })
            )
        )
        .then(Commands.literal("harvest")
            .executes(context -> {
                // Harvest all
                return 1;
            })
        )
    )
);

// Usage:
// /farm plots buy
// /farm plots list
// /farm crops plant wheat
// /farm crops harvest
```

---

## <a id="targeting"></a>Player Targeting

### Target Self

```java
dispatcher.register(Commands.literal("stats")
    .executes(context -> {
        ServerPlayer player = context.getSource().getPlayerOrException();
        // Show player's stats
        return 1;
    })
);
```

### Target Other Player

```java
import net.minecraft.commands.arguments.EntityArgument;

dispatcher.register(Commands.literal("heal")
    .then(Commands.argument("target", EntityArgument.player())
        .requires(source -> source.hasPermission(2))
        .executes(context -> {
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            target.setHealth(target.getMaxHealth());
            target.sendSystemMessage(Component.literal("§aYou have been healed!"));
            return 1;
        })
    )
    .executes(context -> {
        // No argument: heal self
        ServerPlayer player = context.getSource().getPlayerOrException();
        player.setHealth(player.getMaxHealth());
        player.sendSystemMessage(Component.literal("§aYou healed yourself!"));
        return 1;
    })
);

// Usage:
// /heal          -> Heals self
// /heal Steve    -> Heals Steve
```

### Broadcast to All Players

```java
dispatcher.register(Commands.literal("broadcast")
    .then(Commands.argument("message", StringArgumentType.greedyString())
        .executes(context -> {
            String message = StringArgumentType.getString(context, "message");
            
            context.getSource().getServer().getPlayerList().getPlayers()
                .forEach(player -> {
                    player.sendSystemMessage(
                        Component.literal("§6[Broadcast] §f" + message)
                    );
                });
            
            return 1;
        })
    )
);
```

---

## <a id="errors"></a>Error Handling

### Try-Catch Pattern

```java
dispatcher.register(Commands.literal("risky")
    .executes(context -> {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            // Risky operation
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    })
);
```

### Custom Error Messages

```java
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

private static final SimpleCommandExceptionType NO_PERMISSION = 
    new SimpleCommandExceptionType(Component.literal("§cYou don't have permission!"));

dispatcher.register(Commands.literal("restricted")
    .executes(context -> {
        ServerPlayer player = context.getSource().getPlayerOrException();
        
        if (!hasPermission(player)) {
            throw NO_PERMISSION.create();
        }
        
        // Command logic
        return 1;
    })
);
```

### Validation Errors

```java
dispatcher.register(Commands.literal("buy")
    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
        .executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            int amount = IntegerArgumentType.getInteger(context, "amount");
            
            if (amount > 64) {
                player.sendSystemMessage(Component.literal("§cCan't buy more than 64 at once!"));
                return 0;
            }
            
            if (!hasEnoughMoney(player, amount)) {
                player.sendSystemMessage(Component.literal("§cNot enough money!"));
                return 0;
            }
            
            // Process purchase
            player.sendSystemMessage(Component.literal("§aPurchased " + amount + " items!"));
            return 1;
        })
    )
);
```

---

## <a id="examples"></a>Complete Examples

### Example 1: Economy System

```java
public class EconomyCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("balance")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                int balance = EconomyManager.getBalance(player);
                player.sendSystemMessage(Component.literal("§eBalance: §a$" + balance));
                return 1;
            })
        );
        
        dispatcher.register(Commands.literal("pay")
            .then(Commands.argument("target", EntityArgument.player())
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ServerPlayer target = EntityArgument.getPlayer(context, "target");
                        int amount = IntegerArgumentType.getInteger(context, "amount");
                        
                        if (player.getUUID().equals(target.getUUID())) {
                            player.sendSystemMessage(Component.literal("§cCan't pay yourself!"));
                            return 0;
                        }
                        
                        if (!EconomyManager.hasBalance(player, amount)) {
                            player.sendSystemMessage(Component.literal("§cInsufficient funds!"));
                            return 0;
                        }
                        
                        EconomyManager.removeMoney(player, amount);
                        EconomyManager.addMoney(target, amount);
                        
                        player.sendSystemMessage(Component.literal("§aPaid $" + amount + " to " + target.getName().getString()));
                        target.sendSystemMessage(Component.literal("§aReceived $" + amount + " from " + player.getName().getString()));
                        
                        return 1;
                    })
                )
            )
        );
    }
}
```

### Example 2: Teleport System

```java
public class TeleportCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawn")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                ServerLevel world = player.serverLevel();
                
                // Get spawn position
                BlockPos spawn = world.getSharedSpawnPos();
                
                player.teleportTo(world, 
                    spawn.getX() + 0.5, 
                    spawn.getY(), 
                    spawn.getZ() + 0.5, 
                    0, 0);
                
                player.sendSystemMessage(Component.literal("§aTeleported to spawn!"));
                return 1;
            })
        );
        
        dispatcher.register(Commands.literal("tpa")
            .then(Commands.argument("target", EntityArgument.player())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerPlayer target = EntityArgument.getPlayer(context, "target");
                    
                    // Send request
                    TeleportManager.createRequest(player, target);
                    
                    player.sendSystemMessage(Component.literal("§aTeleport request sent to " + target.getName().getString()));
                    target.sendSystemMessage(Component.literal("§e" + player.getName().getString() + " wants to teleport to you. Use /tpaccept to accept."));
                    
                    return 1;
                })
            )
        );
        
        dispatcher.register(Commands.literal("tpaccept")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                
                if (!TeleportManager.hasRequest(player)) {
                    player.sendSystemMessage(Component.literal("§cNo pending teleport requests!"));
                    return 0;
                }
                
                ServerPlayer requester = TeleportManager.acceptRequest(player);
                
                // Teleport requester to accepter
                requester.teleportTo(player.serverLevel(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYRot(),
                    player.getXRot());
                
                requester.sendSystemMessage(Component.literal("§aTeleported to " + player.getName().getString() + "!"));
                player.sendSystemMessage(Component.literal("§a" + requester.getName().getString() + " teleported to you!"));
                
                return 1;
            })
        );
    }
}
```

### Example 3: Admin Tools

```java
public class AdminCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fly")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                
                boolean canFly = !player.getAbilities().mayfly;
                player.getAbilities().mayfly = canFly;
                player.getAbilities().flying = canFly && player.getAbilities().flying;
                player.onUpdateAbilities();
                
                player.sendSystemMessage(Component.literal(
                    canFly ? "§aFlight enabled!" : "§cFlight disabled!"
                ));
                
                return 1;
            })
        );
        
        dispatcher.register(Commands.literal("clear")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("target", EntityArgument.player())
                .executes(context -> {
                    ServerPlayer target = EntityArgument.getPlayer(context, "target");
                    target.getInventory().clearContent();
                    target.sendSystemMessage(Component.literal("§cYour inventory has been cleared!"));
                    return 1;
                })
            )
        );
    }
}
```

---

## Best Practices

✅ Always return 1 for success, 0 for failure
✅ Use `.requires()` for permission checks
✅ Provide helpful error messages
✅ Add default `.executes()` for help text
✅ Use `getPlayerOrException()` for player commands
✅ Validate input before processing

---

## Next Steps

Continue to:
- [03_GUI_SYSTEMS.md](03_GUI_SYSTEMS.md) - Open GUIs from commands
- [05_DATA_STORAGE.md](05_DATA_STORAGE.md) - Save command data
- [08_PATTERNS.md](08_PATTERNS.md) - More command patterns

---

**Version Note:** All code is for Minecraft 1.21.11 with Mojang mappings.
