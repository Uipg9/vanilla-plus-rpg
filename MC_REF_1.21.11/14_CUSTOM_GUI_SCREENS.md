# 14. Custom GUI Screens - DrawContext API (1.21.11)

> **No SGUI, No GooeyLibs, No Chest Interfaces** - Pure procedural rendering with Minecraft's built-in DrawContext/GuiGraphics API.

This guide covers creating custom, modern-looking GUI screens in Fabric 1.21.11 without external libraries.

## Table of Contents
1. [Philosophy](#philosophy)
2. [Base Screen Class](#base-screen-class)
3. [Drawing Primitives](#drawing-primitives)
4. [Button Handling](#button-handling)
5. [Shift Key Detection (1.21.11 Specific)](#shift-key-detection)
6. [Screen Lifecycle](#screen-lifecycle)
7. [Complete Examples](#complete-examples)
8. [Common Pitfalls](#common-pitfalls)

---

## Philosophy

### Why Custom Screens?

Many mods use SGUI or GooeyLibs which force you into chest-style inventory interfaces. For a modern, sleek aesthetic, you want:

- **Procedural rendering** - Draw shapes, gradients, text with code
- **Dark mode aesthetic** - Transparent backgrounds, gold accents
- **No texture files** - Everything drawn programmatically
- **Catalog-style layouts** - Not limited to 9x6 slot grids

### Design Guidelines

```
┌─────────────────────────────────┐  ← 1px gold border (#FFD700)
│  ✦ Title ✦                      │  ← Centered, shadowed, gold text
│                                 │
│  ┌─────────┐  ┌─────────┐       │  ← Custom buttons with hover states
│  │ Button1 │  │ Button2 │       │
│  └─────────┘  └─────────┘       │
│                                 │
│  [ Content Area ]               │  ← Gradient background (black → blue)
│                                 │
└─────────────────────────────────┘

Background: 90% transparent black → midnight blue gradient
Border: 1px solid gold
Text: White body, gold titles, gray hints
Buttons: Dark background, gold border, highlight on hover
```

---

## Base Screen Class

### The Foundation

Create a base class that all your screens extend:

```java
package com.yourmod.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class BaseModScreen extends Screen {
    
    // Color palette
    protected static final int COLOR_BG_TOP = 0xE6000000;      // 90% black
    protected static final int COLOR_BG_BOTTOM = 0xE6001428;   // Midnight blue
    protected static final int COLOR_GOLD = 0xFFFFD700;        // Gold
    protected static final int COLOR_WHITE = 0xFFFFFFFF;
    protected static final int COLOR_GRAY = 0xFF888888;
    protected static final int COLOR_HIGHLIGHT = 0x40FFFFFF;   // Hover highlight
    protected static final int COLOR_BUTTON_BG = 0xCC222222;
    protected static final int COLOR_BUTTON_HOVER = 0xCC444444;
    
    // Window bounds (calculated in init())
    protected int windowX, windowY, windowWidth, windowHeight;
    
    protected BaseModScreen(Component title) {
        super(title);
    }
    
    @Override
    protected void init() {
        super.init();
        // Center window with padding
        windowWidth = Math.min(320, width - 40);
        windowHeight = Math.min(240, height - 40);
        windowX = (width - windowWidth) / 2;
        windowY = (height - windowHeight) / 2;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // 1. Dark background (blur effect)
        graphics.fill(0, 0, width, height, 0x80000000);
        
        // 2. Window with gradient
        graphics.fillGradient(windowX, windowY, 
            windowX + windowWidth, windowY + windowHeight,
            COLOR_BG_TOP, COLOR_BG_BOTTOM);
        
        // 3. Gold border
        drawBorder(graphics, windowX, windowY, windowWidth, windowHeight, COLOR_GOLD);
        
        // 4. Title
        drawCenteredText(graphics, getTitle().getString(), 
            windowX + windowWidth / 2, windowY + 10, COLOR_GOLD);
        
        // 5. Content (subclass)
        renderContent(graphics, mouseX, mouseY, delta);
        
        // 6. IMPORTANT: Call super to render widgets!
        super.render(graphics, mouseX, mouseY, delta);
    }
    
    protected abstract void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta);
    
    @Override
    public boolean isPauseScreen() {
        return false;  // Don't pause game
    }
}
```

### Critical: Always Call `super.render()`

The most common mistake is forgetting `super.render()`:

```java
@Override
public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    // Your drawing code...
    
    // ⚠️ MUST call super.render() or widgets won't work!
    super.render(graphics, mouseX, mouseY, delta);
}
```

---

## Drawing Primitives

### GuiGraphics Methods (1.21.11)

```java
// Solid fill
graphics.fill(x1, y1, x2, y2, color);

// Vertical gradient
graphics.fillGradient(x1, y1, x2, y2, colorTop, colorBottom);

// Text with shadow
graphics.drawString(font, "text", x, y, color, true);  // true = shadow

// Text without shadow
graphics.drawString(font, "text", x, y, color, false);

// Render item
graphics.renderItem(itemStack, x, y);

// Item with count overlay
graphics.renderItemDecorations(font, itemStack, x, y);

// Tooltip
graphics.renderTooltip(font, itemStack, mouseX, mouseY);
```

### Helper Methods

```java
/**
 * Draw 1px border
 */
protected void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
    graphics.fill(x, y, x + w, y + 1, color);           // Top
    graphics.fill(x, y + h - 1, x + w, y + h, color);   // Bottom
    graphics.fill(x, y, x + 1, y + h, color);           // Left
    graphics.fill(x + w - 1, y, x + w, y + h, color);   // Right
}

/**
 * Draw centered text with shadow
 */
protected void drawCenteredText(GuiGraphics g, String text, int centerX, int y, int color) {
    int w = font.width(text);
    g.drawString(font, text, centerX - w / 2, y, color, true);
}

/**
 * Check if mouse is in bounds
 */
protected boolean isMouseOver(int mouseX, int mouseY, int x, int y, int w, int h) {
    return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
}

/**
 * Draw styled button
 */
protected void drawButton(GuiGraphics g, int x, int y, int w, int h, 
                          String label, boolean hovered) {
    // Background
    g.fill(x, y, x + w, y + h, hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON_BG);
    
    // Border
    drawBorder(g, x, y, w, h, COLOR_GOLD);
    
    // Label
    int textW = font.width(label);
    g.drawString(font, label, x + (w - textW) / 2, y + (h - 8) / 2,
        hovered ? COLOR_GOLD : COLOR_WHITE, true);
}
```

---

## Button Handling

### Two Approaches

#### 1. Invisible Minecraft Buttons (Recommended)

Use Minecraft's Button widget but draw your own visuals:

```java
@Override
protected void init() {
    super.init();
    
    int btnX = windowX + 50;
    int btnY = windowY + 60;
    
    // Add invisible button for click handling
    addRenderableWidget(Button.builder(Component.literal(""), button -> {
        // Handle click
        openShop();
    }).bounds(btnX, btnY, 100, 20).build());
}

@Override
protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    // Draw your styled button on top
    drawButton(graphics, btnX, btnY, 100, 20, "Shop", 
        isMouseOver(mouseX, mouseY, btnX, btnY, 100, 20));
}
```

#### 2. Manual Click Detection

Override `mouseClicked` (note: no `@Override` annotation in 1.21.11):

```java
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    int mx = (int) mouseX;
    int my = (int) mouseY;
    
    if (isMouseOver(mx, my, shopBtnX, shopBtnY, 100, 20)) {
        openShop();
        return true;
    }
    
    return super.mouseClicked(mouseX, mouseY, button);
}
```

**⚠️ 1.21.11 Note:** The `mouseClicked` signature changed - do NOT add `@Override`.

---

## Shift Key Detection

### The Problem

`Screen.hasShiftDown()` **does not exist** in 1.21.11!

### The Solution: Direct GLFW Input

```java
import org.lwjgl.glfw.GLFW;

protected boolean isShiftDown() {
    long window = GLFW.glfwGetCurrentContext();
    return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
           GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
}
```

### Usage Example

```java
@Override
protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    // Show hidden content when shift held
    if (isShiftDown()) {
        drawCenteredText(graphics, "§5Secret Menu!", centerX, y, 0xFFAA00AA);
    }
}

public boolean mouseClicked(double mouseX, double mouseY, int button) {
    int amount = isShiftDown() ? 64 : 1;  // Shift+click = stack
    buyItem(item, amount);
    return true;
}
```

---

## Screen Lifecycle

### Opening Screens

```java
// From anywhere with Minecraft instance
Minecraft.getInstance().setScreen(new MyScreen());

// From within a screen
this.minecraft.setScreen(new OtherScreen());

// Close current screen
this.onClose();
// or
Minecraft.getInstance().setScreen(null);
```

### init() vs Constructor

```java
public class MyScreen extends Screen {
    private int buttonX;  // Calculated in init()
    
    public MyScreen() {
        super(Component.literal("My Screen"));
        // ❌ Don't access width/height here - they're 0!
    }
    
    @Override
    protected void init() {
        super.init();
        // ✅ Now width/height are set
        buttonX = width / 2 - 50;
    }
}
```

### onClose()

```java
@Override
public void onClose() {
    // Cleanup, save state, etc.
    sellAllItems();  // Sell-on-close mechanic
    super.onClose();
}
```

---

## Complete Examples

### Simple Hub Screen

```java
public class HubScreen extends BaseModScreen {
    
    private int shopBtnY, binBtnY, skillsBtnY;
    private static final int BTN_WIDTH = 150;
    private static final int BTN_HEIGHT = 24;
    
    public HubScreen() {
        super(Component.literal("✦ Hub ✦"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        int btnX = windowX + (windowWidth - BTN_WIDTH) / 2;
        shopBtnY = windowY + 50;
        binBtnY = shopBtnY + BTN_HEIGHT + 8;
        skillsBtnY = binBtnY + BTN_HEIGHT + 8;
        
        // Invisible click handlers
        addRenderableWidget(Button.builder(Component.empty(), b -> 
            minecraft.setScreen(new ShopScreen())
        ).bounds(btnX, shopBtnY, BTN_WIDTH, BTN_HEIGHT).build());
        
        addRenderableWidget(Button.builder(Component.empty(), b -> 
            minecraft.setScreen(new BinScreen())
        ).bounds(btnX, binBtnY, BTN_WIDTH, BTN_HEIGHT).build());
        
        addRenderableWidget(Button.builder(Component.empty(), b -> 
            minecraft.setScreen(new SkillsScreen())
        ).bounds(btnX, skillsBtnY, BTN_WIDTH, BTN_HEIGHT).build());
    }
    
    @Override
    protected void renderContent(GuiGraphics g, int mx, int my, float delta) {
        int btnX = windowX + (windowWidth - BTN_WIDTH) / 2;
        
        drawButton(g, btnX, shopBtnY, BTN_WIDTH, BTN_HEIGHT, "[ Shop ]",
            isMouseOver(mx, my, btnX, shopBtnY, BTN_WIDTH, BTN_HEIGHT));
            
        drawButton(g, btnX, binBtnY, BTN_WIDTH, BTN_HEIGHT, "[ Shipping Bin ]",
            isMouseOver(mx, my, btnX, binBtnY, BTN_WIDTH, BTN_HEIGHT));
            
        drawButton(g, btnX, skillsBtnY, BTN_WIDTH, BTN_HEIGHT, "[ My Skills ]",
            isMouseOver(mx, my, btnX, skillsBtnY, BTN_WIDTH, BTN_HEIGHT));
        
        // Footer hint
        drawCenteredText(g, "§7Press ESC to close", 
            windowX + windowWidth / 2, windowY + windowHeight - 15, COLOR_GRAY);
    }
}
```

### Catalog Grid (Shop)

```java
@Override
protected void renderContent(GuiGraphics g, int mx, int my, float delta) {
    int cols = 6, rows = 4;
    int slotSize = 24;
    int spacing = 4;
    
    for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
            int index = page * cols * rows + row * cols + col;
            if (index >= items.size()) break;
            
            Item item = items.get(index);
            int x = gridX + col * (slotSize + spacing);
            int y = gridY + row * (slotSize + spacing);
            
            boolean hover = isMouseOver(mx, my, x, y, slotSize, slotSize);
            
            // Slot background
            g.fill(x, y, x + slotSize, y + slotSize, 
                hover ? 0x60FFFFFF : 0x40000000);
            
            // Border
            drawBorder(g, x, y, slotSize, slotSize, 
                hover ? COLOR_GOLD : 0x80888888);
            
            // Item
            g.renderItem(new ItemStack(item), x + 4, y + 4);
            
            if (hover) {
                hoveredItem = item;
            }
        }
    }
    
    // Tooltip
    if (hoveredItem != null) {
        drawTooltip(g, mx, my, hoveredItem);
    }
}
```

---

## Common Pitfalls

### 1. Screen.hasShiftDown() Doesn't Exist

```java
// ❌ WRONG - method doesn't exist in 1.21.11
if (Screen.hasShiftDown()) { ... }

// ✅ CORRECT - use GLFW
if (isShiftDown()) { ... }
```

### 2. Forgetting super.render()

```java
// ❌ WRONG - widgets won't render or respond
@Override
public void render(GuiGraphics g, int mx, int my, float d) {
    drawStuff(g);
}

// ✅ CORRECT
@Override
public void render(GuiGraphics g, int mx, int my, float d) {
    drawStuff(g);
    super.render(g, mx, my, d);  // MUST call!
}
```

### 3. mouseClicked @Override

```java
// ❌ WRONG - signature changed, don't use @Override
@Override
public boolean mouseClicked(double mx, double my, int btn) { }

// ✅ CORRECT - no annotation
public boolean mouseClicked(double mx, double my, int btn) { }
```

### 4. Accessing width/height in Constructor

```java
// ❌ WRONG - width and height are 0 in constructor
public MyScreen() {
    buttonX = width / 2;  // Always 0!
}

// ✅ CORRECT - use init()
protected void init() {
    buttonX = width / 2;  // Now correct
}
```

### 5. Color Format

```java
// Colors are ARGB (Alpha-Red-Green-Blue)
0xAARRGGBB

// Examples:
0xFFFFFFFF  // Opaque white
0x80000000  // 50% transparent black
0xE6001428  // 90% opaque midnight blue
0xFFFFD700  // Opaque gold
```

---

## Summary

| Feature | Solution |
|---------|----------|
| Base rendering | `GuiGraphics.fill()`, `fillGradient()`, `drawString()` |
| Item rendering | `GuiGraphics.renderItem()` |
| Click handling | Invisible `Button` widgets or manual `mouseClicked()` |
| Shift detection | `GLFW.glfwGetKey()` |
| Widget rendering | Always call `super.render()` |
| Screen sizing | Calculate in `init()`, not constructor |

This approach gives you complete creative control over your UI while staying compatible with vanilla Minecraft and Fabric's APIs.
