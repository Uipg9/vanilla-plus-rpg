# Testing Checklist - Vanilla+ RPG & Shop

Before releasing, test each item in order. Check the box when verified working.

## 1. Basic Launch
- [ ] Game launches without crash
- [ ] No errors in console on startup
- [ ] "vanillaplusrpg initialized successfully!" appears in log
- [ ] "vanillaplusrpg client initialized successfully!" appears in log

## 2. World Entry
- [ ] Can create and enter a new world
- [ ] "vanillaplusrpg server started!" appears in log
- [ ] "Market rotated - Hot: X, Cheap: Y" appears in log
- [ ] Player receives starting money ($100)

## 3. Keybinds
- [ ] Press **H** key - Hub screen opens
- [ ] Press **ESC** - Hub screen closes
- [ ] H key doesn't open menu when chatting/in inventory

## 4. Hub Screen
- [ ] Window appears centered with gold border
- [ ] Title "✦ RPG Hub ✦" displays
- [ ] "[ The Shop ]" button visible
- [ ] "[ Shipping Bin ]" button visible
- [ ] "[ My Skills ]" button visible
- [ ] Buttons highlight on hover
- [ ] Clicking Shop button opens Shop screen
- [ ] Clicking Shipping Bin button opens Shipping Bin screen
- [ ] Clicking Skills button opens Skills screen
- [ ] Hold Shift - "Black Market available in Shop..." hint appears

## 5. Shop Screen
- [ ] Shop screen opens with gold border
- [ ] Item grid displays (6 columns x 4 rows)
- [ ] Items render correctly in slots
- [ ] Hovering item shows tooltip with name and price
- [ ] Page navigation works (if > 24 items)
- [ ] "< Back" button returns to Hub
- [ ] Hold Shift - "⚠ Illegal" Black Market button appears
- [ ] Click Black Market button - opens red-themed market

## 6. Shipping Bin Screen
- [ ] Shipping Bin screen opens
- [ ] 3x3 slot grid with wooden crate appearance
- [ ] "SELL ALL" button visible (green)
- [ ] "Total Value: $0" shown initially
- [ ] "< Back" button returns to Hub

## 7. Skills Screen
- [ ] Skills screen opens
- [ ] Progress bars for Mining, Combat, Farming, Trading visible
- [ ] Percentage numbers display
- [ ] "< Back" button returns to Hub

## 8. Commands - Basic
- [ ] `/balance` - Shows "You have $100" (starting money)
- [ ] `/bal` - Same as /balance (alias)
- [ ] `/stats` - Shows level 1, XP 0/100, Money $100
- [ ] `/market` - Shows current hot/cheap items
- [ ] `/daily` - Shows "You haven't earned anything today"

## 9. Commands - Buy/Sell
- [ ] `/buy cobblestone 10` - Purchases 10 cobblestone, money decreases
- [ ] `/sell` (holding item) - Sells item in hand, money increases
- [ ] `/sell all` - Sells all sellable items in inventory

## 10. XP System
- [ ] Mine coal ore - "+5 XP" appears in action bar
- [ ] Mine diamond ore - "+25 XP" appears in action bar
- [ ] Kill a mob - "+X XP" appears in action bar
- [ ] After gaining enough XP - "LEVEL UP!" message appears
- [ ] Level up gives money reward

## 11. HUD (Action Bar)
- [ ] Action bar shows: `[ Lv1 ] $XXX | XP: [....] | Hot: ItemName`
- [ ] Updates when money changes
- [ ] Updates when XP gained
- [ ] Shows current hot item from market

## 12. Admin Commands (Single Player = Always Admin)
- [ ] `/rpgadmin setmoney 1000` - Sets money to $1000
- [ ] `/rpgadmin setlevel 5` - Sets level to 5
- [ ] `/rpgadmin addxp 50` - Adds 50 XP
- [ ] `/rpgadmin addmoney 500` - Adds $500
- [ ] `/rpgadmin rotatemarket` - Forces market rotation

## 13. Data Persistence
- [ ] Leave and rejoin world - money persists
- [ ] Leave and rejoin world - level persists
- [ ] Leave and rejoin world - XP persists

## 14. Edge Cases
- [ ] Can't buy if not enough money (error message shown)
- [ ] Can't sell items not in inventory (error message shown)
- [ ] Open Hub while inventory is open - Hub opens normally

## 15. Visual Quality
- [ ] Gradient backgrounds render smoothly (no banding)
- [ ] Gold borders are exactly 1 pixel
- [ ] Text is properly centered
- [ ] Hover effects are responsive (no lag)
- [ ] Colors match dark mode aesthetic

---

## Release Checklist

- [ ] All tests above pass
- [ ] `./gradlew build` succeeds
- [ ] JAR file exists in `build/libs/`
- [ ] README.md is complete
- [ ] LICENSE file exists
- [ ] .gitignore excludes build artifacts
- [ ] No sensitive data in repository
- [ ] MC_REF folder excluded from release

---

## Known Issues / TODO for Future

- Shop screen buy functionality sends command (needs server response)
- Shipping Bin doesn't actually transfer items (visual only for now)
- Skills percentages are placeholder values
- Black Market purchases not fully implemented

---

**Test Date:** _______________  
**Tester:** _______________  
**Version:** 1.0.0  
**Minecraft:** 1.21.11  
**Fabric Loader:** 0.18.4  
