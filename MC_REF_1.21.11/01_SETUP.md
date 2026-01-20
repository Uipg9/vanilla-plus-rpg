# 🔧 Complete Project Setup - Minecraft 1.21.11 Fabric

**For Minecraft 1.21.11 ONLY** - Setup guide from zero to working mod.

> ✅ **Production-Tested**: These exact versions power Pocket Life mod (7,800+ lines, fully functional).
> 
> ⚠️ **Version Critical**: Using 1.21.0, 1.21.4, or 1.20.x will cause crashes. Only 1.21.11 works with this guide.

---

## Prerequisites

### Required Software

| Software | Version | Download |
|----------|---------|----------|
| **Java JDK** | 21 or newer | https://adoptium.net/ (Temurin recommended) |
| **Git** | Latest | https://git-scm.com/downloads |
| **IDE** | VS Code or IntelliJ | Your preference |

### Verify Installation

```powershell
java -version    # Should show "21" or higher
git --version    # Any recent version
```

---

## Method 1: Quick Start (Recommended)

### Step 1: Use Fabric Template Generator

1. Go to https://fabricmc.net/develop/template/
2. Configure:
   - **Minecraft Version:** 1.21.11
   - **Mod Name:** YourModName
   - **Package:** com.yourname.modname
3. Download ZIP and extract

### Step 2: Configure gradle.properties

Replace entire contents with:

```properties
# Fabric Properties - VERIFIED FOR 1.21.11
# These EXACT versions are tested and working
minecraft_version=1.21.11
loader_version=0.18.4
fabric_version=0.141.1+1.21.11

# Mod Properties
mod_version=1.0.0
maven_group=com.yourname
archives_base_name=modname

# Build Settings
loom.platform=fabric
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
```

### Step 3: Configure build.gradle

```gradle
plugins {
    id 'fabric-loom' version '1.14.10'
    id 'maven-publish'
}

version = project.mod_version
group = project.maven_group

repositories {
    mavenCentral()
    // For SGUI library (if needed)
    maven { url 'https://maven.nucleoid.xyz' }
}

dependencies {
    // Minecraft & Fabric
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    
    // ⚠️ CRITICAL: Use Mojang mappings for 1.21.11
    mappings loom.officialMojangMappings()
    
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
    
    // Optional: SGUI for inventory GUIs
    // include(modImplementation("eu.pb4:sgui:1.12.0+1.21.11"))
}

processResources {
    inputs.property "version", project.version
    filteringCharset "UTF-8"
    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}

def targetJavaVersion = 21
tasks.withType(JavaCompile).configureEach {
    it.options.encoding = "UTF-8"
    it.options.release = targetJavaVersion
}

java {
    def javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    withSourcesJar()
}

jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}"}
    }
}
```

### Step 4: Configure fabric.mod.json

```json
{
  "schemaVersion": 1,
  "id": "modname",
  "version": "${version}",
  "name": "Your Mod Name",
  "description": "Description of your mod for 1.21.11",
  "authors": ["YourName"],
  "contact": {
    "homepage": "https://example.com",
    "sources": "https://github.com/yourname/modname"
  },
  "license": "MIT",
  "icon": "assets/modname/icon.png",
  "environment": "*",
  "entrypoints": {
    "main": [
      "com.yourname.modname.ModName"
    ]
  },
  "mixins": [],
  "depends": {
    "fabricloader": ">=0.18.0",
    "minecraft": "1.21.11",
    "java": ">=21",
    "fabric-api": "*"
  }
}
```

### Step 5: Create Main Class

**src/main/java/com/yourname/modname/ModName.java:**

```java
package com.yourname.modname;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModName implements ModInitializer {
    public static final String MOD_ID = "modname";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {} for Minecraft 1.21.11", MOD_ID);
        
        // Example command
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
            dispatcher.register(Commands.literal(MOD_ID)
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.sendSystemMessage(Component.literal("§a" + MOD_ID + " is working!"));
                    return 1;
                })
            );
        });
        
        LOGGER.info("{} initialized successfully!", MOD_ID);
    }
}
```

### Step 6: First Build

```powershell
.\gradlew.bat build
```

### Step 7: Test in Dev Environment

```powershell
.\gradlew.bat runClient
```

---

## Project Structure

```
YourMod/
├── src/
│   └── main/
│       ├── java/com/yourname/modname/
│       │   ├── ModName.java              # Main initializer
│       │   ├── commands/                 # Command classes
│       │   ├── gui/                      # GUI classes
│       │   ├── data/                     # Data storage
│       │   ├── managers/                 # Business logic
│       │   └── util/                     # Helper classes
│       └── resources/
│           ├── fabric.mod.json           # Mod metadata
│           └── assets/modname/
│               ├── icon.png              # 128x128 PNG
│               └── lang/
│                   └── en_us.json        # Translations
├── gradle/
│   └── wrapper/
├── build.gradle                          # Build config
├── gradle.properties                     # Versions
└── settings.gradle                       # Project settings
```

---

## Adding SGUI for GUIs

### Step 1: Add to build.gradle repositories

```gradle
repositories {
    mavenCentral()
    maven { url 'https://maven.nucleoid.xyz' }
}
```

### Step 2: Add to dependencies

```gradle
dependencies {
    // ... existing dependencies ...
    
    // SGUI - include() bundles it in your JAR
    include(modImplementation("eu.pb4:sgui:1.12.0+1.21.11"))
}
```

### Step 3: Sync/Reload Gradle

---

## Common Gradle Commands

```powershell
# Build the mod JAR
.\gradlew.bat build

# Run Minecraft client for testing
.\gradlew.bat runClient

# Run dedicated server for testing
.\gradlew.bat runServer

# Clean build artifacts
.\gradlew.bat clean

# Regenerate IDE files
.\gradlew.bat idea      # IntelliJ
.\gradlew.bat eclipse   # Eclipse
```

---

## Troubleshooting Setup

### Issue: "cannot find symbol: ServerPlayer"

**Cause:** Not using Mojang Official mappings

**Fix:**
```gradle
// In build.gradle, ensure you have:
mappings loom.officialMojangMappings()
```

Then clean rebuild:
```powershell
.\gradlew.bat clean build
```

### Issue: "Unsupported class file major version 65"

**Cause:** Java version mismatch

**Fix:** Verify Java 21+ is installed and set as default:
```powershell
java -version   # Should show 21 or higher
```

### Issue: Gradle daemon fails

**Fix:** Increase memory in gradle.properties:
```properties
org.gradle.jvmargs=-Xmx4G
```

---

## IDE Configuration

### VS Code Setup

1. Install extensions:
   - Extension Pack for Java (Microsoft)
   - Gradle for Java
2. Open project folder
3. Let Gradle sync automatically

### IntelliJ IDEA Setup

1. Open project
2. Trust Gradle project
3. File → Project Structure → Project SDK → Set to Java 21
4. Let Gradle import finish
5. Run → Edit Configurations → Add Application → Main class: net.fabricmc.devlaunchinjector.Main

---

## Next Steps

✅ Project is now set up for 1.21.11!

Continue to:
- [02_CORE_API.md](02_CORE_API.md) - Learn the APIs
- [03_GUI_SYSTEMS.md](03_GUI_SYSTEMS.md) - Build inventory GUIs
- [04_COMMANDS.md](04_COMMANDS.md) - Add commands
- [08_PATTERNS.md](08_PATTERNS.md) - Copy working code

---

**Remember:** Always verify gradle.properties shows `minecraft_version=1.21.11`
