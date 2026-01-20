# Smelting Rewards System (Mixins)

## Overview
The mod uses a **Mixin** to hook into Minecraft's furnace system and grant Smithing skill XP and money when players take smelted items from furnaces.

## Implementation

### FurnaceResultSlotMixin
**Location:** `src/main/java/com/vanillaplus/rpg/mixin/FurnaceResultSlotMixin.java`

Injects into the `FurnaceResultSlot.onTake()` method to detect when players take items from furnace output.

```java
@Mixin(FurnaceResultSlot.class)
public abstract class FurnaceResultSlotMixin {
    
    @Inject(method = "onTake", at = @At("HEAD"))
    private void vanillaplusrpg$onTakeResult(Player player, ItemStack stack, CallbackInfo ci) {
        // Check if item gives rewards
        // Calculate bonuses based on Smithing level
        // Grant money and send notification
    }
}
```

### Key Features

1. **Item-Based Rewards**
   - Maps items to XP and money values
   - Different rewards for different smelted items
   
2. **Smithing Level Bonus**
   - +5% per Smithing skill level
   - Applies to both XP and money
   
3. **Batch Processing**
   - Multiplies rewards by item count
   - Works with shift-clicking stacks

### Reward Tables

#### High-Value Smelting
```java
vanillaplusrpg$smeltingXp.put(Items.NETHERITE_SCRAP, 15);
vanillaplusrpg$smeltingMoney.put(Items.NETHERITE_SCRAP, 50L);

vanillaplusrpg$smeltingXp.put(Items.GOLD_INGOT, 5);
vanillaplusrpg$smeltingMoney.put(Items.GOLD_INGOT, 5L);

vanillaplusrpg$smeltingXp.put(Items.IRON_INGOT, 3);
vanillaplusrpg$smeltingMoney.put(Items.IRON_INGOT, 2L);
```

#### Building Materials
```java
vanillaplusrpg$smeltingXp.put(Items.GLASS, 1);
vanillaplusrpg$smeltingMoney.put(Items.GLASS, 1L);

vanillaplusrpg$smeltingXp.put(Items.BRICK, 1);
vanillaplusrpg$smeltingMoney.put(Items.BRICK, 1L);

vanillaplusrpg$smeltingXp.put(Items.SMOOTH_STONE, 1);
vanillaplusrpg$smeltingMoney.put(Items.SMOOTH_STONE, 1L);
```

## Configuration

### Mixin Registration
**File:** `src/main/resources/vanillaplusrpg.mixins.json`

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.vanillaplus.rpg.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [
    "FurnaceResultSlotMixin"
  ],
  "client": [],
  "injectors": {
    "defaultRequire": 1
  }
}
```

## Notification System

### Reward Type
The system uses reward type `4` for smelting notifications:

```java
PlayerDataSyncHandler.RewardNotificationPayload payload = 
    new PlayerDataSyncHandler.RewardNotificationPayload(finalXp, finalMoney, 0, 4);
ServerPlayNetworking.send(serverPlayer, payload);
```

### Client-Side Display
**File:** `RewardOverlay.java`

```java
public static void showSmeltingReward(int xp, long money) {
    StringBuilder msg = new StringBuilder("§d🔥 ");
    
    if (xp > 0) {
        msg.append("§d+").append(xp).append(" Smithing");
    }
    if (money > 0) {
        if (xp > 0) msg.append("  ");
        msg.append("§a+$").append(formatMoney(money));
    }
    
    addNotification(msg.toString(), 0xFFAA6644);
}
```

## How It Works

1. **Player takes item from furnace output slot**
2. **Mixin intercepts the `onTake` method**
3. **Checks if item has smelting rewards**
4. **Calculates bonus from Smithing level**
   - Base XP × (1 + level × 0.05)
   - Base Money × (1 + level × 0.05)
5. **Grants money to player**
6. **Sends notification packet to client**
7. **Client displays reward overlay**

## Compatibility

- Works with all furnace types:
  - Regular Furnace
  - Blast Furnace  
  - Smoker
- Compatible with auto-smelting from hoppers
- Works with shift-click mass extraction

## Adding New Rewards

To add a new smelted item reward:

1. Add to XP map (if applicable):
```java
vanillaplusrpg$smeltingXp.put(Items.YOUR_ITEM, xpAmount);
```

2. Add to money map (if applicable):
```java
vanillaplusrpg$smeltingMoney.put(Items.YOUR_ITEM, moneyAmount);
```

3. Rebuild the mod

## Testing

1. Place items in furnace
2. Wait for smelting to complete
3. Take smelted items from output slot
4. Verify notification appears
5. Check Smithing skill increased
6. Check money balance increased

## Troubleshooting

### Rewards not appearing
- Verify mixin is loaded (check logs for "Applied mixin")
- Check item is in reward maps
- Ensure player is ServerPlayer (not client-side)

### Wrong reward amounts
- Check Smithing level calculation
- Verify base values in static maps
- Test with different item counts

### Notification not showing
- Check network packet sending
- Verify RewardOverlay registered
- Test reward type 4 handler in client
