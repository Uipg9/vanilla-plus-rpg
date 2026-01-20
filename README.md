# Vanilla+ RPG & Shop

A clean, code-only Minecraft Fabric mod for **1.21.11** that adds an RPG progression system and economy without cluttering the vanilla experience.

![Version](https://img.shields.io/badge/Minecraft-1.21.11-green)
![Fabric](https://img.shields.io/badge/Fabric-0.18.4+-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## Features

### 🎨 Custom GUI System
- **No external GUI libraries** - Pure DrawContext API rendering
- Sleek, modern "Dark Mode" aesthetic with gold accents
- Gradient backgrounds, procedural borders, no texture files
- **Press H** to open the Hub dashboard
- Fully clickable buttons (uses 1.21.11 Button widget system)

### 💰 Economy System
- Server-authoritative money management
- Buy and sell items via commands or GUI
- Dynamic market with rotating "hot" and "cheap" items
- Shipping Bin for quick selling directly from hotbar

### ⚔️ RPG Progression
- **6 Skills**: Farming, Combat, Defense, Smithing, Woodcutting, Mining
- Max level 10 per skill (+5% bonus per level)
- Gain XP by mining ores, breaking blocks, killing mobs
- **NEW**: Earn Smithing XP & money from smelting in furnaces!
- **NEW**: Earn rewards from trees, crops, dirt, sand, gravel
- **NEW**: Get rewards for traveling/running
- Level up system with money rewards
- Action bar HUD showing level, money, and XP progress

### 🛒 Shop System
- Catalog-style shop (not inventory slots!)
- **100+ items** organized into categories:
  - All, Tools, Weapons, Armor, Food, Building, Farming, Ores, Misc
- Items sorted logically (pickaxes together, axes together, etc.)
- Hidden "Black Market" for rare items (hold Shift!)
- Market rotates daily with bonus prices

### 🔥 Smithing System
- Smelting items in furnaces gives Smithing XP and money!
- Higher Smithing level = more rewards (+5% per level)
- Works with all furnace types (furnace, blast furnace, smoker)
- Rewards for smelting: Iron, Gold, Copper, Netherite, Glass, Bricks, etc.

## Screenshots

*Hub Screen with navigation buttons*
- The Shop - Browse and buy items
- Shipping Bin - Sell items from your hotbar  
- My Skills - View your level, XP progress, and balance

## Installation

1. Install [Fabric Loader 0.18.4+](https://fabricmc.net/use/)
2. Install [Fabric API 0.141.1+](https://modrinth.com/mod/fabric-api)
3. Drop `vanillaplus-rpg-1.0.0.jar` into your `mods` folder
4. Launch Minecraft 1.21.11

## Usage

### Keybinds
| Key | Action |
|-----|--------|
| **H** | Open Hub Dashboard |
| **ESC** | Close any menu |

### Commands
| Command | Description |
|---------|-------------|
| `/buy <item> [amount]` | Purchase items from shop |
| `/sell` | Sell item in hand |
| `/sell all` | Sell all sellable items in inventory |
| `/balance` or `/bal` | Check your money |
| `/stats` | View RPG stats (level, XP, money) |
| `/market` | See today hot and cheap items |
| `/daily` | View daily earnings report |

### Admin Commands (OP only)
| Command | Description |
|---------|-------------|
| `/rpgadmin setmoney <amount>` | Set player money |
| `/rpgadmin setlevel <level>` | Set RPG level |
| `/rpgadmin addxp <amount>` | Add XP |
| `/rpgadmin addmoney <amount>` | Add/remove money |
| `/rpgadmin rotatemarket` | Force market rotation |

## XP System

### Mining & Breaking XP
| Activity | XP | Money |
|----------|-----|-------|
| Coal Ore | 5 | $2 |
| Iron Ore | 8-10 | $5-6 |
| Gold Ore | 12-15 | $10-12 |
| Diamond Ore | 25-30 | $50-60 |
| Emerald Ore | 20-25 | $40-50 |
| Ancient Debris | 50 | $100 |
| Wood Logs | 2 | $1 |
| Dirt/Sand/Gravel | 1 | - |
| Mature Crops | 6 (2x bonus) | $4 |
| Sugar Cane | 2 | $1 |

### Combat XP & Money
| Mob Type | XP Formula | Money |
|----------|------------|-------|
| Monsters | (Max Health / 2) × 1.5 | $15+ |
| Animals | (Max Health / 2) × 0.75 | $5+ |
| Range | 3-200 XP per kill | Scales with health |

### Movement Rewards
- Travel 100 blocks = 2 XP + $1
- Sprint 100 blocks = 5 XP + $3
- (Max once per 30 seconds)

### Smelting Rewards (Smithing Skill)
| Smelted Item | XP | Money |
|--------------|-----|-------|
| Iron Ingot | 3 | $2 |
| Gold Ingot | 5 | $5 |
| Copper Ingot | 2 | $1 |
| Netherite Scrap | 15 | $50 |
| Glass | 1 | $1 |
| Smooth Stone | 1 | $1 |
| Charcoal | 1 | $1 |
| Brick | 1 | $1 |

*Higher Smithing level = +5% bonus per level!*

### Level Formula
- XP needed = Level * 100
- Level 1: 100 XP, Level 10: 1000 XP, etc.
- Level up rewards: Level * $50

## Pricing Examples

| Item | Buy Price | Sell Price |
|------|-----------|------------|
| Cobblestone | $5 | $1 |
| Iron Ingot | $25 | $20 |
| Gold Ingot | $100 | $80 |
| Diamond | $500 | $400 |
| Emerald | $300 | $240 |
| Netherite Ingot | $5,000 | $4,000 |
| Elytra | $50,000 | $40,000 |

*Sell price = 80% of buy price*

## Technical Details

- **Minecraft:** 1.21.11
- **Fabric Loader:** 0.18.4+
- **Fabric API:** 0.141.1+
- **Java:** 21+
- **Mappings:** Official Mojang

### Architecture
- Server-side: Economy, data persistence, commands
- Client-side: GUI screens, HUD, keybinds
- Networking: Custom packet sync for player data
- Storage: NBT files per player in world folder

## Building

```bash
./gradlew build
```

The built JAR will be in `build/libs/`.

## License

MIT License - See LICENSE file for details.
