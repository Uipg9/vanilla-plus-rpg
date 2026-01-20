# Shop Layout & Sorting System

## Overview
The shop GUI uses a catalog-style layout with categorized items, pagination, and logical sorting within categories.

## Window Dimensions
```java
windowWidth = Math.min(420, width - 20);
windowHeight = Math.min(360, height - 20);
gridStartY = windowY + 100; // Space for categories and balance
```

## Category System

### Category Tabs
- **Position:** `windowY + 44` (centered horizontally)
- **Tab Width:** 38px
- **Tab Spacing:** 2px
- **Layout:** Centered across window width

```java
int tabWidth = 38;
int tabSpacing = 2;
int totalTabsWidth = ShopCategory.values().length * (tabWidth + tabSpacing) - tabSpacing;
int tabX = windowX + (windowWidth - totalTabsWidth) / 2; // Center categories
```

### Categories
1. **All** - Shows all items
2. **Tools** - Pickaxes, axes, shovels, hoes
3. **Weapons** - Swords, bows, arrows
4. **Armor** - All armor tiers
5. **Food** - Cooked meats, crops, special foods
6. **Building** - Blocks, decorations, crafting stations
7. **Farming** - Seeds, farming tools, animal items
8. **Ores** - Raw materials, ingots, gems
9. **Misc** - Everything else

## Page Navigation

### Position
```java
int pageNavY = windowY + 64; // Below categories (which end at ~58)
```

### Controls
- **Previous button:** `windowX + 10`, width 24px
- **Next button:** `windowX + windowWidth - 34`, width 24px
- **Page indicator:** Centered between buttons

This ensures no overlap with category tabs!

## Item Sorting

### Logical Grouping
Items are sorted within categories to group similar items together.

#### Tools Category Order
1. **Pickaxes** (Wooden → Stone → Iron → Golden → Diamond → Netherite)
2. **Axes** (same tier order)
3. **Shovels** (same tier order)
4. **Hoes** (same tier order)
5. **Misc Tools** (Fishing Rod, Shears, Compass, etc.)

#### Weapons Category Order
1. **Swords** (Wooden → Stone → Iron → Golden → Diamond → Netherite)
2. **Ranged** (Bow, Crossbow, Trident, Mace)
3. **Defense** (Shield)
4. **Ammo** (Arrow, Spectral Arrow, Tipped Arrow)

#### Armor Category Order
1. **Leather** (Helmet → Chestplate → Leggings → Boots)
2. **Chainmail** (same slot order)
3. **Iron** (same slot order)
4. **Golden** (same slot order)
5. **Diamond** (same slot order)
6. **Netherite** (same slot order)
7. **Special** (Turtle Helmet, Elytra)

#### Food Category Order
1. **Cooked Meats** (Beef, Pork, Chicken, etc.)
2. **Baked Foods** (Bread, Baked Potato)
3. **Fruits** (Apple, Golden Apple, Berries)
4. **Special** (Golden Carrot, Honey, Stews)

#### Ores Category Order
1. **Coal** (Raw, Ingot, Block progression)
2. **Copper** (Raw → Ingot → Block)
3. **Iron** (Raw → Ingot → Block)
4. **Gold** (Raw → Ingot → Block)
5. **Diamond** (Gem → Block)
6. **Emerald** (Gem → Block)
7. **Other Gems** (Lapis, Redstone, Quartz)
8. **Netherite** (Scrap → Ingot → Block)
9. **Mob Drops** (Blaze Rod, Ender Pearl, etc.)

### Implementation

```java
private void sortItemsLogically() {
    Map<Item, Integer> itemOrder = new HashMap<>();
    int order = 0;
    
    // Define order for each item type
    for (Item item : List.of(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, ...)) {
        itemOrder.put(item, order++);
    }
    
    // Sort with fallback to alphabetical
    filteredItems.sort((a, b) -> {
        int orderA = itemOrder.getOrDefault(a, 9999);
        int orderB = itemOrder.getOrDefault(b, 9999);
        if (orderA != orderB) {
            return Integer.compare(orderA, orderB);
        }
        return a.getDescriptionId().compareTo(b.getDescriptionId());
    });
}
```

### Calling Sort
```java
private void applyFilter() {
    // ... filter items by category ...
    
    // Sort items within the filter
    sortItemsLogically();
    currentPage = 0;
}
```

## Grid Layout

### Item Grid
- **Columns:** 7 items per row
- **Rows:** 5 items per column
- **Items per page:** 35 items
- **Item size:** 28x28 pixels
- **Item spacing:** 4 pixels

### Position Calculation
```java
int gridWidth = GRID_COLS * (ITEM_SIZE + ITEM_SPACING) - ITEM_SPACING;
gridStartX = windowX + (windowWidth - gridWidth) / 2; // Center grid
```

## Visual Styling

### Category Tabs
```java
// Selected tab
int bgColor = 0xCC444444;
int borderColor = cat.color; // Category-specific color

// Hovered tab
int bgColor = 0xCC333333;
int textColor = 0xFFFFFFFF;

// Normal tab
int bgColor = 0xCC222222;
int textColor = 0xFFAAAAAA;
```

### Page Controls
```java
// Hovered
graphics.fill(x, y, x + width, y + height, COLOR_BUTTON_HOVER);
drawText(graphics, "<<", x + 5, y + 3, COLOR_GOLD_TEXT);

// Normal
graphics.fill(x, y, x + width, y + height, COLOR_BUTTON_BG);
drawText(graphics, "<<", x + 5, y + 3, COLOR_WHITE_TEXT);
```

## Interaction

### Category Switching
```java
Button catBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
    playClickSound();
    currentCategory = category;
    applyFilter(); // Filters AND sorts
    refreshScreen();
}).bounds(tabX, tabY, tabWidth, 14).build());
```

### Page Navigation
- Previous button at fixed left position
- Next button at fixed right position  
- Both have 24px width for better clickability
- Only shown when applicable (page > 0, or page < totalPages-1)

## Layout Evolution

### Problem: Category tabs overlapping with page controls
**Old layout:**
- Window: 360x320
- Categories: 9 tabs × 38px = 342px (tight fit)
- Page controls at `gridStartY - 18`

**Solution:**
- Widened window to 420x360
- Reduced tab width to 38px
- Moved page controls to fixed `windowY + 64`
- Centered both categories and page controls
- Added more vertical spacing

### Result
- Categories at y=44 (height 14) → end at y=58
- Page controls at y=64 → start at y=64
- **6px gap** prevents overlap
- Grid starts at y=100 → plenty of space

## Testing Checklist

- [ ] All categories visible without wrapping
- [ ] Page controls don't overlap with categories
- [ ] Items sorted logically within categories
- [ ] All pickaxes appear together
- [ ] All axes appear together
- [ ] Armor organized by tier then slot
- [ ] Food shows cooked meats first
- [ ] Ores show progression (raw → ingot → block)
- [ ] Pagination works correctly
- [ ] Category switching maintains sort order
