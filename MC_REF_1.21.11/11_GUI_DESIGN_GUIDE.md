# 🎨 Professional GUI Design Guide - Minecraft 1.21.11

> **Creating Beautiful, Functional, User-Friendly Interfaces**
> 
> Based on production experience from Pocket Life mod - a premium UI/UX focused mod with 7,800+ lines of polished interface code.

---

## Table of Contents

1. [Design Philosophy](#design-philosophy)
2. [Visual Design Principles](#visual-design)
3. [Layout & Spacing](#layout-spacing)
4. [Interactive Elements](#interactive-elements)
5. [Feedback Systems](#feedback-systems)
6. [Color Theory for GUIs](#color-theory)
7. [Typography & Text](#typography)
8. [Sound Design](#sound-design)
9. [Complete Examples](#examples)
10. [Common Mistakes](#mistakes)

---

## <a id="design-philosophy"></a>1. Design Philosophy

### "No Boring Grey Boxes"

**❌ BAD - Typical Mod GUI:**
```
┌─────────────────┐
│ Grey Slot       │
│ Grey Slot       │
│ Grey Slot       │
│ [Button]        │
└─────────────────┘
```

**✅ GOOD - Premium GUI:**
```
┌─────────────────────────┐
│ ╔═══════════════════╗   │  ← Themed border
│ ║ 🎯 Module: Quarry ║   │  ← Clear title + icon
│ ╠═══════════════════╣   │
│ ║ ⛏️  Excavator [■]  ║   │  ← Status indicator
│ ║ 🗺️  Landscaper [■] ║   │
│ ║ 💎 Prospector [□] ║   │
│ ╚═══════════════════╝   │
│     [⚙️ Settings]       │  ← Clear actions
└─────────────────────────┘
```

### Core Principles

1. **Visual Hierarchy** - Important info stands out
2. **Feedback** - Every action has response (sound + visual)
3. **Consistency** - Same actions work the same way
4. **Clarity** - User always knows what will happen
5. **Polish** - Small details matter

---

## <a id="visual-design"></a>2. Visual Design Principles

### Themed Backgrounds

Each screen should have a **unique visual identity**:

```java
// Example: Mining-themed background for Quarry module
private void createThemedBackground(SimpleGui gui) {
    // Border pattern (slot 0-8, 45-53)
    Item borderItem = Items.GRAY_STAINED_GLASS_PANE;
    for (int i = 0; i < 9; i++) {
        gui.setSlot(i, createBorder(borderItem));
        gui.setSlot(45 + i, createBorder(borderItem));
    }
    
    // Side borders (slots 9, 18, 27, 36 and 17, 26, 35, 44)
    for (int row = 1; row < 5; row++) {
        gui.setSlot(row * 9, createBorder(borderItem));
        gui.setSlot(row * 9 + 8, createBorder(borderItem));
    }
    
    // Accent corners (Copper for mining theme)
    gui.setSlot(0, createAccent(Items.COPPER_BLOCK));
    gui.setSlot(8, createAccent(Items.COPPER_BLOCK));
    gui.setSlot(45, createAccent(Items.COPPER_BLOCK));
    gui.setSlot(53, createAccent(Items.COPPER_BLOCK));
}

private GuiElementBuilder createBorder(Item item) {
    return new GuiElementBuilder()
        .setItem(item)
        .setName(Component.literal("")); // No name = clean look
}

private GuiElementBuilder createAccent(Item item) {
    return new GuiElementBuilder()
        .setItem(item)
        .setName(Component.literal(""));
}
```

### Module-Specific Themes

| Module | Primary Color | Accent | Border Material | Theme |
|--------|---------------|--------|-----------------|-------|
| **Quarry** | Stone Gray | Copper | Gray Glass Pane | Mining/Industrial |
| **Estate** | Oak Brown | Green | Oak Leaves | Nature/Agriculture |
| **Arena** | Crimson Red | Nether | Red Glass Pane | Combat/Fire |
| **Laboratory** | End Purple | Magenta | Purple Glass | Science/Magic |
| **Terminal** | Copper Orange | Yellow | Orange Glass | Tech/Steampunk |

---

## <a id="layout-spacing"></a>3. Layout & Spacing

### Grid System for 9x6 GUI (54 slots)

```
Row 0:  [0 ][1 ][2 ][3 ][4 ][5 ][6 ][7 ][8 ]  ← Header/Title
Row 1:  [9 ][10][11][12][13][14][15][16][17]  ← Tab Navigation
Row 2:  [18][19][20][21][22][23][24][25][26]  ← Content Area
Row 3:  [27][28][29][30][31][32][33][34][35]  ← Content Area
Row 4:  [36][37][38][39][40][41][42][43][44]  ← Content Area
Row 5:  [45][46][47][48][49][50][51][52][53]  ← Footer/Actions
```

### Standard Layout Pattern

```java
public class ModuleScreen extends SimpleGui {
    // Layout constants
    private static final int HEADER_ROW = 0;
    private static final int TAB_ROW = 1;
    private static final int CONTENT_START = 18; // Row 2, column 0
    private static final int FOOTER_ROW = 5;
    
    // Specific positions
    private static final int PLAYER_HEAD_SLOT = 4;    // Center of header
    private static final int WALLET_SLOT = 8;         // Top right
    private static final int BACK_BUTTON_SLOT = 45;   // Bottom left
    private static final int HELP_BUTTON_SLOT = 49;   // Bottom center
    private static final int SETTINGS_SLOT = 53;      // Bottom right
    
    private void setupLayout() {
        setupHeader();     // Row 0
        setupTabs();       // Row 1
        setupContent();    // Rows 2-4
        setupFooter();     // Row 5
    }
}
```

### Content Spacing

**Rule of Thumb**: Leave at least 1 empty slot between interactive elements

```java
// ❌ BAD - Cramped
[Button][Button][Button][Button]

// ✅ GOOD - Breathing room
[Button] [ ] [Button] [ ] [Button]
```

---

## <a id="interactive-elements"></a>4. Interactive Elements

### Button Design

```java
private GuiElementBuilder createButton(
    String label, 
    String emoji, 
    Item icon, 
    Runnable onClick
) {
    return new GuiElementBuilder()
        .setItem(icon)
        .setName(Component.literal(emoji + " " + label).withStyle(ChatFormatting.YELLOW))
        .addLoreLine(Component.literal(""))
        .addLoreLine(Component.literal("§7Click to " + label.toLowerCase()))
        .setCallback((index, type, action) -> {
            PocketSounds.playClick(player);
            onClick.run();
        })
        .glow(); // Makes important buttons glow
}

// Usage
gui.setSlot(49, createButton(
    "Collect All",
    "💰",
    Items.GOLD_INGOT,
    this::collectAllItems
));
```

### Toggle Switches

```java
private GuiElementBuilder createToggle(String name, boolean state) {
    return new GuiElementBuilder()
        .setItem(state ? Items.LIME_DYE : Items.GRAY_DYE)
        .setName(Component.literal("§f" + name))
        .addLoreLine(Component.literal(""))
        .addLoreLine(Component.literal(
            state ? "§a● Enabled" : "§7○ Disabled"
        ))
        .addLoreLine(Component.literal(""))
        .addLoreLine(Component.literal("§eClick to toggle"))
        .setCallback((index, type, action) -> {
            toggleSetting(name);
            PocketSounds.playClick(player);
            refresh(); // Rebuild GUI
        });
}
```

### Progress Bars

```java
private String createProgressBar(int percent) {
    int filled = percent / 10;
    int empty = 10 - filled;
    
    // Color based on progress
    String color = percent >= 100 ? "§a" :   // Green when full
                   percent >= 50  ? "§e" :   // Yellow when half
                   "§c";                      // Red when low
    
    return color + "█".repeat(filled) + "§8" + "░".repeat(empty) + " " + color + percent + "%";
}

// Usage in lore
builder.addLoreLine(Component.literal("§7Progress: " + createProgressBar(progress)));
```

### Status Indicators

```java
private GuiElementBuilder createStatusSlot(String name, boolean active, int level) {
    Item icon = active ? Items.LIME_CONCRETE : Items.GRAY_CONCRETE;
    String statusEmoji = active ? "●" : "○";
    ChatFormatting color = active ? ChatFormatting.GREEN : ChatFormatting.GRAY;
    
    GuiElementBuilder builder = new GuiElementBuilder()
        .setItem(icon)
        .setName(Component.literal("§f" + name))
        .addLoreLine(Component.literal(""))
        .addLoreLine(Component.literal(color + statusEmoji + " " + 
            (active ? "Active" : "Inactive")));
    
    if (active) {
        builder.addLoreLine(Component.literal("§7Level: §f" + level));
        builder.glow(); // Glow when active
    }
    
    return builder;
}
```

---

## <a id="feedback-systems"></a>5. Feedback Systems

### Multi-Channel Feedback

Every user action should trigger **3 types of feedback**:

1. **Visual** - GUI change, glow effect, color change
2. **Audio** - Click sound, success sound, error sound
3. **Text** - Chat message, action bar, or toast

```java
private void handlePurchase(String item, int cost) {
    PocketPlayerData data = PlayerDataManager.get(player);
    
    if (data.getCoins() >= cost) {
        // SUCCESS PATH
        
        // 1. Visual: Update GUI
        data.addCoins(-cost);
        refresh(); // Rebuild screen with new coin count
        
        // 2. Audio: Success sound
        player.playNotifySound(
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            SoundSource.PLAYERS,
            1.0f, 1.0f
        );
        
        // 3. Text: Action bar notification
        player.displayClientMessage(
            Component.literal("§a✓ Purchased " + item + " for §6$" + cost),
            true // Action bar
        );
        
    } else {
        // FAILURE PATH
        
        // 1. Visual: Shake effect (close and reopen)
        close();
        player.getLevel().playSound(null, player.blockPosition(), 
            SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.5f, 1.0f);
        
        // 2. Audio: Error sound
        player.playNotifySound(
            SoundEvents.VILLAGER_NO,
            SoundSource.PLAYERS,
            0.8f, 1.0f
        );
        
        // 3. Text: Error message
        player.displayClientMessage(
            Component.literal("§c✗ Not enough coins! Need §6$" + cost),
            true
        );
        
        // Reopen after brief delay
        player.getServer().execute(() -> open());
    }
}
```

### Sound Design Principles

```java
public class ModSounds {
    // Navigation
    public static void playClick(ServerPlayer player) {
        play(player, SoundEvents.LEVER_CLICK, 0.5f, 1.0f);
    }
    
    // Success states
    public static void playSuccess(ServerPlayer player) {
        play(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
    }
    
    public static void playUpgrade(ServerPlayer player) {
        play(player, SoundEvents.ANVIL_USE, 0.3f, 1.5f);
    }
    
    // Error states
    public static void playError(ServerPlayer player) {
        play(player, SoundEvents.VILLAGER_NO, 0.8f, 1.0f);
    }
    
    public static void playInsufficientFunds(ServerPlayer player) {
        play(player, SoundEvents.VILLAGER_NO, 0.5f, 0.8f);
    }
    
    // Context-specific
    public static void playHarvest(ServerPlayer player) {
        play(player, SoundEvents.CROP_BREAK, 0.6f, 1.0f);
    }
    
    public static void playMining(ServerPlayer player) {
        play(player, SoundEvents.STONE_BREAK, 0.4f, 1.0f);
    }
    
    private static void play(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
    }
}
```

---

## <a id="color-theory"></a>6. Color Theory for Minecraft GUIs

### Formatting Codes Reference

```java
// Text colors
§0 Black       §8 Dark Gray
§1 Dark Blue   §9 Blue
§2 Dark Green  §a Green
§3 Dark Aqua   §b Aqua
§4 Dark Red    §c Red
§5 Dark Purple §d Light Purple
§6 Gold        §e Yellow
§7 Gray        §f White

// Formatting
§l Bold        §o Italic
§n Underline   §m Strikethrough
§k Obfuscated  §r Reset
```

### Semantic Color Usage

| Purpose | Color | Code | When to Use |
|---------|-------|------|-------------|
| **Success** | Green | `§a` | Confirmations, active states, high values |
| **Warning** | Yellow/Gold | `§e`, `§6` | Cautions, medium values, costs |
| **Error** | Red | `§c` | Failures, disabled states, low values |
| **Info** | Gray | `§7`, `§8` | Descriptions, secondary text |
| **Highlight** | White | `§f` | Important text, titles, values |
| **Special** | Aqua/Purple | `§b`, `§d` | Rare items, bonuses, special features |

### Practical Examples

```java
// Wallet display
Component.literal("§6💰 $" + coins) // Gold for currency

// Level display
Component.literal("§bLevel " + level) // Aqua for progression

// Status messages
Component.literal("§a✓ Operation completed")
Component.literal("§e⚠ Low durability warning")
Component.literal("§c✗ Failed to process")

// Resource counts
private String formatCount(int count, int max) {
    if (count >= max) return "§a" + count + "§7/" + max;  // Full = green
    if (count >= max/2) return "§e" + count + "§7/" + max; // Half = yellow
    return "§c" + count + "§7/" + max;  // Low = red
}
```

---

## <a id="typography"></a>7. Typography & Text

### Emoji Usage

Emojis make text more scannable and visually appealing:

```java
// Module icons
"⛏️ Quarry"
"🌾 Estate"
"⚔️ Arena"
"⚗️ Laboratory"
"🚀 Terminal"

// Status icons
"● Active"      // Filled circle
"○ Inactive"    // Empty circle
"✓ Complete"
"✗ Failed"
"⚠ Warning"

// Resource icons
"💰 Coins"
"💎 Gems"
"⭐ XP"
"🔋 Energy"
"⏱️ Time"
"🔧 Tools"
```

### Text Hierarchy

```java
private GuiElementBuilder createInfoCard() {
    return new GuiElementBuilder()
        .setItem(Items.PAPER)
        // TITLE - Bold, white, emoji
        .setName(Component.literal("§f§l📊 Statistics"))
        
        // SECTION - Empty line for spacing
        .addLoreLine(Component.literal(""))
        
        // SUBSECTION - Yellow, bold
        .addLoreLine(Component.literal("§e§lProduction"))
        
        // DATA - Gray label, white value
        .addLoreLine(Component.literal("§7Total Items: §f1,234"))
        .addLoreLine(Component.literal("§7Per Hour: §f89"))
        
        // SECTION BREAK
        .addLoreLine(Component.literal(""))
        
        // SUBSECTION
        .addLoreLine(Component.literal("§e§lEconomy"))
        .addLoreLine(Component.literal("§7Total Earned: §6$45,678"))
        .addLoreLine(Component.literal("§7Current Rate: §6$12/min"))
        
        // ACTION HINT - Empty line + italic gray
        .addLoreLine(Component.literal(""))
        .addLoreLine(Component.literal("§7§oClick for details"));
}
```

### Number Formatting

```java
public class NumberFormat {
    // Format large numbers with commas
    public static String formatNumber(int value) {
        return String.format("%,d", value);
    }
    
    // Format currency
    public static String formatCurrency(int amount) {
        if (amount >= 1_000_000) {
            return String.format("$%.1fM", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return String.format("$%.1fK", amount / 1_000.0);
        }
        return "$" + formatNumber(amount);
    }
    
    // Format percentage
    public static String formatPercent(double value) {
        return String.format("%.1f%%", value * 100);
    }
    
    // Format time
    public static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        }
        return secs + "s";
    }
}

// Usage
builder.addLoreLine(Component.literal("§7Coins: §6" + NumberFormat.formatCurrency(45678)));
// Output: "Coins: $45.7K"
```

---

## <a id="sound-design"></a>8. Sound Design

### Sound Mapping for UI Actions

```java
public enum UIAction {
    CLICK(SoundEvents.LEVER_CLICK, 0.5f, 1.0f),
    SELECT(SoundEvents.LEVER_CLICK, 0.6f, 1.2f),
    BACK(SoundEvents.LEVER_CLICK, 0.4f, 0.8f),
    
    SUCCESS(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f),
    UPGRADE(SoundEvents.ANVIL_USE, 0.3f, 1.5f),
    UNLOCK(SoundEvents.PLAYER_LEVELUP, 0.5f, 1.0f),
    
    ERROR(SoundEvents.VILLAGER_NO, 0.8f, 1.0f),
    INSUFFICIENT_FUNDS(SoundEvents.VILLAGER_NO, 0.5f, 0.8f),
    
    PURCHASE(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f),
    SELL(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7f, 0.9f),
    
    COLLECT(SoundEvents.ITEM_PICKUP, 0.6f, 1.0f),
    HARVEST(SoundEvents.CROP_BREAK, 0.6f, 1.0f),
    MINE(SoundEvents.STONE_BREAK, 0.4f, 1.0f),
    CRAFT(SoundEvents.ANVIL_USE, 0.2f, 1.2f);
    
    private final SoundEvent sound;
    private final float volume;
    private final float pitch;
    
    UIAction(SoundEvent sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    public void play(ServerPlayer player) {
        player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
    }
}

// Usage
UIAction.CLICK.play(player);
UIAction.SUCCESS.play(player);
```

---

## <a id="examples"></a>9. Complete Examples

### Example 1: Dashboard with Module Icons

```java
public class DashboardScreen extends SimpleGui {
    private final ServerPlayer player;
    private final PocketPlayerData data;
    
    public DashboardScreen(ServerPlayer player) {
        super(MenuType.GENERIC_9X6, player, false);
        this.player = player;
        this.data = PlayerDataManager.get(player);
        
        setTitle(Component.literal("🎒 Pocket Dashboard"));
        setupGui();
    }
    
    private void setupGui() {
        // Background
        createThemedBackground();
        
        // Header - Player info
        setSlot(4, createPlayerHead());
        setSlot(8, createWallet());
        
        // Module icons - Row 2
        setSlot(20, createModuleIcon("⛏️ Quarry", Items.IRON_PICKAXE, "quarry"));
        setSlot(22, createModuleIcon("🌾 Estate", Items.IRON_HOE, "estate"));
        setSlot(24, createModuleIcon("⚔️ Arena", Items.IRON_SWORD, "arena"));
        
        // Module icons - Row 3
        setSlot(29, createModuleIcon("⚗️ Laboratory", Items.BREWING_STAND, "laboratory"));
        setSlot(33, createModuleIcon("🚀 Terminal", Items.COMPASS, "terminal"));
        
        // Footer
        setSlot(49, createHelpButton());
        setSlot(53, createSettingsButton());
    }
    
    private GuiElementBuilder createModuleIcon(String name, Item icon, String module) {
        int activeOps = data.countActiveTabsForModule(module);
        boolean hasActive = activeOps > 0;
        
        GuiElementBuilder builder = new GuiElementBuilder()
            .setItem(icon)
            .setName(Component.literal("§f§l" + name))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Active Operations: " + 
                (hasActive ? "§a" : "§7") + activeOps))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§eClick to open"))
            .setCallback((index, type, action) -> {
                UIAction.SELECT.play(player);
                new ModuleScreen(player, module).open();
            });
        
        if (hasActive) {
            builder.glow();
        }
        
        return builder;
    }
    
    private GuiElementBuilder createWallet() {
        return new GuiElementBuilder()
            .setItem(Items.GOLD_INGOT)
            .setName(Component.literal("§6💰 Wallet"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Balance: §6$" + 
                NumberFormat.formatCurrency(data.getCoins())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Total Earned: §6$" + 
                NumberFormat.formatCurrency(data.getLifetimeEarnings())))
            .glow();
    }
    
    private void createThemedBackground() {
        // Simple border
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder()
                .setItem(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal("")));
            setSlot(45 + i, new GuiElementBuilder()
                .setItem(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal("")));
        }
    }
}
```

### Example 2: Tool Slot with Durability

```java
private GuiElementBuilder createToolSlot() {
    ItemStack tool = data.getTabTool(MODULE, selectedTab);
    
    if (tool.isEmpty()) {
        // Empty slot - Show what's needed
        return new GuiElementBuilder()
            .setItem(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
            .setName(Component.literal("§7🔧 Tool Slot"))
            .addLoreLine(Component.literal("§8Insert: §fPickaxe"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Required Tier: §fStone§7+"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§eHold tool + click to insert"))
            .setCallback((index, type, action) -> tryInsertTool());
    } else {
        // Tool inserted - Show durability
        int durability = data.getTabToolDurability(MODULE, selectedTab);
        int maxDur = data.getTabToolMaxDurability(MODULE, selectedTab);
        int percent = (durability * 100) / maxDur;
        
        String durBar = createDurabilityBar(percent);
        String durColor = percent > 50 ? "§a" : (percent > 20 ? "§e" : "§c");
        
        GuiElementBuilder builder = new GuiElementBuilder()
            .setItem(tool.getItem())
            .setName(Component.literal("§f" + formatItemName(tool.getItem())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Durability: " + durBar + " " + 
                durColor + percent + "%"))
            .addLoreLine(Component.literal("§8(" + durability + "/" + maxDur + ")"))
            .setCallback((index, type, action) -> tryRemoveTool());
        
        if (percent <= 20) {
            builder.addLoreLine(Component.literal(""))
                   .addLoreLine(Component.literal("§c⚠ LOW DURABILITY!"));
        }
        
        return builder;
    }
}

private String createDurabilityBar(int percent) {
    int filled = Math.max(0, Math.min(10, percent / 10));
    int empty = 10 - filled;
    String color = percent > 50 ? "§a" : (percent > 20 ? "§e" : "§c");
    return color + "█".repeat(filled) + "§8" + "░".repeat(empty);
}
```

---

## <a id="mistakes"></a>10. Common Mistakes to Avoid

### ❌ Mistake 1: No Visual Feedback
```java
// BAD
.setCallback((index, type, action) -> {
    doSomething();
});

// GOOD
.setCallback((index, type, action) -> {
    UIAction.CLICK.play(player);  // Sound
    doSomething();
    refresh();  // Visual update
    player.displayClientMessage(
        Component.literal("§a✓ Done!"), true
    );  // Text feedback
});
```

### ❌ Mistake 2: Poor Text Hierarchy
```java
// BAD - All same color
.setName(Component.literal("My Button"))
.addLoreLine(Component.literal("This is a description"))
.addLoreLine(Component.literal("Click to activate"))

// GOOD - Clear hierarchy
.setName(Component.literal("§f§lMy Button"))  // Bold white title
.addLoreLine(Component.literal(""))
.addLoreLine(Component.literal("§7This is a description"))  // Gray info
.addLoreLine(Component.literal(""))
.addLoreLine(Component.literal("§eClick to activate"))  // Yellow action
```

### ❌ Mistake 3: Unclear Button Purpose
```java
// BAD
.setItem(Items.STONE_BUTTON)
.setName(Component.literal("Button"))

// GOOD
.setItem(Items.EMERALD)
.setName(Component.literal("§a💰 Collect Earnings"))
.addLoreLine(Component.literal(""))
.addLoreLine(Component.literal("§7Available: §6$1,234"))
.addLoreLine(Component.literal(""))
.addLoreLine(Component.literal("§eClick to collect"))
```

### ❌ Mistake 4: No Error Handling
```java
// BAD
.setCallback((index, type, action) -> {
    data.spendCoins(1000);
    upgrade();
});

// GOOD
.setCallback((index, type, action) -> {
    if (data.getCoins() >= 1000) {
        data.spendCoins(1000);
        upgrade();
        UIAction.SUCCESS.play(player);
        player.displayClientMessage(
            Component.literal("§a✓ Upgraded!"), true
        );
    } else {
        UIAction.ERROR.play(player);
        player.displayClientMessage(
            Component.literal("§c✗ Not enough coins!"), true
        );
    }
    refresh();
});
```

### ❌ Mistake 5: Cluttered Layout
```java
// BAD - Everything crammed together
for (int i = 0; i < items.size(); i++) {
    gui.setSlot(i, createItem(items.get(i)));
}

// GOOD - Strategic spacing
int slot = 10; // Start offset
for (Item item : items) {
    gui.setSlot(slot, createItem(item));
    slot += 2; // Skip every other slot
    if (slot % 9 >= 7) slot += 3; // Skip to next row with margin
}
```

---

## Best Practices Checklist

✅ **Visual**
- [ ] Themed background (not plain gray)
- [ ] Clear visual hierarchy
- [ ] Consistent spacing
- [ ] Status indicators have colors
- [ ] Important items glow
- [ ] Progress bars use color coding

✅ **Interactive**
- [ ] Every button has clear label
- [ ] Hover text explains action
- [ ] Click callback includes sound
- [ ] Disabled items are grayed out
- [ ] Actions provide feedback

✅ **Text**
- [ ] Titles are bold and clear
- [ ] Descriptions use gray text
- [ ] Actions use yellow text
- [ ] Values are highlighted (white)
- [ ] Emojis used for quick recognition
- [ ] Numbers formatted with commas

✅ **Sound**
- [ ] Click sounds on navigation
- [ ] Success sounds on positive actions
- [ ] Error sounds on failures
- [ ] Context-specific sounds (harvest, craft, etc.)
- [ ] Volume levels are appropriate

✅ **UX**
- [ ] User always knows what will happen
- [ ] Confirmation for destructive actions
- [ ] Clear path to go back
- [ ] Help available when needed
- [ ] No dead ends

---

## Final Thoughts

**Good GUI design is:**
- **Intuitive** - Users understand without explanation
- **Responsive** - Every action has immediate feedback
- **Beautiful** - Pleasant to look at and use
- **Consistent** - Same patterns throughout
- **Polished** - Small details matter

Remember: **Players judge your mod by its interface.** A well-designed GUI makes your mod feel professional and trustworthy.

---

*Based on Pocket Life mod - GitHub: https://github.com/Uipg9/pocketlife*
