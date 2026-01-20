# 🎨 GUI Systems - Minecraft 1.21.11

**Complete GUI development guide for Minecraft 1.21.11**

> ✅ **Two Approaches**: 
> 1. **SGUI** - Server-side inventory GUIs (chest/hopper style)
> 2. **Custom DrawContext** - Fully procedural client-side GUIs (see `14_CUSTOM_GUI_SYSTEM.md`)

---

## Custom Procedural GUI (Recommended for Modern Look)

For beautiful, custom-drawn GUIs that don't look like inventory screens, see **[14_CUSTOM_GUI_SYSTEM.md](14_CUSTOM_GUI_SYSTEM.md)**.

Features:
- Gold-bordered dark theme
- No texture files needed
- Works with 1.21.11's new mouse input system
- Perfect for RPG/shop interfaces

---

## SGUI (Server-Side Inventory GUIs)

**SGUI 1.12.0+1.21.11** - For inventory-style GUIs using chest/hopper containers.

> ⚠️ **SGUI Version Critical**: Must use SGUI 1.12.0+1.21.11 - other versions incompatible.

---

## Table of Contents

1. [SGUI Setup](#setup)
2. [Basic GUI Structure](#basic-structure)
3. [SimpleGui vs PagedGui](#gui-types)
4. [Slot Management](#slot-management)
5. [Full-Screen Layout](#fullscreen-layout)
6. [Pagination System](#pagination)
7. [Real-World Example: 21-Plot Farm GUI](#farm-gui)

---

## <a id="setup"></a>SGUI Setup

### Add SGUI Dependency

**build.gradle:**
```gradle
repositories {
    mavenCentral()
    maven { url 'https://maven.nucleoid.xyz' }
}

dependencies {
    // ... existing dependencies ...
    
    // SGUI 1.12.0 for 1.21.11
    include(modImplementation("eu.pb4:sgui:1.12.0+1.21.11"))
}
```

Then reload Gradle: `.\gradlew.bat build`

### Essential Imports

```java
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
```

---

## <a id="basic-structure"></a>Basic GUI Structure

### Minimal Working GUI

```java
public class BasicGui extends SimpleGui {
    
    public BasicGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x3, player, false);
        this.setTitle(Component.literal("§6My GUI"));
        
        // Add a button at slot 13 (center of 9x3)
        this.setSlot(13, new GuiElementBuilder()
            .setItem(Items.DIAMOND)
            .setName(Component.literal("§bClick Me"))
            .setCallback((index, type, action) -> {
                player.sendSystemMessage(Component.literal("§aYou clicked the diamond!"));
            })
        );
    }
    
    public void open() {
        this.open();
    }
}
```

### Opening the GUI

```java
// In your command or event
BasicGui gui = new BasicGui(player);
gui.open();
```

---

## <a id="gui-types"></a>SimpleGui vs PagedGui

### SimpleGui - Fixed Content

Use for: Single-page GUIs, menus, settings screens

```java
public class ShopGui extends SimpleGui {
    public ShopGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x3, player, false);
        
        setupShopItems();
    }
    
    private void setupShopItems() {
        // Row 1: Items for sale
        this.setSlot(10, createShopItem(Items.DIAMOND, 100));
        this.setSlot(11, createShopItem(Items.EMERALD, 50));
        // ...
    }
    
    private GuiElementBuilder createShopItem(Item item, int price) {
        return new GuiElementBuilder()
            .setItem(item)
            .setName(Component.literal("§aBuy for $" + price))
            .setCallback((index, type, action) -> {
                // Purchase logic
            });
    }
}
```

### PagedGui - Dynamic Content

Use for: Lists, inventories, collections that span multiple pages

```java
import eu.pb4.sgui.api.gui.PagedGui;

public class CollectionGui extends PagedGui {
    
    public CollectionGui(ServerPlayer player, List<ItemStack> items) {
        super(MenuType.GENERIC_9x6, player, false);
        this.setTitle(Component.literal("§6Collection"));
        
        // Add all items
        for (ItemStack item : items) {
            this.addElement(new GuiElementBuilder()
                .setItem(item)
                .setCallback((index, type, action) -> {
                    // Item click handler
                })
            );
        }
        
        // Add navigation buttons
        setupNavigation();
    }
    
    private void setupNavigation() {
        // Previous page button (slot 45, bottom-left)
        this.setSlot(45, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Component.literal("§7← Previous Page"))
            .setCallback((index, type, action) -> {
                this.previousPage();
            })
        );
        
        // Next page button (slot 53, bottom-right)
        this.setSlot(53, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Component.literal("§7Next Page →"))
            .setCallback((index, type, action) -> {
                this.nextPage();
            })
        );
    }
}
```

---

## <a id="slot-management"></a>Slot Management

### Understanding Slot Numbers

```
9x3 GUI (27 slots):
┌─────────────────────────────────────┐
│  0   1   2   3   4   5   6   7   8  │
│  9  10  11  12  13  14  15  16  17  │
│ 18  19  20  21  22  23  24  25  26  │
└─────────────────────────────────────┘

9x6 GUI (54 slots):
┌─────────────────────────────────────┐
│  0   1   2   3   4   5   6   7   8  │  ← Top row
│  9  10  11  12  13  14  15  16  17  │
│ 18  19  20  21  22  23  24  25  26  │
│ 27  28  29  30  31  32  33  34  35  │
│ 36  37  38  39  40  41  42  43  44  │
│ 45  46  47  48  49  50  51  52  53  │  ← Bottom row
└─────────────────────────────────────┘
```

### Calculating Slot Positions

```java
// Convert (row, col) to slot number
public int getSlot(int row, int col) {
    return row * 9 + col;
}

// Examples:
// Top-left corner: getSlot(0, 0) = 0
// Center of 9x3: getSlot(1, 4) = 13
// Bottom-right of 9x6: getSlot(5, 8) = 53
```

### Common Layouts

```java
// Border (9x3)
private void addBorder() {
    GuiElementBuilder border = new GuiElementBuilder()
        .setItem(Items.GRAY_STAINED_GLASS_PANE)
        .setName(Component.literal(" "));
    
    // Top row
    for (int i = 0; i < 9; i++) this.setSlot(i, border);
    
    // Bottom row
    for (int i = 18; i < 27; i++) this.setSlot(i, border);
    
    // Sides
    this.setSlot(9, border);
    this.setSlot(17, border);
}

// Center item (9x3)
private void setCenterItem(GuiElementBuilder item) {
    this.setSlot(13, item);  // Slot 13 is center
}

// Navigation bar (bottom row, 9x6)
private void addNavigationBar() {
    this.setSlot(45, backButton);      // Bottom-left
    this.setSlot(49, mainButton);      // Bottom-center
    this.setSlot(53, nextButton);      // Bottom-right
}
```

---

## <a id="fullscreen-layout"></a>Full-Screen Layout (9x6)

### Complete Layout Template

```java
public class FullScreenGui extends SimpleGui {
    
    public FullScreenGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.setTitle(Component.literal("§6Full Screen GUI"));
        
        setupTopBar();
        setupContentArea();
        setupBottomBar();
    }
    
    private void setupTopBar() {
        // Slots 0-8: Top controls
        this.setSlot(0, new GuiElementBuilder()
            .setItem(Items.BARRIER)
            .setName(Component.literal("§cClose"))
            .setCallback((index, type, action) -> this.close())
        );
        
        this.setSlot(4, new GuiElementBuilder()
            .setItem(Items.BOOK)
            .setName(Component.literal("§eInfo"))
        );
        
        this.setSlot(8, new GuiElementBuilder()
            .setItem(Items.WRITABLE_BOOK)
            .setName(Component.literal("§bSettings"))
        );
    }
    
    private void setupContentArea() {
        // Slots 9-44: Main content (4 rows × 9 columns)
        int[] contentSlots = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
        };
        
        for (int slot : contentSlots) {
            // Add content items
        }
    }
    
    private void setupBottomBar() {
        // Slots 45-53: Bottom controls
        this.setSlot(45, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Component.literal("§7← Back"))
            .setCallback((index, type, action) -> {
                // Navigation logic
            })
        );
        
        this.setSlot(49, new GuiElementBuilder()
            .setItem(Items.EMERALD)
            .setName(Component.literal("§aConfirm"))
        );
        
        this.setSlot(53, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Component.literal("§7Next →"))
        );
    }
}
```

---

## <a id="pagination"></a>Pagination System

### Manual Pagination (Custom Control)

```java
public class CustomPagedGui extends SimpleGui {
    private List<GuiElementBuilder> items;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 21;  // 3 rows × 7 columns
    
    public CustomPagedGui(ServerPlayer player, List<GuiElementBuilder> items) {
        super(MenuType.GENERIC_9x6, player, false);
        this.items = items;
        
        updatePage();
        setupControls();
    }
    
    private void updatePage() {
        // Clear content area (slots 10-16, 19-25, 28-34)
        int[][] contentSlots = {
            {10, 11, 12, 13, 14, 15, 16},  // Row 2
            {19, 20, 21, 22, 23, 24, 25},  // Row 3
            {28, 29, 30, 31, 32, 33, 34}   // Row 4
        };
        
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int itemIndex = 0;
        
        for (int[] row : contentSlots) {
            for (int slot : row) {
                int globalIndex = startIndex + itemIndex;
                
                if (globalIndex < items.size()) {
                    this.setSlot(slot, items.get(globalIndex));
                } else {
                    // Empty slot
                    this.clearSlot(slot);
                }
                
                itemIndex++;
            }
        }
        
        // Update title with page number
        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        this.setTitle(Component.literal(String.format("§6GUI - Page %d/%d", currentPage + 1, totalPages)));
    }
    
    private void setupControls() {
        // Previous page
        this.setSlot(45, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Component.literal("§7← Previous"))
            .setCallback((index, type, action) -> {
                if (currentPage > 0) {
                    currentPage--;
                    updatePage();
                }
            })
        );
        
        // Next page
        this.setSlot(53, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Component.literal("§7Next →"))
            .setCallback((index, type, action) -> {
                int maxPage = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE) - 1;
                if (currentPage < maxPage) {
                    currentPage++;
                    updatePage();
                }
            })
        );
        
        // Page indicator
        this.setSlot(49, new GuiElementBuilder()
            .setItem(Items.PAPER)
            .setName(Component.literal("§ePage " + (currentPage + 1)))
        );
    }
}
```

---

## <a id="farm-gui"></a>Real-World Example: 21-Plot Farm GUI

### From Pocket Estate Mod v1.2.0

Complete working GUI with:
- 21 plot slots (7×3 grid)
- 9 control buttons
- Pagination support (3 plots per page × 7 pages)
- Dynamic updates

```java
public class FieldsGui extends SimpleGui {
    private final ServerPlayer player;
    private final VirtualCropManager cropManager;
    private int currentPage = 0;
    private static final int PLOTS_PER_PAGE = 21;
    
    // Layout definition
    private final int[][] plotSlots = {
        {10, 11, 12, 13, 14, 15, 16},  // Row 2: Plots 1-7
        {19, 20, 21, 22, 23, 24, 25},  // Row 3: Plots 8-14
        {28, 29, 30, 31, 32, 33, 34}   // Row 4: Plots 15-21
    };
    
    public FieldsGui(ServerPlayer player, VirtualCropManager cropManager) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.cropManager = cropManager;
        
        setupControlButtons();
        updatePlots();
    }
    
    private void setupControlButtons() {
        // Row 1: Control buttons (slots 0-8)
        
        // Slot 0: Back button
        this.setSlot(0, new GuiElementBuilder()
            .setItem(Items.BARRIER)
            .setName(Component.literal("§cBack"))
            .setCallback((index, type, action) -> {
                new MainGui(player).open();
            })
        );
        
        // Slot 2: Statistics
        this.setSlot(2, new GuiElementBuilder()
            .setItem(Items.PAPER)
            .setName(Component.literal("§eStatistics"))
            .addLoreLine(Component.literal("§7Total Plots: " + cropManager.getTotalPlots()))
            .addLoreLine(Component.literal("§7Active Crops: " + cropManager.getActiveCrops()))
        );
        
        // Slot 3: Plant All
        this.setSlot(3, new GuiElementBuilder()
            .setItem(Items.WHEAT_SEEDS)
            .setName(Component.literal("§aPlant All"))
            .setCallback((index, type, action) -> {
                buildCropSelector(null); // Opens crop selection
            })
        );
        
        // Slot 4: Harvest All
        this.setSlot(4, new GuiElementBuilder()
            .setItem(Items.WHEAT)
            .setName(Component.literal("§6Harvest All"))
            .setCallback((index, type, action) -> {
                harvestAll();
            })
        );
        
        // Slot 5: Bonemeal Storage
        this.setSlot(5, new GuiElementBuilder()
            .setItem(Items.BONE_MEAL)
            .setName(Component.literal("§fBonemeal Storage"))
            .setCount(Math.min(64, cropManager.getBonemealCount()))
            .addLoreLine(Component.literal("§7Stored: " + cropManager.getBonemealCount()))
            .setCallback((index, type, action) -> {
                openBonemealGui();
            })
        );
        
        // Slot 6: Compost Bin
        this.setSlot(6, new GuiElementBuilder()
            .setItem(Items.COMPOSTER)
            .setName(Component.literal("§2Compost Bin"))
            .setCallback((index, type, action) -> {
                openCompostGui();
            })
        );
        
        // Slot 7: Buy Plots
        this.setSlot(7, new GuiElementBuilder()
            .setItem(Items.EMERALD)
            .setName(Component.literal("§aBuy More Plots"))
            .addLoreLine(Component.literal("§7Cost: $" + cropManager.getNextPlotCost()))
            .setCallback((index, type, action) -> {
                buyPlot();
            })
        );
        
        // Slot 8: Auto-Harvest Toggle
        this.setSlot(8, new GuiElementBuilder()
            .setItem(cropManager.isAutoHarvestEnabled() ? Items.LIME_DYE : Items.GRAY_DYE)
            .setName(Component.literal(cropManager.isAutoHarvestEnabled() ? "§aAuto-Harvest: ON" : "§7Auto-Harvest: OFF"))
            .setCallback((index, type, action) -> {
                cropManager.toggleAutoHarvest();
                updatePlots();
            })
        );
        
        // Row 6: Navigation (slots 45, 49, 53)
        
        // Previous Page
        this.setSlot(45, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Component.literal("§7← Previous Page"))
            .setCallback((index, type, action) -> {
                if (currentPage > 0) {
                    currentPage--;
                    updatePlots();
                }
            })
        );
        
        // Collect All Output
        this.setSlot(49, new GuiElementBuilder()
            .setItem(Items.CHEST)
            .setName(Component.literal("§eCollect All Output"))
            .setCallback((index, type, action) -> {
                collectAllOutput();
            })
        );
        
        // Next Page
        this.setSlot(53, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Component.literal("§7Next Page →"))
            .setCallback((index, type, action) -> {
                int maxPage = (cropManager.getTotalPlots() - 1) / PLOTS_PER_PAGE;
                if (currentPage < maxPage) {
                    currentPage++;
                    updatePlots();
                }
            })
        );
    }
    
    private void updatePlots() {
        int startPlotIndex = currentPage * PLOTS_PER_PAGE;
        int plotIndex = 0;
        
        for (int row = 0; row < plotSlots.length; row++) {
            for (int col = 0; col < plotSlots[row].length; col++) {
                int slot = plotSlots[row][col];
                int globalPlotIndex = startPlotIndex + plotIndex;
                
                if (globalPlotIndex < cropManager.getTotalPlots()) {
                    VirtualPlot plot = cropManager.getPlot(globalPlotIndex);
                    this.setSlot(slot, createPlotElement(plot, globalPlotIndex));
                } else {
                    // Locked plot
                    this.setSlot(slot, new GuiElementBuilder()
                        .setItem(Items.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.literal("§7Locked Plot"))
                        .addLoreLine(Component.literal("§7Buy more plots to unlock!"))
                    );
                }
                
                plotIndex++;
            }
        }
        
        // Update title
        int totalPages = (cropManager.getTotalPlots() - 1) / PLOTS_PER_PAGE + 1;
        this.setTitle(Component.literal(String.format("§6Fields - Page %d/%d", currentPage + 1, totalPages)));
    }
    
    private GuiElementBuilder createPlotElement(VirtualPlot plot, int plotIndex) {
        if (plot.isEmpty()) {
            // Empty plot - click to plant
            return new GuiElementBuilder()
                .setItem(Items.DIRT)
                .setName(Component.literal("§7Plot #" + (plotIndex + 1)))
                .addLoreLine(Component.literal("§eClick to plant!"))
                .setCallback((index, type, action) -> {
                    buildCropSelector(plotIndex);
                });
        } else {
            // Growing crop
            Item displayItem = plot.getCropType().getSeedItem();
            int growthPercent = (int) (plot.getGrowthProgress() * 100);
            
            GuiElementBuilder builder = new GuiElementBuilder()
                .setItem(displayItem)
                .setName(Component.literal("§aPlot #" + (plotIndex + 1)))
                .addLoreLine(Component.literal("§7Crop: §f" + plot.getCropType().getName()))
                .addLoreLine(Component.literal("§7Growth: §e" + growthPercent + "%"));
            
            if (plot.isReadyToHarvest()) {
                builder.addLoreLine(Component.literal("§a§lREADY TO HARVEST!"));
                builder.addLoreLine(Component.literal("§eClick to harvest"));
                builder.setCallback((index, type, action) -> {
                    harvest(plotIndex);
                });
            } else {
                builder.addLoreLine(Component.literal("§7Right-click: Bonemeal"));
                builder.setCallback((index, type, action) -> {
                    if (action.isRightClick) {
                        applyBonemeal(plotIndex);
                    }
                });
            }
            
            return builder;
        }
    }
    
    private void buildCropSelector(Integer targetPlot) {
        // Opens another GUI to select crop type
        new CropSelectorGui(player, cropManager, targetPlot, this).open();
    }
    
    private void harvestAll() {
        int harvested = cropManager.harvestAllReady();
        player.sendSystemMessage(Component.literal("§a[FARM] Harvested " + harvested + " plots!"));
        updatePlots();
    }
    
    private void harvest(int plotIndex) {
        VirtualPlot plot = cropManager.getPlot(plotIndex);
        if (plot.harvest()) {
            player.sendSystemMessage(Component.literal("§a[FARM] Harvested " + plot.getCropType().getName() + "!"));
            updatePlots();
        }
    }
    
    private void applyBonemeal(int plotIndex) {
        if (cropManager.useBonemeal(1)) {
            VirtualPlot plot = cropManager.getPlot(plotIndex);
            plot.applyBonemeal();
            player.sendSystemMessage(Component.literal("§a[FARM] Applied bonemeal!"));
            updatePlots();
        } else {
            player.sendSystemMessage(Component.literal("§c[FARM] No bonemeal in storage!"));
        }
    }
    
    private void collectAllOutput() {
        Map<Item, Integer> output = cropManager.collectAllOutput();
        // Transfer items to player inventory
        player.sendSystemMessage(Component.literal("§a[FARM] Collected all output!"));
    }
    
    private void buyPlot() {
        if (cropManager.buyPlot(player)) {
            player.sendSystemMessage(Component.literal("§a[FARM] Purchased new plot!"));
            updatePlots();
        } else {
            player.sendSystemMessage(Component.literal("§c[FARM] Not enough money!"));
        }
    }
    
    private void openBonemealGui() {
        // GUI for adding/removing bonemeal
    }
    
    private void openCompostGui() {
        // GUI for composting items into bonemeal
    }
}
```

---

## Key Takeaways

✅ **Use SGUI 1.12.0+1.21.11** for inventory GUIs
✅ **9x6 MenuType** for full-screen layouts
✅ **Slot planning** is critical - draw your layout first
✅ **updatePage()** pattern for dynamic content
✅ **GuiElementBuilder** for button creation
✅ **setCallback()** for click handling

---

## Next Steps

Continue to:
- [04_COMMANDS.md](04_COMMANDS.md) - Command integration with GUIs
- [05_DATA_STORAGE.md](05_DATA_STORAGE.md) - Save GUI state
- [08_PATTERNS.md](08_PATTERNS.md) - More GUI patterns

---

**Version Note:** All code is for SGUI 1.12.0+1.21.11 with Mojang mappings.
