# 🎯 Core API Reference - Minecraft 1.21.11 Fabric

**BREAKING CHANGES IN 1.21.11** - Critical information for mod development.

---

## Table of Contents

1. [Mappings (Mojang vs Yarn)](#mappings)
2. [Breaking API Changes](#breaking-changes)
3. [NBT Optional Returns](#nbt-optional)
4. [Component API](#component-api)
5. [Enchantment System](#enchantment-system)
6. [Item Changes](#item-changes)
7. [Common Imports](#common-imports)

---

## <a id="mappings"></a>Mappings: Mojang vs Yarn

### ⚠️ USE MOJANG MAPPINGS FOR 1.21.11

**build.gradle:**
```gradle
dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings loom.officialMojangMappings()  // ✅ CORRECT
    // mappings "net.fabricmc:yarn:..."     // ❌ WRONG FOR 1.21.11
}
```

### Naming Convention Comparison

| Yarn (OLD) | Mojang (1.21.11) | Description |
|------------|------------------|-------------|
| `PlayerEntity` | `ServerPlayer` / `Player` | Player object |
| `ServerWorld` | `ServerLevel` | Server world |
| `ItemStack` | `ItemStack` | Same |
| `NbtCompound` | `CompoundTag` | NBT data |
| `Text` | `Component` | Chat/text components |
| `CommandContext<ServerCommandSource>` | `CommandContext<CommandSourceStack>` | Command context |

---

## <a id="breaking-changes"></a>Breaking API Changes in 1.21.11

### Critical Changes Table

| Area | Old API | 1.21.11 API | Impact |
|------|---------|-------------|--------|
| **NBT Tags** | `.getInt("key")` returns `0` if missing | `.getInt("key")` returns `0` (no change), but **`.get("key")` returns `Optional<Tag>`** | Must handle Optional |
| **Enchantments** | Direct `Enchantment` objects | Registry-based `Holder<Enchantment>` | Complete rewrite needed |
| **Component Ternary** | `Text.literal(cond ? "a" : "b")` | Must parenthesize: `Component.literal((cond ? "a" : "b"))` | Compilation errors |
| **Items** | `Items.WATCH` | Removed, use `Items.CLOCK` | Replace all usages |
| **Command Source** | `ServerCommandSource` | `CommandSourceStack` | Update imports |

---

## <a id="nbt-optional"></a>NBT Optional Returns (BREAKING CHANGE)

### The Problem

In 1.21.11, methods like `.get()` return `Optional<Tag>` instead of nullable `Tag`.

### ❌ Old Code (Breaks in 1.21.11)

```java
CompoundTag nbt = itemStack.getTag();
if (nbt != null && nbt.contains("customKey")) {
    String value = nbt.getString("customKey");
}
```

### ✅ New Code (1.21.11 Compatible)

```java
itemStack.getOrCreateTag();  // Safe access
CompoundTag nbt = itemStack.getTag();

if (nbt.contains("customKey")) {
    String value = nbt.getString("customKey");
}
```

### Safe NBT Access Pattern

```java
// Reading with defaults
public int readValue(CompoundTag nbt, String key, int defaultValue) {
    return nbt.contains(key) ? nbt.getInt(key) : defaultValue;
}

// Writing safely
public void writeValue(ItemStack stack, String key, int value) {
    CompoundTag nbt = stack.getOrCreateTag();
    nbt.putInt(key, value);
}
```

### Complete NBT Example

```java
public class NBTHelper {
    
    // Save player data
    public static void savePlayerData(ServerPlayer player, String key, int value) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag modData = nbt.getCompound("YourModID");
        modData.putInt(key, value);
        nbt.put("YourModID", modData);
    }
    
    // Load player data with default
    public static int loadPlayerData(ServerPlayer player, String key, int defaultValue) {
        CompoundTag nbt = player.getPersistentData();
        if (nbt.contains("YourModID")) {
            CompoundTag modData = nbt.getCompound("YourModID");
            return modData.contains(key) ? modData.getInt(key) : defaultValue;
        }
        return defaultValue;
    }
    
    // Check if key exists
    public static boolean hasPlayerData(ServerPlayer player, String key) {
        CompoundTag nbt = player.getPersistentData();
        return nbt.contains("YourModID") && 
               nbt.getCompound("YourModID").contains(key);
    }
}
```

---

## <a id="component-api"></a>Component API (Text/Chat)

### Basic Component Creation

```java
import net.minecraft.network.chat.Component;

// Plain text
Component msg = Component.literal("Hello World");

// With formatting codes
Component colored = Component.literal("§aGreen §cRed §bBlue");

// Translatable (uses lang files)
Component trans = Component.translatable("gui.yourmod.title");

// ⚠️ TERNARY OPERATORS MUST BE PARENTHESIZED
boolean condition = true;
Component dynamic = Component.literal((condition ? "Yes" : "No"));  // ✅
// Component.literal(condition ? "Yes" : "No");  // ❌ COMPILATION ERROR
```

### Sending Messages to Players

```java
// System message (gray, left side)
player.sendSystemMessage(Component.literal("System notification"));

// Action bar (above hotbar)
player.displayClientMessage(Component.literal("§eAction bar message"), true);

// Chat message (from "server")
player.sendSystemMessage(Component.literal("§7[Server] §fMessage"));
```

### Advanced Component Building

```java
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;

// Builder pattern
MutableComponent msg = Component.literal("[YourMod] ")
    .withStyle(ChatFormatting.GOLD)
    .append(Component.literal("Success!")
        .withStyle(ChatFormatting.GREEN));

player.sendSystemMessage(msg);

// Click events (requires TextContent)
MutableComponent clickable = Component.literal("§b[Click Me]")
    .withStyle(style -> style
        .withClickEvent(new ClickEvent(
            ClickEvent.Action.RUN_COMMAND, 
            "/yourmod help"
        ))
        .withHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            Component.literal("§7Click to run command")
        ))
    );
```

---

## <a id="enchantment-system"></a>Enchantment System (1.21.11)

### ⚠️ COMPLETE REWRITE FROM 1.20.x

Enchantments are now **registry-based** using `Holder<Enchantment>`.

### Getting Enchantment from Registry

```java
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.resources.ResourceKey;

// Method 1: Built-in constants
public Optional<Holder<Enchantment>> getSharpness(ServerLevel level) {
    return level.registryAccess()
        .registryOrThrow(Registries.ENCHANTMENT)
        .getHolder(Enchantments.SHARPNESS);
}

// Method 2: By resource key
public Optional<Holder<Enchantment>> getEnchantment(ServerLevel level, ResourceKey<Enchantment> key) {
    return level.registryAccess()
        .registryOrThrow(Registries.ENCHANTMENT)
        .getHolder(key);
}
```

### Applying Enchantments

```java
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public void enchantItem(ItemStack stack, ServerLevel level, ResourceKey<Enchantment> enchantKey, int enchantLevel) {
    Optional<Holder<Enchantment>> enchantmentHolder = level.registryAccess()
        .registryOrThrow(Registries.ENCHANTMENT)
        .getHolder(enchantKey);
    
    if (enchantmentHolder.isPresent()) {
        stack.enchant(enchantmentHolder.get(), enchantLevel);
    }
}

// Example: Add Sharpness V to sword
public ItemStack createSharpSword(ServerLevel level) {
    ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
    
    Optional<Holder<Enchantment>> sharpness = level.registryAccess()
        .registryOrThrow(Registries.ENCHANTMENT)
        .getHolder(Enchantments.SHARPNESS);
    
    if (sharpness.isPresent()) {
        sword.enchant(sharpness.get(), 5);
    }
    
    return sword;
}
```

### Reading Enchantments

```java
import net.minecraft.world.item.enchantment.ItemEnchantments;

public int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantKey, ServerLevel level) {
    Optional<Holder<Enchantment>> holder = level.registryAccess()
        .registryOrThrow(Registries.ENCHANTMENT)
        .getHolder(enchantKey);
    
    if (holder.isEmpty()) return 0;
    
    ItemEnchantments enchantments = stack.getEnchantments();
    return enchantments.getLevel(holder.get());
}
```

---

## <a id="item-changes"></a>Item Changes in 1.21.11

### Removed Items

| Old Name | Replacement |
|----------|-------------|
| `Items.WATCH` | `Items.CLOCK` |

### Item Creation Patterns

```java
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// Basic item
ItemStack diamond = new ItemStack(Items.DIAMOND, 64);

// Item with custom name
ItemStack named = new ItemStack(Items.STICK);
named.setHoverName(Component.literal("§6Magic Wand"));

// Item with lore/description
ItemStack withLore = new ItemStack(Items.PAPER);
CompoundTag display = withLore.getOrCreateTagElement("display");
ListTag loreList = new ListTag();
loreList.add(StringTag.valueOf(Component.Serializer.toJson(
    Component.literal("§7Line 1 of lore")
)));
loreList.add(StringTag.valueOf(Component.Serializer.toJson(
    Component.literal("§7Line 2 of lore")
)));
display.put("Lore", loreList);
```

### Item Comparison

```java
// Check item type
boolean isDiamond = stack.is(Items.DIAMOND);

// Check multiple types (1.21.11)
boolean isValuable = stack.is(Items.DIAMOND) || 
                     stack.is(Items.EMERALD) || 
                     stack.is(Items.NETHERITE_INGOT);

// Count items in inventory
public int countItem(ServerPlayer player, Item item) {
    int count = 0;
    for (ItemStack stack : player.getInventory().items) {
        if (stack.is(item)) {
            count += stack.getCount();
        }
    }
    return count;
}
```

---

## <a id="common-imports"></a>Common Imports for 1.21.11

### Copy-Paste Imports Block

```java
// Fabric Core
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

// Commands
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

// Players & Entities
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

// Items
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

// NBT
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

// Components (Text)
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;

// Registries
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

// Enchantments
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Java utilities
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
```

---

## Quick Reference: API Migration

### Commands

```java
// ❌ 1.20.x
CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
    dispatcher.register(Commands.literal("old")
        .executes(ctx -> {
            ServerCommandSource source = ctx.getSource();
            // ...
        })
    );
});

// ✅ 1.21.11
CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
    dispatcher.register(Commands.literal("new")
        .executes(ctx -> {
            CommandSourceStack source = ctx.getSource();
            ServerPlayer player = source.getPlayerOrException();
            // ...
            return 1;
        })
    );
});
```

### Player Messages

```java
// ❌ 1.20.x
player.sendMessage(Text.literal("Message"), false);

// ✅ 1.21.11
player.sendSystemMessage(Component.literal("Message"));
```

### NBT Access

```java
// ❌ 1.20.x
NbtCompound nbt = stack.getOrCreateNbt();
nbt.putInt("key", 123);

// ✅ 1.21.11
CompoundTag nbt = stack.getOrCreateTag();
nbt.putInt("key", 123);
```

---

## Next Steps

Continue to:
- [03_GUI_SYSTEMS.md](03_GUI_SYSTEMS.md) - Build inventory GUIs with SGUI
- [04_COMMANDS.md](04_COMMANDS.md) - Complete command patterns
- [05_DATA_STORAGE.md](05_DATA_STORAGE.md) - Persistent data storage

---

**CRITICAL REMINDER:** All code examples are for 1.21.11 with Mojang mappings ONLY.
