# Custom GUI System for Minecraft 1.21.11

## Overview

This document details the **custom procedural GUI system** we built for the Vanilla+ RPG mod. Instead of using vanilla textures or external image files, we render everything programmatically using `DrawContext` primitives.

**Key Achievement**: A beautiful, gold-bordered dark theme GUI that looks professional and fits the RPG aesthetic, built entirely with code.

## Why Procedural GUI?

### Advantages Over Texture-Based GUI
1. **No image assets needed** - No PNG files to create/maintain
2. **Perfect scaling** - Adapts to any resolution
3. **Easy theming** - Change colors in code, instant updates
4. **Smaller mod size** - No texture bloat
5. **Dynamic content** - Easy to add/modify elements at runtime

### The 1.21.11 Challenge
In Minecraft 1.21.11, the mouse input API changed significantly:
- `mouseClicked(double, double, int)` is **no longer called** with the old signature
- The new system uses `MouseButtonEvent` which is harder to override

**Our Solution**: Use vanilla `Button` widgets for click detection, then draw custom visuals ON TOP.

## Architecture

### Base Class: `BaseRpgScreen`
```java
public abstract class BaseRpgScreen extends Screen {
    // Color constants for consistent theming
    protected static final int COLOR_WINDOW_BG = 0xE6102030;     // Dark blue, 90% opaque
    protected static final int COLOR_GOLD_BORDER = 0xFFD4AF37;   // Gold
    protected static final int COLOR_BUTTON_BG = 0xCC1A1A2E;     // Dark purple
    protected static final int COLOR_BUTTON_HOVER = 0xCC2A2A4E;  // Lighter purple
    protected static final int COLOR_GOLD_TEXT = 0xFFFFD700;     // Gold text
    protected static final int COLOR_WHITE_TEXT = 0xFFFFFFFF;    // White
    protected static final int COLOR_GRAY_TEXT = 0xFFAAAAAA;     // Gray
    
    // Window positioning
    protected int windowX, windowY, windowWidth, windowHeight;
}
```

### Drawing Primitives

#### Window Background with Gradient
```java
protected void drawWindowBackground(GuiGraphics graphics, int x, int y, int w, int h) {
    // Gradient from top to bottom
    drawGradient(graphics, x, y, w, h, 0xE6102030, 0xE6081018);
    // Gold border
    drawBorder(graphics, x, y, w, h, COLOR_GOLD_BORDER);
}

private void drawGradient(GuiGraphics graphics, int x, int y, int w, int h, int colorTop, int colorBottom) {
    int steps = h;
    for (int i = 0; i < steps; i++) {
        float ratio = (float) i / (float) steps;
        int r = (int) (((colorTop >> 16) & 0xFF) * (1 - ratio) + ((colorBottom >> 16) & 0xFF) * ratio);
        int g = (int) (((colorTop >> 8) & 0xFF) * (1 - ratio) + ((colorBottom >> 8) & 0xFF) * ratio);
        int b = (int) ((colorTop & 0xFF) * (1 - ratio) + (colorBottom & 0xFF) * ratio);
        int a = (int) (((colorTop >> 24) & 0xFF) * (1 - ratio) + ((colorBottom >> 24) & 0xFF) * ratio);
        int color = (a << 24) | (r << 16) | (g << 8) | b;
        graphics.fill(x, y + i, x + w, y + i + 1, color);
    }
}
```

#### Border Drawing
```java
protected void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
    graphics.fill(x, y, x + w, y + 1, color);           // Top
    graphics.fill(x, y + h - 1, x + w, y + h, color);   // Bottom
    graphics.fill(x, y, x + 1, y + h, color);           // Left
    graphics.fill(x + w - 1, y, x + w, y + h, color);   // Right
}
```

## The Button Widget Trick

### The Problem
Custom `mouseClicked()` methods are ignored in 1.21.11. Manual coordinate checking doesn't work.

### The Solution
1. Add invisible vanilla `Button` widgets for click areas
2. Render custom styled buttons ON TOP in `renderContent()`
3. Vanilla handles click detection, we handle visuals

### Implementation Pattern
```java
@Override
protected void init() {
    super.init();
    
    // Add invisible button widget (handles clicks)
    addRenderableWidget(Button.builder(Component.empty(), btn -> {
        playClickSound();
        Minecraft.getInstance().setScreen(new TargetScreen());
    }).bounds(x, y, width, height).build());
}

@Override
public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    // Background
    graphics.fill(0, 0, width, height, 0xA0000000);
    
    // Window
    drawWindowBackground(graphics, windowX, windowY, windowWidth, windowHeight);
    
    // IMPORTANT: Render vanilla widgets FIRST (they draw gray)
    super.render(graphics, mouseX, mouseY, delta);
    
    // Now render our custom visuals ON TOP (covers the gray)
    renderContent(graphics, mouseX, mouseY, delta);
}

protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    // Draw styled button that covers the vanilla gray one
    boolean hovered = isMouseOver(mouseX, mouseY, x, y, width, height);
    drawStyledButton(graphics, x, y, width, height, "Label", hovered);
}
```

### Critical Render Order
```
1. Background (dim overlay)
2. Window (gradient + border)
3. super.render() → Vanilla Button widgets render GRAY
4. renderContent() → Our styled buttons render ON TOP
```

## Styled Button Drawing

