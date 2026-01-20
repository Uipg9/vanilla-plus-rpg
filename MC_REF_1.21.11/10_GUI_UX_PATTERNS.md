# GUI/UX Patterns for Minecraft Mods (1.21.11)

> **Premium UI/UX Guide for Minecraft 1.21.11** - Best practices for creating polished, intuitive Minecraft mod interfaces using SGUI 1.12.0+1.21.11.
> 
> ✅ **Production-Tested**: All patterns from Pocket Life mod - a premium UI/UX focused mod.

---

## Table of Contents
1. [SGUI Library Fundamentals](#1-sgui-library-fundamentals)
2. [Screen Architecture Patterns](#2-screen-architecture-patterns)
3. [Color Schemes & Visual Design](#3-color-schemes--visual-design)
4. [Slot Layouts & Grid Systems](#4-slot-layouts--grid-systems)
5. [Interactive Elements](#5-interactive-elements)
6. [Status Indicators & Feedback](#6-status-indicators--feedback)
7. [Tab Navigation Systems](#7-tab-navigation-systems)
8. [Action Bar Notifications](#8-action-bar-notifications)
9. [Best Practices Checklist](#9-best-practices-checklist)
10. [Complete Example: Module Screen](#10-complete-example-module-screen)

---

## 1. SGUI Library Fundamentals

### Dependency Setup (build.gradle)
```groovy
repositories {
    maven { url 'https://maven.nucleoid.xyz/' }
}

dependencies {
    modImplementation include("eu.pb4:sgui:1.12.0+1.21.11")
}
```

### Core Classes
| Class | Purpose |
|-------|---------|
| `SimpleGui` | Basic chest-like GUI with fixed size |
| `SimpleGuiBuilder` | Fluent builder for SimpleGui |
| `GuiElement` | Clickable slot element |
| `GuiElementBuilder` | Builder for creating elements |
| `GuiElementInterface.ClickCallback` | Click event handler |

### Creating a Basic GUI
```java
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.screen.ScreenHandlerType;

public class MyScreen {
    public static void open(ServerPlayerEntity player) {
        // Create GUI with size (GENERIC_9X6 = 6 rows = 54 slots)
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("My Screen"));
        
        // Add elements
        gui.setSlot(0, new GuiElementBuilder(Items.DIAMOND)
            .setName(Text.literal("Click Me!").formatted(Formatting.AQUA))
            .setCallback((index, type, action) -> {
                player.sendMessage(Text.literal("Clicked!"), false);
            })
            .build());
        
        gui.open();
    }
}
```

### Available Screen Sizes
```java
// Standard chest sizes
ScreenHandlerType.GENERIC_9X1  // 1 row  (9 slots)
ScreenHandlerType.GENERIC_9X2  // 2 rows (18 slots)
ScreenHandlerType.GENERIC_9X3  // 3 rows (27 slots) - Small chest
ScreenHandlerType.GENERIC_9X4  // 4 rows (36 slots)
ScreenHandlerType.GENERIC_9X5  // 5 rows (45 slots)
ScreenHandlerType.GENERIC_9X6  // 6 rows (54 slots) - Large chest

// Special types
ScreenHandlerType.HOPPER       // 5 slots horizontal
ScreenHandlerType.GENERIC_3X3  // 3x3 grid (dispenser style)
```

---

## 2. Screen Architecture Patterns

### Pattern 1: Dashboard → Module → Tab Navigation
```
┌─────────────────────────────────────────┐
│           MAIN DASHBOARD                │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐       │
│  │ MOD1│ │ MOD2│ │ MOD3│ │ MOD4│       │
│  └──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘       │
└─────┼───────┼───────┼───────┼───────────┘
      ▼       ▼       ▼       ▼
┌─────────────────────────────────────────┐
│           MODULE SCREEN                 │
│  [Tab1] [Tab2] [Tab3] [Tab4]  ← Tabs    │
│  ┌─────────────────────────┐            │
│  │     TAB CONTENT         │            │
│  │   - Tool Slot           │            │
│  │   - Status Display      │            │
│  │   - Action Buttons      │            │
│  │   - Output Display      │            │
│  └─────────────────────────┘            │
│  [Back]           [Upgrade]  ← Actions  │
└─────────────────────────────────────────┘
```

### Pattern 2: Slot Index Reference (9x6 Grid)
```
┌────┬────┬────┬────┬────┬────┬────┬────┬────┐
│  0 │  1 │  2 │  3 │  4 │  5 │  6 │  7 │  8 │ Row 0
├────┼────┼────┼────┼────┼────┼────┼────┼────┤
│  9 │ 10 │ 11 │ 12 │ 13 │ 14 │ 15 │ 16 │ 17 │ Row 1
├────┼────┼────┼────┼────┼────┼────┼────┼────┤
│ 18 │ 19 │ 20 │ 21 │ 22 │ 23 │ 24 │ 25 │ 26 │ Row 2
├────┼────┼────┼────┼────┼────┼────┼────┼────┤
│ 27 │ 28 │ 29 │ 30 │ 31 │ 32 │ 33 │ 34 │ 35 │ Row 3
├────┼────┼────┼────┼────┼────┼────┼────┼────┤
│ 36 │ 37 │ 38 │ 39 │ 40 │ 41 │ 42 │ 43 │ 44 │ Row 4
├────┼────┼────┼────┼────┼────┼────┼────┼────┤
│ 45 │ 46 │ 47 │ 48 │ 49 │ 50 │ 51 │ 52 │ 53 │ Row 5
└────┴────┴────┴────┴────┴────┴────┴────┴────┘
```

### Pattern 3: Recommended Layout Zones
```
┌─────────────────────────────────────────────────────┐
│ [0-8]   HEADER ROW - Title, Close, Navigation       │
├─────────────────────────────────────────────────────┤
│ [9-17]  TAB ROW - Tab buttons, category selection   │
├─────────────────────────────────────────────────────┤
│ [18-44] CONTENT AREA - Main functionality           │
│         - Tool/Input slots (left side)              │
│         - Status/Info display (center)              │
│         - Output/Preview (right side)               │
├─────────────────────────────────────────────────────┤
│ [45-53] ACTION ROW - Buttons, Back, Upgrade, etc.   │
└─────────────────────────────────────────────────────┘
```

---

## 3. Color Schemes & Visual Design

### Formatting Codes
```java
// Text colors
Formatting.BLACK        // §0
Formatting.DARK_BLUE    // §1
Formatting.DARK_GREEN   // §2
Formatting.DARK_AQUA    // §3
Formatting.DARK_RED     // §4
Formatting.DARK_PURPLE  // §5
Formatting.GOLD         // §6
Formatting.GRAY         // §7
Formatting.DARK_GRAY    // §8
Formatting.BLUE         // §9
Formatting.GREEN        // §a
Formatting.AQUA         // §b
Formatting.RED          // §c
Formatting.LIGHT_PURPLE // §d
Formatting.YELLOW       // §e
Formatting.WHITE        // §f

// Styles
Formatting.BOLD         // §l
Formatting.ITALIC       // §o
Formatting.UNDERLINE    // §n
Formatting.STRIKETHROUGH // §m
Formatting.OBFUSCATED   // §k (animated/glitchy)
```

### Module-Specific Color Themes
```java
// Pocket Life Color Scheme
public class ModuleColors {
    // Quarry - Mining/Industrial (Blue)
    public static final Formatting QUARRY_PRIMARY = Formatting.AQUA;
    public static final Formatting QUARRY_ACCENT = Formatting.DARK_AQUA;
    public static final Item QUARRY_GLASS = Items.CYAN_STAINED_GLASS_PANE;
    
    // Estate - Nature/Farming (Green)
    public static final Formatting ESTATE_PRIMARY = Formatting.GREEN;
    public static final Formatting ESTATE_ACCENT = Formatting.DARK_GREEN;
    public static final Item ESTATE_GLASS = Items.LIME_STAINED_GLASS_PANE;
    
    // Arena - Combat/Danger (Red)
    public static final Formatting ARENA_PRIMARY = Formatting.RED;
    public static final Formatting ARENA_ACCENT = Formatting.DARK_RED;
    public static final Item ARENA_GLASS = Items.RED_STAINED_GLASS_PANE;
    
    // Laboratory - Magic/Research (Purple)
    public static final Formatting LAB_PRIMARY = Formatting.LIGHT_PURPLE;
    public static final Formatting LAB_ACCENT = Formatting.DARK_PURPLE;
    public static final Item LAB_GLASS = Items.PURPLE_STAINED_GLASS_PANE;
    
    // Terminal - Economy/Jobs (Gold)
    public static final Formatting TERMINAL_PRIMARY = Formatting.GOLD;
    public static final Formatting TERMINAL_ACCENT = Formatting.YELLOW;
    public static final Item TERMINAL_GLASS = Items.ORANGE_STAINED_GLASS_PANE;
}
```

### Status Color Conventions
```java
// Universal status colors
Formatting.GREEN  // Active, Running, Success, Enabled
Formatting.YELLOW // Ready, Waiting, Warning, Idle
Formatting.RED    // Stopped, Error, Disabled, Missing
Formatting.GRAY   // Inactive, Unavailable, Locked
Formatting.GOLD   // Currency, Rewards, Special
Formatting.AQUA   // Information, Tips, Stats
```

### Glass Pane Backgrounds
```java
// Filler elements for empty slots
public static GuiElement createFiller(Item glassPane) {
    return new GuiElementBuilder(glassPane)
        .setName(Text.empty())  // No tooltip
        .hideDefaultTooltip()
        .build();
}

// Usage - fill all empty slots
for (int i = 0; i < 54; i++) {
    if (gui.getSlot(i) == null) {
        gui.setSlot(i, createFiller(Items.GRAY_STAINED_GLASS_PANE));
    }
}
```

---

## 4. Slot Layouts & Grid Systems

### Standard Tool Slot Pattern
```java
// Tool slot with border decoration
private void createToolSlot(SimpleGui gui, int centerSlot, String toolType) {
    int row = centerSlot / 9;
    int col = centerSlot % 9;
    
    // Border slots (around the center)
    int[] borderSlots = {
        centerSlot - 10, centerSlot - 9, centerSlot - 8,  // Top row
        centerSlot - 1,                   centerSlot + 1,  // Middle sides
        centerSlot + 8,  centerSlot + 9, centerSlot + 10   // Bottom row
    };
    
    // Add border
    for (int slot : borderSlots) {
        if (slot >= 0 && slot < 54) {
            gui.setSlot(slot, new GuiElementBuilder(Items.IRON_BARS)
                .setName(Text.literal("Tool Slot Border").formatted(Formatting.GRAY))
                .hideDefaultTooltip()
                .build());
        }
    }
    
    // Center tool slot
    gui.setSlot(centerSlot, createToolSlotElement(toolType));
}
```

### Output Display Grid (3x3)
```java
// 3x3 output preview grid
private void createOutputGrid(SimpleGui gui, int startSlot, Map<Item, Integer> items) {
    List<Map.Entry<Item, Integer>> itemList = new ArrayList<>(items.entrySet());
    
    for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 3; col++) {
            int index = row * 3 + col;
            int slot = startSlot + (row * 9) + col;
            
            if (index < itemList.size()) {
                Map.Entry<Item, Integer> entry = itemList.get(index);
                gui.setSlot(slot, new GuiElementBuilder(entry.getKey())
                    .setCount(Math.min(entry.getValue(), 64))
                    .setName(Text.literal(entry.getValue() + "x ")
                        .append(entry.getKey().getName())
                        .formatted(Formatting.WHITE))
                    .build());
            } else {
                gui.setSlot(slot, createFiller(Items.BLACK_STAINED_GLASS_PANE));
            }
        }
    }
}
```

### Tab Button Row
```java
// Create tab buttons in row 1 (slots 9-17)
private void createTabRow(SimpleGui gui, String[] tabs, int selectedIndex) {
    int startSlot = 10;  // Center the tabs
    
    for (int i = 0; i < tabs.length; i++) {
        int slot = startSlot + i;
        boolean isSelected = (i == selectedIndex);
        
        gui.setSlot(slot, new GuiElementBuilder(isSelected ? Items.LIME_DYE : Items.GRAY_DYE)
            .setName(Text.literal(tabs[i])
                .formatted(isSelected ? Formatting.GREEN : Formatting.GRAY))
            .setCallback((idx, type, action) -> {
                // Switch to this tab
                switchTab(gui, i);
            })
            .build());
    }
}
```

---

## 5. Interactive Elements

### Button Patterns
```java
// Standard action button
public static GuiElement createButton(Item icon, String label, Formatting color, 
                                       GuiElementInterface.ClickCallback callback) {
    return new GuiElementBuilder(icon)
        .setName(Text.literal(label).formatted(color, Formatting.BOLD))
        .glow()  // Enchant glint for emphasis
        .setCallback(callback)
        .build();
}

// Start/Stop toggle button
public static GuiElement createToggleButton(boolean isActive, Runnable onToggle) {
    if (isActive) {
        return new GuiElementBuilder(Items.RED_CONCRETE)
            .setName(Text.literal("⏹ STOP").formatted(Formatting.RED, Formatting.BOLD))
            .addLoreLine(Text.literal("Click to stop operation").formatted(Formatting.GRAY))
            .setCallback((i, t, a) -> onToggle.run())
            .build();
    } else {
        return new GuiElementBuilder(Items.LIME_CONCRETE)
            .setName(Text.literal("▶ START").formatted(Formatting.GREEN, Formatting.BOLD))
            .addLoreLine(Text.literal("Click to start operation").formatted(Formatting.GRAY))
            .setCallback((i, t, a) -> onToggle.run())
            .build();
    }
}

// Back button (always slot 45 or 0)
public static GuiElement createBackButton(Runnable onBack) {
    return new GuiElementBuilder(Items.ARROW)
        .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
        .setCallback((i, t, a) -> onBack.run())
        .build();
}

// Upgrade button with cost display
public static GuiElement createUpgradeButton(int level, int cost, int playerCoins,
                                              Runnable onUpgrade) {
    boolean canAfford = playerCoins >= cost;
    
    return new GuiElementBuilder(canAfford ? Items.EXPERIENCE_BOTTLE : Items.GLASS_BOTTLE)
        .setName(Text.literal("⬆ Upgrade to Level " + (level + 1))
            .formatted(canAfford ? Formatting.GREEN : Formatting.RED))
        .addLoreLine(Text.literal("Cost: $" + cost)
            .formatted(canAfford ? Formatting.GOLD : Formatting.DARK_RED))
        .addLoreLine(Text.literal("Your coins: $" + playerCoins)
            .formatted(Formatting.GRAY))
        .addLoreLine(Text.empty())
        .addLoreLine(Text.literal(canAfford ? "Click to upgrade!" : "Not enough coins!")
            .formatted(canAfford ? Formatting.YELLOW : Formatting.RED))
        .setCallback((i, t, a) -> {
            if (canAfford) onUpgrade.run();
        })
        .build();
}
```

### Collect Button with Count
```java
public static GuiElement createCollectButton(int itemCount, Runnable onCollect) {
    if (itemCount > 0) {
        return new GuiElementBuilder(Items.CHEST)
            .setName(Text.literal("📦 Collect " + itemCount + " Items")
                .formatted(Formatting.GOLD, Formatting.BOLD))
            .addLoreLine(Text.literal("Click to collect all!").formatted(Formatting.GRAY))
            .glow()
            .setCallback((i, t, a) -> onCollect.run())
            .build();
    } else {
        return new GuiElementBuilder(Items.CHEST)
            .setName(Text.literal("📦 No Items to Collect")
                .formatted(Formatting.GRAY))
            .build();
    }
}
```

---

## 6. Status Indicators & Feedback

### Status Display Element
```java
public static GuiElement createStatusDisplay(String tabName, boolean hasTools, 
                                              boolean isActive, int itemCount) {
    Item statusItem;
    Formatting statusColor;
    String statusText;
    
    if (isActive) {
        statusItem = Items.LIME_CONCRETE;
        statusColor = Formatting.GREEN;
        statusText = "🟢 RUNNING";
    } else if (hasTools) {
        statusItem = Items.YELLOW_CONCRETE;
        statusColor = Formatting.YELLOW;
        statusText = "🟡 READY";
    } else {
        statusItem = Items.RED_CONCRETE;
        statusColor = Formatting.RED;
        statusText = "🔴 NO TOOL";
    }
    
    return new GuiElementBuilder(statusItem)
        .setName(Text.literal(tabName + " Status").formatted(Formatting.WHITE, Formatting.BOLD))
        .addLoreLine(Text.literal(statusText).formatted(statusColor))
        .addLoreLine(Text.empty())
        .addLoreLine(Text.literal("Buffer: " + itemCount + " items").formatted(Formatting.AQUA))
        .build();
}
```

### Progress Bar (Lore-based)
```java
public static List<Text> createProgressBar(int current, int max, int barLength, 
                                           Formatting fillColor, Formatting emptyColor) {
    List<Text> lore = new ArrayList<>();
    
    float percentage = (float) current / max;
    int filled = (int) (percentage * barLength);
    int empty = barLength - filled;
    
    StringBuilder bar = new StringBuilder();
    bar.append("§" + fillColor.getCode());
    for (int i = 0; i < filled; i++) bar.append("█");
    bar.append("§" + emptyColor.getCode());
    for (int i = 0; i < empty; i++) bar.append("░");
    
    lore.add(Text.literal(bar.toString()));
    lore.add(Text.literal(current + "/" + max + " (" + (int)(percentage * 100) + "%)")
        .formatted(Formatting.GRAY));
    
    return lore;
}

// Usage
builder.addLoreLine(Text.literal("Durability:").formatted(Formatting.GRAY));
for (Text line : createProgressBar(50, 100, 20, Formatting.GREEN, Formatting.DARK_GRAY)) {
    builder.addLoreLine(line);
}
```

### Level/XP Display
```java
public static GuiElement createLevelDisplay(String moduleName, int level, int maxLevel) {
    return new GuiElementBuilder(Items.NETHER_STAR)
        .setName(Text.literal(moduleName + " Level " + level)
            .formatted(Formatting.GOLD, Formatting.BOLD))
        .addLoreLine(Text.literal("━".repeat(25)).formatted(Formatting.DARK_GRAY))
        .addLoreLine(Text.literal("Speed Bonus: +" + (level * 10) + "%")
            .formatted(Formatting.GREEN))
        .addLoreLine(Text.literal("Luck Bonus: +" + (level * 5) + "%")
            .formatted(Formatting.AQUA))
        .addLoreLine(Text.literal("Capacity: " + (level * 100) + " items")
            .formatted(Formatting.YELLOW))
        .addLoreLine(Text.literal("━".repeat(25)).formatted(Formatting.DARK_GRAY))
        .addLoreLine(Text.literal(level >= maxLevel ? "MAX LEVEL!" : "Upgradeable")
            .formatted(level >= maxLevel ? Formatting.LIGHT_PURPLE : Formatting.GRAY))
        .build();
}
```

---

## 7. Tab Navigation Systems

### Tab System Architecture
```java
public class TabbedScreen {
    private final String[] tabs;
    private int currentTab = 0;
    private final ServerPlayerEntity player;
    private SimpleGui gui;
    
    public TabbedScreen(ServerPlayerEntity player, String[] tabs) {
        this.player = player;
        this.tabs = tabs;
    }
    
    public void open() {
        gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Module - " + tabs[currentTab]));
        
        renderTabs();
        renderContent();
        renderActions();
        fillEmpty();
        
        gui.open();
    }
    
    private void renderTabs() {
        // Clear tab row
        for (int i = 9; i < 18; i++) {
            gui.setSlot(i, createFiller(Items.BLACK_STAINED_GLASS_PANE));
        }
        
        // Render tab buttons centered
        int startSlot = 9 + (9 - tabs.length) / 2;
        for (int i = 0; i < tabs.length; i++) {
            final int tabIndex = i;
            boolean selected = (i == currentTab);
            
            gui.setSlot(startSlot + i, new GuiElementBuilder(
                    selected ? Items.LIME_STAINED_GLASS_PANE : Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(tabs[i])
                    .formatted(selected ? Formatting.GREEN : Formatting.GRAY)
                    .formatted(selected ? Formatting.BOLD : Formatting.ITALIC))
                .addLoreLine(selected 
                    ? Text.literal("Currently viewing").formatted(Formatting.DARK_GREEN)
                    : Text.literal("Click to switch").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, type, action) -> {
                    if (!selected) {
                        currentTab = tabIndex;
                        open();  // Re-render with new tab
                    }
                })
                .build());
        }
    }
    
    private void renderContent() {
        // Override in subclass for tab-specific content
    }
    
    private void renderActions() {
        // Back button (slot 45)
        gui.setSlot(45, createBackButton(() -> DashboardScreen.open(player)));
        
        // Upgrade button (slot 53)
        gui.setSlot(53, createUpgradeButton(...));
    }
    
    private void fillEmpty() {
        for (int i = 0; i < 54; i++) {
            if (gui.getSlot(i) == null) {
                gui.setSlot(i, createFiller(Items.GRAY_STAINED_GLASS_PANE));
            }
        }
    }
}
```

### Per-Tab State Management
```java
// Store state per-tab (not per-module)
public class TabState {
    private final Map<String, Boolean> activeStates = new HashMap<>();
    private final Map<String, ItemStack> tools = new HashMap<>();
    private final Map<String, Map<Item, Integer>> buffers = new HashMap<>();
    
    public String getTabKey(String module, String tab) {
        return module + "_" + tab.toLowerCase().replace(" ", "_");
    }
    
    public boolean isActive(String module, String tab) {
        return activeStates.getOrDefault(getTabKey(module, tab), false);
    }
    
    public void setActive(String module, String tab, boolean active) {
        activeStates.put(getTabKey(module, tab), active);
    }
}
```

---

## 8. Action Bar Notifications

### Using Action Bar Instead of Chat
```java
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;

public class Notifications {
    
    // Send action bar message (appears above hotbar, non-intrusive)
    public static void sendActionBar(ServerPlayerEntity player, Text message) {
        player.networkHandler.sendPacket(new OverlayMessageS2CPacket(message));
    }
    
    // Alternative using built-in method
    public static void sendActionBarAlt(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message), true);  // true = action bar
    }
    
    // Formatted status notification
    public static void notifyProduction(ServerPlayerEntity player, 
                                        int operationCount, 
                                        Map<String, Integer> moduleItems) {
        MutableText message = Text.literal("")
            .append(Text.literal("[Pocket Life] ").formatted(Formatting.GOLD, Formatting.BOLD))
            .append(Text.literal(operationCount + " running. ").formatted(Formatting.YELLOW));
        
        for (Map.Entry<String, Integer> entry : moduleItems.entrySet()) {
            message.append(Text.literal(entry.getKey() + ": ")
                .formatted(Formatting.GRAY))
                .append(Text.literal(entry.getValue() + " ")
                    .formatted(Formatting.WHITE))
                .append(Text.literal("| ").formatted(Formatting.DARK_GRAY));
        }
        
        sendActionBar(player, message);
    }
}
```

### When to Use Each Notification Type
| Type | Use Case | Example |
|------|----------|---------|
| **Action Bar** | Periodic status updates, production summaries | "5 operations running" |
| **Chat (false)** | Important events, user actions, achievements | "Upgrade complete!" |
| **Title** | Major milestones, rare events | "LEGENDARY DROP!" |
| **Sound** | Feedback for clicks, completions | BLOCK_NOTE_BLOCK_PLING |

---

## 9. Best Practices Checklist

### ✅ Visual Design
- [ ] Consistent color theme per module
- [ ] Glass pane backgrounds (no empty slots)
- [ ] Status indicators (Green/Yellow/Red)
- [ ] Proper spacing and alignment
- [ ] Tooltips on every interactive element
- [ ] Divider lines in lore (━━━━━━)

### ✅ User Experience
- [ ] Back button always in same position (slot 45)
- [ ] Tab navigation in consistent location (row 1)
- [ ] Immediate visual feedback on click
- [ ] Clear affordances (what's clickable)
- [ ] Descriptive button names with emoji
- [ ] Loading states for async operations

### ✅ Information Hierarchy
- [ ] Title in screen header
- [ ] Current state clearly visible
- [ ] Costs and requirements shown before action
- [ ] Progress/buffers easily readable
- [ ] Stats and bonuses in organized lore

### ✅ Technical
- [ ] Re-render on state change (open() again)
- [ ] Handle null cases gracefully
- [ ] Validate actions server-side
- [ ] Limit item counts to 64 for display
- [ ] Use action bar for frequent updates

---

## 10. Complete Example: Module Screen

```java
package com.example.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.*;

public class ExampleModuleScreen {
    
    private static final String[] TABS = {"Mining", "Smelting", "Crafting", "Storage"};
    private static final Formatting PRIMARY_COLOR = Formatting.AQUA;
    private static final Item THEME_GLASS = Items.CYAN_STAINED_GLASS_PANE;
    
    public static void open(ServerPlayerEntity player, int tabIndex) {
        // Get player data
        PlayerData data = PlayerDataManager.get(player);
        String tabKey = "example_" + TABS[tabIndex].toLowerCase();
        
        // Create GUI
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⛏ Example Module - " + TABS[tabIndex]));
        
        // === ROW 0: Header ===
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
            .setName(Text.literal("Example Module").formatted(PRIMARY_COLOR, Formatting.BOLD))
            .addLoreLine(Text.literal("Level " + data.getLevel("example")).formatted(Formatting.GRAY))
            .build());
        
        // === ROW 1: Tabs ===
        int tabStart = 9 + (9 - TABS.length) / 2;
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            boolean selected = (i == tabIndex);
            gui.setSlot(tabStart + i, new GuiElementBuilder(
                    selected ? Items.LIME_STAINED_GLASS_PANE : Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(TABS[i])
                    .formatted(selected ? Formatting.GREEN : Formatting.GRAY))
                .setCallback((s, t, a) -> {
                    if (!selected) open(player, idx);
                })
                .build());
        }
        
        // === ROW 2-4: Content ===
        // Tool slot (slot 20)
        boolean hasTool = data.hasTabTool("example", TABS[tabIndex]);
        boolean isActive = data.isTabActive("example", TABS[tabIndex]);
        
        gui.setSlot(20, new GuiElementBuilder(hasTool ? Items.DIAMOND_PICKAXE : Items.BARRIER)
            .setName(Text.literal(hasTool ? "✓ Tool Inserted" : "✗ Insert Pickaxe")
                .formatted(hasTool ? Formatting.GREEN : Formatting.RED))
            .addLoreLine(Text.literal("Click with a pickaxe to insert").formatted(Formatting.GRAY))
            .setCallback((slot, type, action, slotGui) -> {
                ItemStack cursor = player.currentScreenHandler.getCursorStack();
                if (!cursor.isEmpty() && cursor.getItem() instanceof PickaxeItem) {
                    data.setTabTool("example", TABS[tabIndex], cursor);
                    player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                    open(player, tabIndex);  // Refresh
                } else if (hasTool) {
                    player.getInventory().offerOrDrop(data.getTabToolStack("example", TABS[tabIndex]));
                    data.removeTabTool("example", TABS[tabIndex]);
                    open(player, tabIndex);
                }
            })
            .build());
        
        // Status display (slot 22)
        Item statusItem = isActive ? Items.LIME_CONCRETE : 
                         (hasTool ? Items.YELLOW_CONCRETE : Items.RED_CONCRETE);
        String statusText = isActive ? "🟢 RUNNING" : (hasTool ? "🟡 READY" : "🔴 NO TOOL");
        
        gui.setSlot(22, new GuiElementBuilder(statusItem)
            .setName(Text.literal("Status").formatted(Formatting.WHITE, Formatting.BOLD))
            .addLoreLine(Text.literal(statusText)
                .formatted(isActive ? Formatting.GREEN : 
                          (hasTool ? Formatting.YELLOW : Formatting.RED)))
            .build());
        
        // Start/Stop button (slot 24)
        if (hasTool) {
            gui.setSlot(24, new GuiElementBuilder(isActive ? Items.RED_CONCRETE : Items.LIME_CONCRETE)
                .setName(Text.literal(isActive ? "⏹ STOP" : "▶ START")
                    .formatted(isActive ? Formatting.RED : Formatting.GREEN, Formatting.BOLD))
                .setCallback((s, t, a) -> {
                    data.setTabActive("example", TABS[tabIndex], !isActive);
                    open(player, tabIndex);
                })
                .build());
        }
        
        // Output display (slots 29-33)
        Map<Item, Integer> buffer = data.getTabBuffer("example", TABS[tabIndex]);
        int totalItems = buffer.values().stream().mapToInt(Integer::intValue).sum();
        
        gui.setSlot(31, new GuiElementBuilder(Items.CHEST)
            .setName(Text.literal("📦 Output: " + totalItems + " items")
                .formatted(Formatting.GOLD))
            .build());
        
        // === ROW 5: Actions ===
        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
            .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
            .setCallback((s, t, a) -> DashboardScreen.open(player))
            .build());
        
        // Collect button
        gui.setSlot(49, new GuiElementBuilder(totalItems > 0 ? Items.HOPPER : Items.BARRIER)
            .setName(Text.literal(totalItems > 0 ? "Collect All" : "Nothing to Collect")
                .formatted(totalItems > 0 ? Formatting.GREEN : Formatting.GRAY))
            .setCallback((s, t, a) -> {
                if (totalItems > 0) {
                    for (Map.Entry<Item, Integer> entry : buffer.entrySet()) {
                        player.getInventory().offerOrDrop(
                            new ItemStack(entry.getKey(), entry.getValue()));
                    }
                    data.clearTabBuffer("example", TABS[tabIndex]);
                    open(player, tabIndex);
                }
            })
            .build());
        
        // Upgrade button
        int level = data.getLevel("example");
        int cost = 100 * (int) Math.pow(2, level);
        int coins = data.getCoins();
        
        gui.setSlot(53, new GuiElementBuilder(coins >= cost ? Items.EXPERIENCE_BOTTLE : Items.GLASS_BOTTLE)
            .setName(Text.literal("⬆ Upgrade (Lv." + (level + 1) + ")")
                .formatted(coins >= cost ? Formatting.GREEN : Formatting.RED))
            .addLoreLine(Text.literal("Cost: $" + cost).formatted(Formatting.GOLD))
            .setCallback((s, t, a) -> {
                if (coins >= cost) {
                    data.setCoins(coins - cost);
                    data.setLevel("example", level + 1);
                    player.sendMessage(Text.literal("Upgraded to Level " + (level + 1) + "!")
                        .formatted(Formatting.GREEN), false);
                    open(player, tabIndex);
                }
            })
            .build());
        
        // Fill empty slots
        for (int i = 0; i < 54; i++) {
            if (gui.getSlot(i) == null) {
                gui.setSlot(i, new GuiElementBuilder(THEME_GLASS)
                    .setName(Text.empty())
                    .hideDefaultTooltip()
                    .build());
            }
        }
        
        gui.open();
    }
}
```

---

## Summary

This guide covers the essential patterns for creating professional Minecraft mod GUIs:

1. **SGUI** provides a clean server-side API
2. **Consistent layouts** improve user familiarity
3. **Color themes** distinguish modules visually
4. **Status indicators** communicate state instantly
5. **Tab systems** organize complex functionality
6. **Action bars** reduce chat spam
7. **Per-tab state** enables parallel operations

Following these patterns results in intuitive, polished mod interfaces that players enjoy using.

---

*Document Version: 1.0 | For Minecraft 1.21.11 with SGUI 1.12.0*
