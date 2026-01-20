# 🔧 Troubleshooting - Minecraft 1.21.11

**Solutions to common errors and problems**

---

## Table of Contents

1. [Compilation Errors](#compilation)
2. [Runtime Errors](#runtime)
3. [Mapping Issues](#mappings)
4. [SGUI Problems](#sgui)
5. [Command Issues](#commands)
6. [NBT/Data Problems](#nbt)
7. [Build Failures](#build)

---

## <a id="compilation"></a>Compilation Errors

### Error: "cannot find symbol: ServerPlayer"

**Problem:** Using wrong mappings

**Solution:**
```gradle
// In build.gradle, ensure you have:
dependencies {
    mappings loom.officialMojangMappings()  // ✅ CORRECT
    // mappings "net.fabricmc:yarn:..."     // ❌ WRONG FOR 1.21.11
}
```

Then clean rebuild:
```powershell
.\gradlew.bat clean build
```

---

### Error: "cannot find symbol: Component"

**Problem:** Missing import

**Solution:**
```java
import net.minecraft.network.chat.Component;  // ✅ ADD THIS
```

---

### Error: "package eu.pb4.sgui does not exist"

**Problem:** SGUI not added to dependencies

**Solution:**
```gradle
// In build.gradle
repositories {
    mavenCentral()
    maven { url 'https://maven.nucleoid.xyz' }  // ✅ ADD THIS
}

dependencies {
    include(modImplementation("eu.pb4:sgui:1.12.0+1.21.11"))  // ✅ ADD THIS
}
```

Then reload Gradle:
```powershell
.\gradlew.bat build
```

---

### Error: "incompatible types: Component cannot be converted to String"

**Problem:** Using Component where String is expected (or vice versa)

**Solution:**
```java
// ❌ WRONG
Component name = "Hello";

// ✅ CORRECT
Component name = Component.literal("Hello");

// Convert Component to String
String text = component.getString();
```

---

### Error: Ternary operator precedence

**Problem:** Java interprets ternary inside method call incorrectly

**Example Error:**
```java
Component msg = Component.literal(condition ? "Yes" : "No");  // ❌ ERROR
```

**Solution:** Parenthesize the ternary
```java
Component msg = Component.literal((condition ? "Yes" : "No"));  // ✅ FIXED
```

---

## <a id="runtime"></a>Runtime Errors

### Error: "java.lang.NoSuchMethodError"

**Problem:** Version mismatch between dependencies

**Solution:**
```properties
# In gradle.properties, verify ALL versions match:
minecraft_version=1.21.11
loader_version=0.18.4
fabric_version=0.141.1+1.21.11
```

Then:
```powershell
.\gradlew.bat clean build
```

---

### Error: "NullPointerException" when accessing player data

**Problem:** Player not loaded or data not initialized

**Solution:**
```java
// ❌ UNSAFE
int value = nbt.getInt("key");

// ✅ SAFE - Check first
int value = nbt.contains("key") ? nbt.getInt("key") : 0;

// ✅ SAFE - Initialize if missing
public static CompoundTag getModData(ServerPlayer player) {
    CompoundTag nbt = player.getPersistentData();
    if (!nbt.contains("yourmod")) {
        nbt.put("yourmod", new CompoundTag());
    }
    return nbt.getCompound("yourmod");
}
```

---

### Error: GUI doesn't open

**Problem:** Not calling `.open()` or player context wrong

**Solution:**
```java
// ❌ WRONG
MyGui gui = new MyGui(player);

// ✅ CORRECT
MyGui gui = new MyGui(player);
gui.open();  // Must explicitly open

// ✅ ALSO CORRECT (if open() is called in constructor)
new MyGui(player).open();
```

---

### Error: Command not found

**Problem:** Command not registered properly

**Solution:**
```java
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;

@Override
public void onInitialize() {
    CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
        dispatcher.register(Commands.literal("mycommand")  // ✅ Must use .register()
            .executes(context -> {
                // ...
                return 1;
            })
        );
    });
}
```

Test with `/mycommand` in-game. Use `/reload` if changed.

---

## <a id="mappings"></a>Mapping Issues

### Issue: "Class names don't match documentation"

**Problem:** Using Yarn mappings docs for Mojang mappings

**Solution:** Use this conversion table

| Yarn | Mojang | Import |
|------|--------|--------|
| `PlayerEntity` | `ServerPlayer` | `net.minecraft.server.level.ServerPlayer` |
| `ServerWorld` | `ServerLevel` | `net.minecraft.server.level.ServerLevel` |
| `Text` | `Component` | `net.minecraft.network.chat.Component` |
| `NbtCompound` | `CompoundTag` | `net.minecraft.nbt.CompoundTag` |
| `ServerCommandSource` | `CommandSourceStack` | `net.minecraft.commands.CommandSourceStack` |

---

### Issue: Can't find method in docs

**Problem:** Documentation is for different version

**Solution:** Use IDE autocomplete (Ctrl+Space in VS Code, IntelliJ) to see actual methods available

---

## <a id="sgui"></a>SGUI Problems

### Issue: GUI closes immediately

**Problem:** Not handling slot clicks properly or returning wrong value

**Solution:**
```java
// ✅ CORRECT callback
this.setSlot(0, new GuiElementBuilder()
    .setItem(Items.DIAMOND)
    .setCallback((index, type, action) -> {
        player.sendSystemMessage(Component.literal("Clicked!"));
        // Don't close GUI - it stays open by default
    })
);

// ❌ WRONG - This closes the GUI
@Override
public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
    return true;  // true = cancel action = might close
}
```

---

### Issue: Pagination doesn't work

**Problem:** Not calling `updatePage()` after page change

**Solution:**
```java
private int currentPage = 0;

this.setSlot(53, new GuiElementBuilder()
    .setItem(Items.ARROW)
    .setName(Component.literal("Next"))
    .setCallback((index, type, action) -> {
        currentPage++;
        updatePage();  // ✅ MUST CALL THIS
    })
);
```

---

### Issue: Items in GUI can be taken

**Problem:** GUI type allows item movement

**Solution:**
```java
// ✅ Use SimpleGui (not draggable)
public class MyGui extends SimpleGui {
    public MyGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);  // false = not manipulable
        // ...
    }
}
```

---

## <a id="commands"></a>Command Issues

### Issue: "requires permission" but I'm OP

**Problem:** Permission level not set correctly

**Solution:**
```java
// Default OP level is 2, but some servers use different levels
dispatcher.register(Commands.literal("admin")
    .requires(source -> source.hasPermission(2))  // Try 0, 1, 2, 3, or 4
    .executes(context -> {
        // ...
        return 1;
    })
);

// OR remove requirement for testing
dispatcher.register(Commands.literal("admin")
    // .requires(source -> source.hasPermission(2))  // ✅ Comment out
    .executes(context -> {
        // ...
        return 1;
    })
);
```

---

### Issue: Command suggestion doesn't appear

**Problem:** Not using proper argument registration

**Solution:**
```java
// ❌ WRONG - Suggestions won't work
dispatcher.register(Commands.literal("give")
    .executes(context -> {
        // parse manually...
    })
);

// ✅ CORRECT - Suggestions work
dispatcher.register(Commands.literal("give")
    .then(Commands.argument("item", StringArgumentType.word())  // ✅ Proper argument
        .executes(context -> {
            String item = StringArgumentType.getString(context, "item");
            // ...
            return 1;
        })
    )
);
```

---

## <a id="nbt"></a>NBT/Data Problems

### Issue: Data doesn't persist after restart

**Problem:** Not saving data properly or saving to wrong place

**Solution:**
```java
// ❌ WRONG - In-memory only
private Map<UUID, Integer> playerData = new HashMap<>();

// ✅ CORRECT - Use player persistent data
public static void saveData(ServerPlayer player, int value) {
    CompoundTag nbt = player.getPersistentData();
    CompoundTag modData = nbt.getCompound("yourmod");
    modData.putInt("value", value);
    nbt.put("yourmod", modData);  // ✅ Must call put() again
}
```

---

### Issue: "Optional is not present" error

**Problem:** Trying to get Optional value without checking

**Solution:**
```java
// ❌ WRONG
Optional<Tag> tag = nbt.get("key");
int value = ((IntTag) tag.get()).getAsInt();  // ❌ Crashes if empty

// ✅ CORRECT
if (nbt.contains("key")) {
    int value = nbt.getInt("key");
}

// ✅ ALSO CORRECT
int value = nbt.contains("key") ? nbt.getInt("key") : defaultValue;
```

---

## <a id="build"></a>Build Failures

### Error: "Unsupported class file major version 65"

**Problem:** Java version mismatch

**Solution:**
```powershell
# Check Java version
java -version  # Should show "21" or higher

# If wrong version, download Java 21:
# https://adoptium.net/
```

Then set JAVA_HOME and retry build.

---

### Error: "Could not resolve fabric-api"

**Problem:** Wrong Fabric API version or missing repository

**Solution:**
```gradle
repositories {
    mavenCentral()  // ✅ Must have this
}

dependencies {
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.141.1+1.21.11"  // ✅ Exact version
}
```

---

### Error: "Task 'runClient' not found"

**Problem:** Gradle not properly configured or not synced

**Solution:**
```powershell
# Delete .gradle folder and rebuild
Remove-Item -Recurse -Force .gradle
.\gradlew.bat build
```

---

### Error: Gradle daemon dies

**Problem:** Not enough memory allocated

**Solution:**
```properties
# In gradle.properties, increase memory
org.gradle.jvmargs=-Xmx4G  # Increase from 2G to 4G
```

---

## Quick Diagnostic Checklist

When something doesn't work, check these in order:

1. ✅ **Version Check:** `gradle.properties` shows `minecraft_version=1.21.11`
2. ✅ **Mappings Check:** `build.gradle` has `mappings loom.officialMojangMappings()`
3. ✅ **Import Check:** All `net.minecraft.*` imports are present
4. ✅ **Clean Build:** Run `.\gradlew.bat clean build`
5. ✅ **Reload:** Use `/reload` in-game after changes
6. ✅ **Logs:** Check logs in `run/logs/latest.log`

---

## Common Typos

| Wrong | Correct |
|-------|---------|
| `Component.litteral()` | `Component.literal()` |
| `ServerPlayer.sendMessage()` | `player.sendSystemMessage()` |
| `nbt.getInteger()` | `nbt.getInt()` |
| `GuiElementBuilder.addLore()` | `.addLoreLine()` |
| `Commands.arguement()` | `Commands.argument()` |

---

## Getting More Help

1. Check VS Code "Problems" tab for compilation errors
2. Read full error stack trace in console
3. Use `/reload` after code changes
4. Check `run/logs/latest.log` for runtime errors
5. Add debug logging: `LOGGER.info("Debug: " + value);`

---

## Next Steps

If issue persists:
- [02_CORE_API.md](02_CORE_API.md) - Verify API usage
- [08_PATTERNS.md](08_PATTERNS.md) - See working examples
- Check Fabric Discord or forums with specific error

---

**Remember:** When asking for help, always include:
- Full error message
- Minecraft version (1.21.11)
- Code snippet causing the problem
- What you've already tried
