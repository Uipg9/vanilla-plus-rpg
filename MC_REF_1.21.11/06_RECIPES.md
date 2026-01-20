# 🍳 Recipes & Crafting - Minecraft 1.21.11

**Complete guide to adding custom recipes for 1.21.11**

---

## Table of Contents

1. [Recipe File Structure](#structure)
2. [Shaped Recipes](#shaped)
3. [Shapeless Recipes](#shapeless)
4. [Smelting Recipes](#smelting)
5. [Recipe Advancement Integration](#advancement)
6. [Testing Recipes](#testing)

---

## <a id="structure"></a>Recipe File Structure

### Directory Layout

```
src/main/resources/data/yourmod/recipes/
├── shaped_example.json
├── shapeless_example.json
├── smelting_example.json
└── blasting_example.json
```

### Basic Recipe Template

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "###",
    "# #",
    "###"
  ],
  "key": {
    "#": {
      "item": "minecraft:diamond"
    }
  },
  "result": {
    "item": "minecraft:diamond_block",
    "count": 1
  }
}
```

---

## <a id="shaped"></a>Shaped Recipes

### Example: Custom Tool Recipe

**data/yourmod/recipes/magic_wand.json:**
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "tools",
  "pattern": [
    "  D",
    " S ",
    "S  "
  ],
  "key": {
    "D": {
      "item": "minecraft:diamond"
    },
    "S": {
      "item": "minecraft:stick"
    }
  },
  "result": {
    "item": "minecraft:stick",
    "count": 1,
    "components": {
      "minecraft:custom_name": "{\"text\":\"Magic Wand\",\"color\":\"gold\"}"
    }
  }
}
```

### Multiple Input Options (Tags)

**data/yourmod/recipes/wooden_gear.json:**
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " W ",
    "WSW",
    " W "
  ],
  "key": {
    "W": {
      "tag": "minecraft:planks"
    },
    "S": {
      "item": "minecraft:stick"
    }
  },
  "result": {
    "item": "minecraft:wooden_sword",
    "count": 1
  }
}
```

### 2x2 Recipe

**data/yourmod/recipes/compressed_coal.json:**
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "CC",
    "CC"
  ],
  "key": {
    "C": {
      "item": "minecraft:coal"
    }
  },
  "result": {
    "item": "minecraft:coal_block",
    "count": 1
  }
}
```

---

## <a id="shapeless"></a>Shapeless Recipes

### Basic Shapeless Recipe

**data/yourmod/recipes/mystery_dust.json:**
```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    {
      "item": "minecraft:glowstone_dust"
    },
    {
      "item": "minecraft:redstone"
    },
    {
      "item": "minecraft:gunpowder"
    }
  ],
  "result": {
    "item": "minecraft:blaze_powder",
    "count": 3
  }
}
```

### Multiple Same Item

**data/yourmod/recipes/diamond_from_fragments.json:**
```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    {
      "item": "minecraft:diamond"
    },
    {
      "item": "minecraft:diamond"
    },
    {
      "item": "minecraft:diamond"
    },
    {
      "item": "minecraft:diamond"
    },
    {
      "item": "minecraft:emerald"
    }
  ],
  "result": {
    "item": "minecraft:diamond",
    "count": 5
  }
}
```

### Using Item Tags

**data/yourmod/recipes/wool_to_string.json:**
```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    {
      "tag": "minecraft:wool"
    }
  ],
  "result": {
    "item": "minecraft:string",
    "count": 4
  }
}
```

---

## <a id="smelting"></a>Smelting Recipes

### Furnace Recipe

**data/yourmod/recipes/cooked_egg.json:**
```json
{
  "type": "minecraft:smelting",
  "category": "food",
  "ingredient": {
    "item": "minecraft:egg"
  },
  "result": "minecraft:cooked_chicken",
  "experience": 0.35,
  "cookingtime": 200
}
```

### Blast Furnace Recipe

**data/yourmod/recipes/fast_iron.json:**
```json
{
  "type": "minecraft:blasting",
  "category": "blocks",
  "ingredient": {
    "item": "minecraft:iron_ore"
  },
  "result": "minecraft:iron_ingot",
  "experience": 0.7,
  "cookingtime": 100
}
```

### Smoker Recipe

**data/yourmod/recipes/fast_beef.json:**
```json
{
  "type": "minecraft:smoking",
  "category": "food",
  "ingredient": {
    "item": "minecraft:beef"
  },
  "result": "minecraft:cooked_beef",
  "experience": 0.35,
  "cookingtime": 100
}
```

### Campfire Cooking

**data/yourmod/recipes/campfire_potato.json:**
```json
{
  "type": "minecraft:campfire_cooking",
  "category": "food",
  "ingredient": {
    "item": "minecraft:potato"
  },
  "result": "minecraft:baked_potato",
  "experience": 0.35,
  "cookingtime": 600
}
```

---

## <a id="advancement"></a>Recipe Advancement Integration

### Recipe with Unlock Criteria

**data/yourmod/recipes/advanced_tool.json:**
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "tools",
  "pattern": [
    "DDD",
    " S ",
    " S "
  ],
  "key": {
    "D": {
      "item": "minecraft:diamond"
    },
    "S": {
      "item": "minecraft:stick"
    }
  },
  "result": {
    "item": "minecraft:diamond_pickaxe"
  },
  "advancement": {
    "trigger": "minecraft:inventory_changed",
    "criteria": {
      "has_diamond": {
        "trigger": "minecraft:inventory_changed",
        "conditions": {
          "items": [
            {
              "items": ["minecraft:diamond"]
            }
          ]
        }
      }
    }
  }
}
```

---

## <a id="testing"></a>Testing Recipes

### In-Game Testing

1. Place recipe JSON in `data/yourmod/recipes/`
2. Run client: `.\gradlew.bat runClient`
3. Use `/reload` command to refresh recipes
4. Check recipe book or try crafting

### Debug Recipe Loading

**In your mod initializer:**
```java
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

@Override
public void onInitialize() {
    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
        server.getRecipeManager().getRecipes().forEach(recipe -> {
            if (recipe.id().getNamespace().equals("yourmod")) {
                LOGGER.info("Loaded recipe: " + recipe.id());
            }
        });
    });
}
```

---

## Recipe Types Reference

| Type | JSON Type | Use Case |
|------|-----------|----------|
| **Shaped** | `minecraft:crafting_shaped` | Specific pattern required |
| **Shapeless** | `minecraft:crafting_shapeless` | Order doesn't matter |
| **Smelting** | `minecraft:smelting` | Furnace (200 ticks default) |
| **Blasting** | `minecraft:blasting` | Blast furnace (100 ticks) |
| **Smoking** | `minecraft:smoking` | Smoker (100 ticks) |
| **Campfire** | `minecraft:campfire_cooking` | Campfire (600 ticks) |
| **Stonecutting** | `minecraft:stonecutting` | Stonecutter |
| **Smithing** | `minecraft:smithing_transform` | Smithing table |

---

## Common Item Tags (1.21.11)

| Tag | Items Included |
|-----|----------------|
| `minecraft:planks` | All wood planks |
| `minecraft:logs` | All logs and wood |
| `minecraft:wool` | All colored wool |
| `minecraft:stone_crafting_materials` | Cobblestone, blackstone, etc. |
| `minecraft:coals` | Coal and charcoal |

---

## Real-World Examples

### Example 1: Upgrade Recipe

**data/yourmod/recipes/iron_to_diamond_pickaxe.json:**
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "tools",
  "pattern": [
    "DDD",
    " P ",
    "   "
  ],
  "key": {
    "D": {
      "item": "minecraft:diamond"
    },
    "P": {
      "item": "minecraft:iron_pickaxe"
    }
  },
  "result": {
    "item": "minecraft:diamond_pickaxe"
  }
}
```

