# RestrictedInventory

A Minecraft mod that lets you lock specific player inventory slots to only accept certain items or item tags. Available for both **NeoForge** and **Fabric**.

## Features

- Restrict any inventory slot (0–35) to a specific item or item tag
- Two restriction modes: server-wide (all players share the same rules) or per-client (each player has their own rules)
- Visual overlay on restricted slots showing what item is required (cycles through tag members for tag-based restrictions)
- Hold **Tab** in any inventory screen to display slot indices — useful when setting up your config

## How It Works

When a slot is restricted:
- Players cannot manually place items that don't match the restriction into that slot
- Picked-up items are automatically routed to their allowed slots first; if no allowed slot is available, the item falls back to unrestricted slots
- The restricted slot displays a ghost item preview with a gray overlay so the player knows what belongs there

## Configuration

The mod uses JSON config files (powered by [CoolerConfig](https://github.com/CoolerProMC/CoolerConfig)).

### Common Config (`config/restrictedinventory-common.json`)

Applies to the server. Controls all players unless `useClientRestriction` is enabled.

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `useClientRestriction` | boolean | `false` | When `true`, each player's restrictions come from their own client config instead of this file |
| `restrictedSlots` | object | `{}` | Map of slot index → item ID or tag |

### Client Config (`config/restrictedinventory-client.json`)

Only used when `useClientRestriction` is `true` on the server.

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `showSlotIndex` | boolean | `true` | Show slot numbers when Tab is held in an inventory screen |
| `restrictedSlots` | object | `{}` | Map of slot index → item ID or tag |

### Slot Numbering

| Slot range | Location |
|------------|----------|
| 0–8 | Hotbar |
| 9–35 | Main inventory |

Hold **Tab** in-game with `showSlotIndex` enabled to see each slot's index overlaid on your inventory.

### Restriction Value Format

| Value | Meaning |
|-------|---------|
| `"minecraft:diamond"` | Only diamonds are allowed |
| `"#minecraft:swords"` | Any item in the `minecraft:swords` tag is allowed |

### Example Config

```json
{
  "useClientRestriction": false,
  "restrictedSlots": {
    "0": "minecraft:torch",
    "1": "#minecraft:swords",
    "8": "minecraft:totem_of_undying"
  }
}
```

This locks hotbar slot 0 to torches, slot 1 to any sword, and slot 8 to totems of undying.

## Compatibility

This mod works by intercepting the standard `Slot#mayPlace` and `Inventory#addResource` methods via Mixin. Any code path that goes through these methods will respect slot restrictions correctly.

However, there are cases where restrictions **may not be enforced**:

- **Direct inventory insertion** — Some mods, modded menus, or block entities insert items directly into specific inventory indices without going through the normal slot placement logic. These bypasses cannot be caught by this mod.
- **Conflicting Mixins** — Mods that also mixin into `Slot`, `Inventory`, or `AbstractContainerScreen` in ways that conflict with this mod's patches may cause unexpected behavior or make restrictions stop working entirely.
- **Custom inventory implementations** — Mods that replace or wrap the vanilla `Inventory` class with their own implementation will not be affected by this mod's logic.

If you encounter an incompatibility, please open an issue and include the mod that is causing the conflict.