# Testing Checklist for Pocket Settlement

Use this checklist to thoroughly test the mod's features.

## 🎮 Initial Setup

- [ ] Mod loads without errors
- [ ] Press **G** to open Governor's Desk
- [ ] All GUI tabs are visible (Buildings, Citizens, Contracts, Tech, Stockpile)
- [ ] GUI renders without graphical glitches
- [ ] Sounds play when opening GUI

---

## 🏗️ Building System

### Grid Interaction
- [ ] Click empty plots to open build menu
- [ ] All 11 building types appear in build menu
- [ ] Building icons display correctly
- [ ] Building costs show properly
- [ ] "Not enough coins" message appears when broke
- [ ] Can cancel out of build menu

### Placing Buildings
- [ ] Can place House (starter building)
- [ ] Can place Greenhouse (requires tech/coins)
- [ ] Can place Quarry
- [ ] Can place Lumber Yard
- [ ] Can place Mob Barn
- [ ] Can place Market
- [ ] Can place Bank
- [ ] Can place Academy
- [ ] Can place Guard Tower
- [ ] Can place Town Hall

### Managing Buildings
- [ ] Click existing building to open management screen
- [ ] Can upgrade building levels (1→2, 2→3, etc.)
- [ ] Upgrade costs increase properly
- [ ] Production speed improves with levels
- [ ] Can assign workers to buildings
- [ ] Can remove workers from buildings
- [ ] Can demolish buildings (get partial refund?)

### Adjacency Bonuses
- [ ] Place Greenhouse next to House - check for bonus
- [ ] Place Quarry next to Academy - check for bonus
- [ ] Place Guard Tower next to Town Hall - check for bonus
- [ ] Bonus indicators show in building info

---

## 👥 Citizen System

### Hiring Citizens
- [ ] Open Citizens tab
- [ ] "Hire Citizen" button works
- [ ] Costs coins to hire
- [ ] Population cap limits hiring
- [ ] New citizens appear in list
- [ ] Citizens have random names
- [ ] Citizens start at level 1

### Citizen Information
- [ ] Each citizen shows:
  - [ ] Name
  - [ ] Job
  - [ ] Level
  - [ ] XP bar
  - [ ] Happiness %
- [ ] Can view citizen details

### Job Assignment
- [ ] Can assign Farmer to Greenhouse
- [ ] Can assign Miner to Quarry
- [ ] Can assign Lumberjack to Lumber Yard
- [ ] Can assign Rancher to Mob Barn
- [ ] Can assign Merchant to Market
- [ ] Can assign Scholar to Academy
- [ ] Can assign Guard to Guard Tower
- [ ] Cannot assign wrong job to building
- [ ] Unassigned citizens stay idle

### Leveling Up
- [ ] Citizens gain XP over time when working
- [ ] XP bar fills visually
- [ ] Citizens level up (1→2→3→4→5)
- [ ] Higher levels improve production
- [ ] Level-up sound/notification appears

---

## 📜 Contract System

### Contract Generation
- [ ] 3 contracts appear daily
- [ ] Contracts request different items (wheat, stone, wood, etc.)
- [ ] Each contract shows:
  - [ ] Required item and amount
  - [ ] Current progress
  - [ ] Coin reward
  - [ ] Completion status

### Fulfilling Contracts
- [ ] Can deliver items from stockpile
- [ ] Progress bar updates when delivering
- [ ] Partial deliveries work
- [ ] Contract completes when fully delivered
- [ ] Coins awarded on completion
- [ ] Completion sound/animation plays
- [ ] "Not enough in stockpile" message works

### Contract Refresh
- [ ] New contracts appear next day
- [ ] Old contracts disappear
- [ ] Incomplete contracts expire

---

## 🔬 Technology Tree

### Tech Display
- [ ] Tech tree shows all branches:
  - [ ] Industry (red)
  - [ ] Civics (blue)
  - [ ] Logistics (green)
- [ ] Locked techs show grayed out
- [ ] Unlocked techs show highlighted
- [ ] Tech requirements display correctly

