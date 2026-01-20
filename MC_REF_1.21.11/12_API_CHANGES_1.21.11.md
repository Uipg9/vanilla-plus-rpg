# API Changes in Minecraft 1.21.11

This document covers critical API changes discovered while developing for Minecraft 1.21.11 that differ from earlier versions.

## 🔴 Critical NBT API Changes

The NBT system has been completely refactored. Methods now return `Optional` types by default.

### CompoundTag Methods

**❌ OLD (1.20.x and earlier):**
```java
int value = tag.getInt("key");  // Returns 0 if missing
String str = tag.getString("key");  // Returns "" if missing
CompoundTag sub = tag.getCompound("key");  // Returns empty tag if missing
ListTag list = tag.getList("key", 10);  // 2 parameters
Set<String> keys = tag.getAllKeys();
```

**✅ NEW (1.21.11):**
```java
int value = tag.getIntOr("key", 0);  // Explicit default
String str = tag.getStringOr("key", "");  // Explicit default
CompoundTag sub = tag.getCompoundOrEmpty("key");  // Returns empty if missing
ListTag list = tag.getListOrEmpty("key");  // 1 parameter - type inferred
Set<String> keys = tag.keySet();  // Renamed method
```

### ListTag Methods

**❌ OLD:**
```java
CompoundTag element = list.getCompound(i);
```

**✅ NEW:**
```java
CompoundTag element = list.getCompoundOrEmpty(i);
```

### Optional-returning Methods

If you need the Optional, these methods exist:
```java
Optional<Integer> optInt = tag.getInt("key");
Optional<String> optStr = tag.getString("key");
Optional<CompoundTag> optTag = tag.getCompound("key");
```

---

## 🔴 ResourceLocation → Identifier

**❌ OLD:**
```java
import net.minecraft.resources.ResourceLocation;

ResourceLocation id = new ResourceLocation("modid:item");
ResourceLocation id2 = ResourceLocation.fromNamespaceAndPath("modid", "item");
```

**✅ NEW:**
```java
import net.minecraft.resources.Identifier;

Identifier id = Identifier.of("modid:item");
Identifier id2 = Identifier.fromNamespaceAndPath("modid", "item");
```

**Note:** Use `fromNamespaceAndPath()` for custom namespaces, `of()` for vanilla/existing resources.

---

## 🔴 ServerPlayer Methods

**❌ OLD:**
```java
ServerLevel level = player.serverLevel();
player.playNotifySound(sound, volume, pitch);
```

**✅ NEW:**
```java
ServerLevel level = player.level();  // Direct cast

// playNotifySound no longer exists - use level-based sound:
level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
```

### Recommended Pattern

Create a helper class:
```java
public class GuiHelper {
    public static void playSound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(
            null, 
            player.blockPosition(), 
            sound, 
            SoundSource.PLAYERS, 
            volume, 
            pitch
        );
    }
}
```

---

## 🔴 Item Methods

**❌ OLD:**
```java
Component description = item.getDescription();
```

**✅ NEW:**
```java
Component name = item.getName();
```

---

## 🔴 Command Permission System

The permission system has been completely rewritten.

**❌ OLD:**
```java
import net.minecraft.commands.CommandSourceStack;

.requires(src -> src.hasPermission(2))  // Level-based
```

**✅ NEW:**
```java
import net.minecraft.server.permissions.Permissions;

.requires(src -> {
    try {
        return src.getPlayerOrException()
            .permissions()
            .hasPermission(Permissions.COMMANDS_GAMEMASTER);
    } catch (Exception e) {
        return false;
    }
})
```

### Available Permission Levels

```java
Permissions.COMMANDS_MODERATOR      // Level 1
Permissions.COMMANDS_GAMEMASTER     // Level 2 (old hasPermission(2))
Permissions.COMMANDS_ADMIN          // Level 3
Permissions.COMMANDS_OWNER          // Level 4
Permissions.COMMANDS_ENTITY_SELECTORS  // @ selectors
```

### Recommended Pattern

```java
private static boolean hasOpPermission(CommandSourceStack src) {
    try {
        ServerPlayer player = src.getPlayerOrException();
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    } catch (Exception e) {
        return false;
    }
}

// Then use:
.requires(MyCommand::hasOpPermission)
```

---

## 🔴 KeyBinding API

KeyBindings now use a `Category` record instead of String.

**❌ OLD:**
```java
import net.minecraft.client.KeyMapping;

KeyMapping key = new KeyMapping(
    "key.modid.name",
    GLFW.GLFW_KEY_G,
    "category.modid.main"  // String category
);
```

**✅ NEW:**
```java
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;

// Register category first
KeyMapping.Category CATEGORY = KeyMapping.Category.register(
    Identifier.fromNamespaceAndPath("modid", "main")
);

KeyMapping key = new KeyMapping(
    "key.modid.name",
    InputConstants.Type.KEYSYM,  // Type required now
    GLFW.GLFW_KEY_G,
    CATEGORY  // Category object
);
```

---

## 🔴 SavedData System

The SavedData system has changed to use Codecs.

**❌ OLD:**
```java
public class MyData extends SavedData {
    public static MyData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            MyData::load,
            MyData::new,
            "my_data"
        );
    }
    
    public static MyData load(CompoundTag tag) {
        // ...
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        // ...
    }
}
```