### Example 2: Repair Recipe

**data/yourmod/recipes/repair_elytra.json:**
```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    {
      "item": "minecraft:elytra"
    },
    {
      "item": "minecraft:phantom_membrane"
    },
    {
      "item": "minecraft:phantom_membrane"
    }
  ],
  "result": {
    "item": "minecraft:elytra"
  }
}
```

### Example 3: Bulk Crafting

**data/yourmod/recipes/bulk_torches.json:**
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "CCC",
    "SSS"
  ],
  "key": {
    "C": {
      "tag": "minecraft:coals"
    },
    "S": {
      "item": "minecraft:stick"
    }
  },
  "result": {
    "item": "minecraft:torch",
    "count": 12
  }
}
```

---

## Best Practices

✅ Use descriptive file names (e.g., `iron_sword_from_nuggets.json`)
✅ Include recipe unlock criteria for better UX
✅ Use tags for flexibility (e.g., `minecraft:planks` instead of specific wood)
✅ Test with `/reload` after changes
✅ Set appropriate experience values for smelting
✅ Use categories to organize recipe book
✅ Document custom recipes in mod README

---

## Next Steps

Continue to:
- [01_SETUP.md](01_SETUP.md) - Project structure for recipes
- [08_PATTERNS.md](08_PATTERNS.md) - Programmatic recipe creation
- [07_TROUBLESHOOTING.md](07_TROUBLESHOOTING.md) - Recipe not loading?

---

**Version Note:** All recipes are for Minecraft 1.21.11. Recipe format may differ in other versions.