### Researching Tech
- [ ] Can unlock FARMING_I with influence
- [ ] Can unlock MINING_I with influence
- [ ] Can unlock FORESTRY_I with influence
- [ ] Can unlock RANCHING_I with influence
- [ ] Can unlock COMMERCE_I with influence
- [ ] Prerequisites are enforced (can't skip tiers)
- [ ] Influence cost shown correctly
- [ ] "Not enough influence" message appears

### Tech Effects
- [ ] Unlocking tech enables new buildings
- [ ] Unlocking tech improves production
- [ ] Unlocking tech unlocks new jobs
- [ ] Tech descriptions are accurate

---

## 📦 Stockpile System

### Viewing Resources
- [ ] All stored items appear in list
- [ ] Item counts display correctly
- [ ] Item icons render properly
- [ ] Empty stockpile shows message

### Depositing Items
- [ ] Can deposit items from inventory
- [ ] Multiple items of same type stack
- [ ] Capacity limits work
- [ ] "Stockpile full" message appears

### Withdrawing Items
- [ ] Left-click withdraws 1 stack
- [ ] Shift-click withdraws all
- [ ] Items go to player inventory
- [ ] Withdrawal sound plays
- [ ] Cannot withdraw more than available

### Production Integration
- [ ] Greenhouse produces crops → stockpile
- [ ] Quarry produces ores → stockpile
- [ ] Lumber Yard produces wood → stockpile
- [ ] Mob Barn produces animal items → stockpile
- [ ] Production continues while offline

---

## ⏰ Time & Progression

### Day/Night Cycle
- [ ] Settlement tracks in-game time
- [ ] Day counter increases
- [ ] Contracts refresh at day start

### Passive Income
- [ ] Bank generates coins over time
- [ ] Market generates coins with workers
- [ ] Coin generation rate is reasonable

### Influence Gain
- [ ] Influence accumulates over time
- [ ] Scholars boost influence gain
- [ ] Influence gain rate is reasonable

---

## 🎨 UI/UX

### Navigation
- [ ] All tabs are clickable
- [ ] Tab switching is smooth
- [ ] Back buttons work
- [ ] Close buttons work
- [ ] ESC key closes GUI

### Visual Feedback
- [ ] Hover effects on buttons
- [ ] Click sounds on buttons
- [ ] Color coding makes sense (green=good, red=bad)
- [ ] Progress bars animate
- [ ] Icons are clear and readable

### Information Display
- [ ] Coin count always visible
- [ ] Influence count always visible
- [ ] Population count shows X/Max
- [ ] Tooltips explain features
- [ ] Error messages are helpful

---

## 🎮 Commands

Test all commands as an operator:

### Info Commands
- [ ] `/settlement info` - Shows overview
- [ ] `/settlement citizens` - Lists citizens
- [ ] `/settlement buildings` - Shows ASCII grid
- [ ] `/settlement tech` - Shows tech progress
- [ ] `/settlement stockpile` - Lists resources

### Debug Commands (OP only)
- [ ] `/settlement give coins <amount>` - Adds coins
- [ ] `/settlement give influence <amount>` - Adds influence
- [ ] `/settlement reset` - Clears settlement
- [ ] `/settlement unlock <tech>` - Unlocks technology

### Permission Checks
- [ ] Non-OP cannot use give/reset/unlock
- [ ] OP can use all commands
- [ ] Error messages appear for invalid commands

---

## 🐛 Edge Cases & Bugs

### Resource Management
- [ ] Cannot place building without coins
- [ ] Cannot hire beyond population cap
- [ ] Cannot research without influence
- [ ] Stockpile capacity enforced
- [ ] Negative numbers don't appear

### Data Persistence
- [ ] Settlement saves on logout
- [ ] Settlement loads on login
- [ ] Buildings persist
- [ ] Citizens persist
- [ ] Tech progress persists
- [ ] Stockpile contents persist

### Multiplayer (if testing)
- [ ] Each player has own settlement
- [ ] Settlements don't interfere
- [ ] Data doesn't corrupt

### Performance
- [ ] No lag when opening GUI
- [ ] No lag with many buildings
- [ ] No lag with many citizens
- [ ] No memory leaks over time

---

## 🏆 Gameplay Balance

### Progression Pacing
- [ ] Starting resources feel fair
- [ ] Early game not too grindy
- [ ] Mid game has goals
- [ ] Late game is achievable
- [ ] Tech tree progression logical

### Economy Balance
- [ ] Building costs reasonable
- [ ] Income generation reasonable
- [ ] Contract rewards fair
- [ ] Tech costs balanced
- [ ] Citizen costs balanced

### Production Rates
- [ ] Greenhouse production useful
- [ ] Quarry production useful
- [ ] Lumber Yard production useful
- [ ] Mob Barn production useful
- [ ] Production scales with levels

---

## 📝 Known Issues to Watch For

Based on the API fixes, watch for:
- [ ] NBT save/load errors in logs
- [ ] Item name display issues
- [ ] Sound playback failures
- [ ] Command permission errors
- [ ] KeyBinding not registering

---

## ✅ Final Checks

- [ ] No errors in latest.log
- [ ] No crashes during gameplay
- [ ] All features accessible via GUI
- [ ] Tutorial/help info is clear
- [ ] Mod feels complete and polished

---

**Testing Notes:**
- Test in both Creative and Survival modes
- Test in both Singleplayer and Multiplayer
- Test with other mods installed (compatibility)
- Test performance on lower-end PCs

**Report Issues At:**
https://github.com/Uipg9/PocketSettlement/issues