**✅ NEW (Codec-based - complex):**
```java
// Requires defining a Codec and SavedDataType
// Very verbose and complex for simple use cases
```

**✅ WORKAROUND (File-based):**
```java
public class MyData {
    private static final String FILE_NAME = "my_data.dat";
    
    public static MyData getOrCreate(ServerLevel level) {
        Path path = getPath(level);
        if (Files.exists(path)) {
            try {
                CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
                return fromNBT(tag);
            } catch (IOException e) {
                // Log and return new
            }
        }
        return new MyData();
    }
    
    public void save(ServerLevel level) {
        Path path = getPath(level);
        try {
            Files.createDirectories(path.getParent());
            CompoundTag tag = toNBT();
            NbtIo.writeCompressed(tag, path);
        } catch (IOException e) {
            // Log error
        }
    }
    
    private static Path getPath(ServerLevel level) {
        return level.getServer()
            .getWorldPath(LevelResource.ROOT)
            .resolve("data")
            .resolve(FILE_NAME);
    }
}
```

---

## 🟡 Registry Access

**✅ Still works but pattern changed:**

```java
import net.minecraft.core.registries.BuiltInRegistries;

// Get item by identifier
Item item = BuiltInRegistries.ITEM.getValue(identifier);

// Get identifier from item
Identifier id = BuiltInRegistries.ITEM.getKey(item);
```

---

## 🟢 Sound Events

**✅ Still mostly compatible:**

```java
import net.minecraft.sounds.SoundEvents;

SoundEvent event = SoundEvents.PLAYER_LEVELUP;
SoundEvent event2 = SoundEvents.NOTE_BLOCK_BASS.value();  // Some require .value()
```

---

## Tips for Migration

1. **Search decompiled sources**: When in doubt, extract from the Minecraft JAR:
   ```powershell
   # Find the JAR
   $jarPath = "~/.gradle/caches/fabric-loom/.../minecraft-merged-*.jar"
   
   # Extract a class
   Add-Type -Assembly System.IO.Compression.FileSystem
   $zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
   $entry = $zip.Entries | Where-Object { $_.FullName -eq "net/minecraft/.../Class.class" }
   # Extract and decompile with javap
   ```

2. **Use IDE autocomplete**: Let the IDE show you what methods actually exist.

3. **Check Fabric API changes**: Some changes are Fabric-specific, not Mojang.

4. **Test incrementally**: Build after each API change to catch errors early.

---

## Common Error Messages

### "cannot find symbol: method hasPermission(int)"
- **Cause**: Permission system changed
- **Fix**: Use `player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)`

### "cannot find symbol: method getDescription()"
- **Cause**: Item method renamed
- **Fix**: Use `item.getName()`

### "incompatible types: String cannot be converted to Category"
- **Cause**: KeyBinding Category is now a record
- **Fix**: Use `KeyMapping.Category.register(Identifier.fromNamespaceAndPath(...))`

### "cannot find symbol: method getAllKeys()"
- **Cause**: Method renamed
- **Fix**: Use `tag.keySet()`

### "cannot find symbol: method getInt(String)"
- **Cause**: Returns Optional now
- **Fix**: Use `tag.getIntOr("key", defaultValue)`

---

## 🔴 Screen API Changes

### hasShiftDown() Does Not Exist

**❌ OLD:**
```java
if (Screen.hasShiftDown()) { ... }
```

**✅ NEW - Use GLFW Directly:**
```java
import org.lwjgl.glfw.GLFW;

protected boolean isShiftDown() {
    long window = GLFW.glfwGetCurrentContext();
    return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
           GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
}
```

### mouseClicked Signature Changed

**❌ OLD:**
```java
@Override
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    return super.mouseClicked(mouseX, mouseY, button);
}
```

**✅ NEW - Remove @Override:**
```java
// No @Override annotation - method signature changed
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    return super.mouseClicked(mouseX, mouseY, button);
}
```

---

## 🔴 Player Permission Check

### hasPermissions(int) Does Not Exist

**❌ OLD:**
```java
if (player.hasPermissions(2)) { ... }
```

**✅ NEW - Simplified Approach:**
```java
// For basic op detection, use game mode check:
if (player.isCreative() || player.isSpectator()) { ... }

// Or check if single player:
if (source.getServer().isSingleplayer()) { ... }
```

---

## 🔴 Window Handle Access

### getWindow().getWindow() Path Broken

**❌ OLD:**
```java
long window = Minecraft.getInstance().getWindow().getWindow();
```

**✅ NEW:**
```java
long window = GLFW.glfwGetCurrentContext();
```

---

## Build Configuration

Ensure your `build.gradle` has the correct versions:

```gradle
minecraft {
    version = "1.21.11"
    mappings "mojang"  // Use Mojang mappings, not Yarn
}

dependencies {
    minecraft "com.mojang:minecraft:1.21.11"
    mappings loom.officialMojangMappings()
    modImplementation "net.fabricmc:fabric-loader:0.18.4"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.141.1+1.21.11"
}
```

---

**Last Updated:** January 2026  
**Minecraft Version:** 1.21.11  
**Mapping:** Mojang Official