```java
private void drawStyledButton(GuiGraphics graphics, int x, int y, int w, int h, 
                              String label, boolean hovered) {
    // Background - changes on hover
    int bgColor = hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON_BG;
    graphics.fill(x, y, x + w, y + h, bgColor);
    
    // Gold border
    drawBorder(graphics, x, y, w, h, COLOR_GOLD_BORDER);
    
    // Centered text
    int textWidth = font.width(label);
    int textX = x + (w - textWidth) / 2;
    int textY = y + (h - 8) / 2;
    graphics.drawString(font, label, textX, textY, 
        hovered ? COLOR_GOLD_TEXT : COLOR_WHITE_TEXT, true);
}
```

## Hover Detection

```java
protected boolean isMouseOver(int mouseX, int mouseY, int x, int y, int w, int h) {
    return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
}
```

## Item Rendering

```java
// Render item icon at position
ItemStack stack = new ItemStack(item);
graphics.renderItem(stack, slotX + 4, slotY + 4);

// Render item count
if (stack.getCount() > 1) {
    graphics.drawString(font, String.valueOf(stack.getCount()), 
        slotX + SLOT_SIZE - 8, slotY + SLOT_SIZE - 10, 
        COLOR_WHITE_TEXT, true);
}
```

## Tooltip Rendering

```java
private void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY, List<String> lines) {
    // Calculate dimensions
    int tooltipWidth = 0;
    for (String line : lines) {
        tooltipWidth = Math.max(tooltipWidth, font.width(line));
    }
    tooltipWidth += 8;
    int tooltipHeight = lines.size() * 10 + 6;
    
    // Position (keep on screen)
    int tooltipX = mouseX + 12;
    int tooltipY = mouseY - 12;
    if (tooltipX + tooltipWidth > width) tooltipX = mouseX - tooltipWidth - 4;
    if (tooltipY < 0) tooltipY = 0;
    
    // Background
    graphics.fill(tooltipX - 2, tooltipY - 2, 
        tooltipX + tooltipWidth + 2, tooltipY + tooltipHeight + 2, 
        0xF0100010);
    
    // Border
    drawBorder(graphics, tooltipX - 2, tooltipY - 2, 
        tooltipWidth + 4, tooltipHeight + 4, 0xFF5000AA);
    
    // Text
    int y = tooltipY;
    for (String line : lines) {
        graphics.drawString(font, line, tooltipX, y, 0xFFFFFFFF, true);
        y += 10;
    }
}
```

## Sound Effects

```java
protected void playClickSound() {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player != null) {
        mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
    }
}
```

## Complete Screen Example

```java
public class ExampleScreen extends BaseRpgScreen {
    
    public ExampleScreen() {
        super(Component.literal("§6✦ Example ✦"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Calculate window position
        windowWidth = Math.min(200, width - 20);
        windowHeight = Math.min(150, height - 20);
        windowX = (width - windowWidth) / 2;
        windowY = (height - windowHeight) / 2;
        
        // Add button widget (invisible, handles clicks)
        addRenderableWidget(Button.builder(Component.empty(), btn -> {
            playClickSound();
            onClose();
        }).bounds(windowX + 8, windowY + 8, 50, 16).build());
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draw button (covers vanilla gray)
        boolean hovered = isMouseOver(mouseX, mouseY, windowX + 8, windowY + 8, 50, 16);
        drawStyledButton(graphics, windowX + 8, windowY + 8, 50, 16, "< Back", hovered);
        
        // Draw content
        drawCenteredText(graphics, "Hello World!", 
            windowX + windowWidth / 2, windowY + 50, COLOR_GOLD_TEXT);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xA0000000);  // Dim background
        drawWindowBackground(graphics, windowX, windowY, windowWidth, windowHeight);
        drawCenteredTitle(graphics, "✦ Example ✦", windowX + windowWidth / 2, windowY + 10);
        super.render(graphics, mouseX, mouseY, delta);   // Vanilla widgets
        renderContent(graphics, mouseX, mouseY, delta);  // Custom visuals ON TOP
    }
}
```

## Color Reference

| Purpose | Hex Code | Description |
|---------|----------|-------------|
| Window Background | `0xE6102030` | Dark blue, 90% opaque |
| Gold Border | `0xFFD4AF37` | Metallic gold |
| Button Normal | `0xCC1A1A2E` | Dark purple, 80% opaque |
| Button Hover | `0xCC2A2A4E` | Lighter purple |
| Gold Text | `0xFFFFD700` | Bright gold |
| White Text | `0xFFFFFFFF` | Pure white |
| Gray Text | `0xFFAAAAAA` | Muted gray |
| Green (Success) | `0xFF44AA44` | For positive actions |
| Red (Danger) | `0xFFAA4444` | For dangerous actions |

## Best Practices

1. **Always use constants** - Define colors at class level
2. **Calculate positions dynamically** - Scale with screen size
3. **Layer rendering correctly** - super.render() THEN custom content
4. **Test hover states** - Make interactions feel responsive
5. **Add sound feedback** - Click sounds make UI feel alive
6. **Keep tooltips informative** - Show useful information on hover

## Files in This System

- `BaseRpgScreen.java` - Abstract base with all drawing utilities
- `HubScreen.java` - Main navigation hub
- `ShopScreen.java` - Item catalog with grid layout
- `SkillsScreen.java` - Progress bars and stats
- `ShippingBinScreen.java` - Inventory sell interface

## Troubleshooting

### Buttons Not Clicking
- Verify Button widget bounds match visual bounds
- Check render order (super.render before renderContent)

### Colors Look Wrong
- ARGB format: `0xAARRGGBB`
- Alpha: `0xFF` = fully opaque, `0x00` = invisible

### Text Not Centered
```java
int textWidth = font.width(text);
int textX = centerX - textWidth / 2;
```

### Items Not Rendering
- Ensure you have `graphics.renderItem(stack, x, y)`
- Check stack is not `ItemStack.EMPTY`
