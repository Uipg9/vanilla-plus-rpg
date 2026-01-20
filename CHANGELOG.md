# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0] - 2026-01-20

### Fixed
- **Shop Screen**: Balance text now positioned below title (no longer overlaps)
- **Shop Screen**: Converted from pagination to smooth vertical scrolling
  - Increased visible rows from 4 to 6
  - Added mouse wheel support for scrolling
  - Added scroll up/down buttons with indicators
  - Shows item range (e.g., "1-36 of 120")
- **Skills Screen**: Down arrow button moved lower (no longer covered)
- **Smelting Rewards**: Now properly grant vanilla Minecraft XP (used for enchanting)
  - Rewards shown as "🔥 +3 Smithing | +$2" with purple fire emoji
  - Smithing skill level still provides +5% bonus to both XP and money

### Changed
- Shop window increased to 450x420 (from 420x360) to accommodate scrolling
- Shop grid increased to 6 visible rows (from 4)
- Improved scrolling UX with clear indicators

## [1.0.0] - 2026-01-19

### Added
- **Custom GUI System** - Pure DrawContext API rendering without external libraries
  - Hub Dashboard with navigation buttons
  - Shop Screen with item catalog grid and pagination
  - Shipping Bin Screen with crate-style visuals
  - Skills Screen with progress bars
  - Dark mode aesthetic with gold accents
  
- **Economy System**
  - Server-authoritative money management
  - 100+ items with balanced buy/sell prices
  - Dynamic market with rotating "hot" (2x sell) and "cheap" (50% buy) items
  - Daily market rotation at dawn
  
- **RPG Progression**
  - XP system from mining ores (5-50 XP) and combat (5-200 XP)
  - Level up system with formula: XP needed = Level × 100
  - Level up rewards: Level × $50 bonus
  - Action bar HUD showing level, money, XP progress
  
- **Commands**
  - `/buy <item> [amount]` - Purchase items
  - `/sell [item] [amount]` - Sell items
  - `/sell all` - Sell entire inventory
  - `/balance` / `/bal` - Check money
  - `/stats` - View RPG stats
  - `/market` - See today's deals
  - `/daily` - View daily earnings
  - `/rpgadmin` - Admin commands for testing
  
- **Keybinds**
  - Press **H** to open Hub menu
  - Using GLFW direct input (1.21.11 compatible)
  
- **Networking**
  - Server→Client data sync for HUD updates
  - Custom packet payload using Fabric Networking API

### Technical
- Minecraft 1.21.11 compatible
- Fabric Loader 0.18.4+
- Fabric API 0.141.1+
- Official Mojang mappings
- No external GUI libraries (no SGUI, no GooeyLibs)

### Known Limitations
- Shipping Bin is visual-only (sends sell command, doesn't transfer items)
- Skills percentages are placeholders
- Black Market fully functional via commands only
