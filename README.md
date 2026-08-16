# RestrictedInventory

A Minecraft mod that lets you lock specific player inventory slots to only accept certain items or item tags. Available for both **NeoForge** and **Fabric**.

## Features

- Restrict any inventory slot (0–35) to a specific item or item tag
- Two restriction modes: server-wide (all players share the same rules) or per-client (each player has their own rules)
- Visual overlay on restricted slots showing what item is required (cycles through tag members for tag-based restrictions)
- Pin the slot icon to one exact item variant with `display`, without changing what the slot accepts
- Group several entries — including NBT-specific ones — under one name and restrict a slot to the whole group
- Optionally give different players different rules through their vanilla scoreboard team or vanilla entity tags (opt-in, off by default)
- Hold **Tab** in any inventory screen to display slot indices — useful when setting up your config

## How It Works

When a slot is restricted:
- Players cannot manually place items that don't match the restriction into that slot
- Picked-up items are automatically routed to their allowed slots first; if no allowed slot is available, the item falls back to unrestricted slots
- The restricted slot displays a ghost item preview with a gray overlay so the player knows what belongs there

## Commands

All commands use the `/restrictedinventory` prefix.

| Command | Description | Permission |
|---------|-------------|------------|
| `/restrictedinventory config` | Opens the Restriction Config Screen | Admin (OP level 4), or any player when `useClientRestriction` is enabled |

### Permission rules

- When `useClientRestriction` is **false** (server-wide mode): only server operators (OP level 4) can open the config screen.
- When `useClientRestriction` is **true** (per-client mode): every player can open their own config screen.

## Restriction Config Screen

The config screen is the in-game GUI for editing slot restrictions. Open it with `/restrictedinventory config`.

### Item / Tag picker (top panel)

- **Items tab** — lists every registered item at ¾ scale. Hover for the item name and registry ID. Click to select.
- **Tags tab** — lists every registered item tag. Each tag cycles through its members as a live preview. Hover for the tag ID. Click to select.
- Scroll with the mouse wheel or drag the scrollbar on the right.

### Slot grid (middle panel)

- Displays all 36 player inventory slots (0–35) laid out in the standard inventory arrangement.
- Each slot shows a cycling preview of its current restriction, or is empty if unrestricted.
- Left-Click a slot to **select** it (highlighted in white). The selected slot will receive the item or tag chosen in the picker.
- Right-Click a slot to remove the item or tag chosen.
- Hover a slot to see its current restriction value as a tooltip.

## Configuration

The mod uses JSON config files (powered by [CoolerConfig](https://github.com/CoolerProMC/CoolerConfig)).

### Common Config (`config/restrictedinventory-common.json`)

Applies to the server. Controls all players unless `useClientRestriction` is enabled.

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `useClientRestriction` | boolean | `false` | When `true`, each player's restrictions come from their own client config instead of this file |
| `useTeamAndTagRestrictions` | boolean | `false` | When `true`, `teamRestrictions` and `tagRestrictions` are read and can target individual players |
| `restrictedSlots` | object | `{}` | Map of slot index → item ID, tag, or restriction entry |
| `groups` | object | `{}` | Map of group name → list of entries a slot can be restricted to as a whole |
| `teamRestrictions` | object | `{}` | Map of vanilla scoreboard team name → restriction profile |
| `tagRestrictions` | object | `{}` | Map of vanilla entity tag → restriction profile |

Groups live in the common config only and are synced to every connecting client, so a group id means
the same thing on both sides even when `useClientRestriction` is enabled.

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
| `{"item": "...", "tag": {...}}` | The item, narrowed to stacks carrying that NBT |
| `{"item": "...", "display": ...}` | The item, drawn as some other stack |
| `{"group": "..."}` | Anything in the named restriction group |

`tag` may also be written as `nbt`; both are read, and `tag` is what gets written back out.

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

### Custom Display

A restriction may name a separate `display` stack. It only decides what the empty slot draws — it
never changes what the slot accepts. This is what makes mods like TaCZ usable, where every gun
shares one item ID and the actual weapon lives in NBT:

```json
{
  "restrictedSlots": {
    "0": {
      "item": "tacz:modern_kinetic_gun",
      "display": {
        "item": "tacz:modern_kinetic_gun",
        "nbt": { "GunId": "tacz:m4a1" }
      }
    }
  }
}
```

The slot accepts *any* TaCZ gun, because the restriction itself has no NBT filter, but draws the M4
instead of the generic base item. `display` is optional; without it the slot renders exactly as
before.

### Restriction Groups

A group is a named set of entries that a slot can be restricted to as a whole. The slot accepts an
item when **any** entry in the group matches:

```json
{
  "groups": {
    "handguns": [
      { "item": "tacz:modern_kinetic_gun", "nbt": { "GunId": "tacz:glock_17" } },
      { "item": "tacz:modern_kinetic_gun", "nbt": { "GunId": "tacz:m1911" } }
    ],
    "flashlights": {
      "entries": ["someflashlightmod:flashlight", "anothermod:torchlight"],
      "display": "someflashlightmod:flashlight"
    }
  },
  "restrictedSlots": {
    "0": { "group": "restrictedinventory:handguns" },
    "1": { "group": "flashlights" }
  }
}
```

- A group name without a namespace is read as `restrictedinventory:<name>`, so `handguns` and
  `restrictedinventory:handguns` refer to the same group.
- Entries may be plain item IDs, `#tags`, or item + NBT objects — the same forms a slot accepts.
- Groups cannot contain other groups, so a group can never reference itself.
- Written as a bare list, or as an object with `entries` and an optional group-wide `display`.
- Without an explicit `display`, a group slot cycles through its members' icons.

**Groups are not a replacement for item tags.** When you are grouping normal, distinct item IDs, a
datapack item tag such as `#restrictedinventory:handguns` is still the better tool and keeps working
unchanged. Reach for a RestrictedInventory group when the members differ only by NBT, which an item
tag cannot express.

### Team and Tag Restrictions

Different players can be given different restrictions based on the vanilla scoreboard team they are
on and the vanilla entity tags they carry. The feature is **off by default**:

```json
{
  "useTeamAndTagRestrictions": false
}
```

- While it is `false`, nothing changes: no team is looked up, no tag is looked up, and every player
  keeps using the behaviour described above.
- While it is `true`, a player who matches a configured team or tag rule uses **that** set instead of
  `restrictedSlots`. A player who matches nothing falls back to `restrictedSlots` as before.

The global `restrictedSlots` is a fallback, **not** a base layer: once any team or tag rule matches, the
global slots are not part of the result at all.

```json
{
  "useTeamAndTagRestrictions": true,
  "restrictedSlots": {
    "9": "minecraft:bread"
  },
  "teamRestrictions": {
    "prisoners": {
      "restrictedSlots": {
        "9": "minecraft:barrier",
        "10": "minecraft:barrier"
      }
    },
    "guards": {
      "restrictedSlots": {
        "9": "minecraft:iron_sword",
        "10": "minecraft:shield"
      }
    }
  },
  "tagRestrictions": {
    "no_weapons": {
      "restrictedSlots": {
        "0": "minecraft:barrier",
        "1": "minecraft:barrier"
      }
    }
  }
}
```

Membership is managed entirely with vanilla commands — RestrictedInventory only reads the result and
adds no commands of its own:

```
/team add prisoners
/team join prisoners Steve
```

```
/tag Steve add no_weapons
/tag Steve remove no_weapons
```

Changes take effect immediately; the server re-reads each player's team and tags every tick, so a
`/team join` or `/tag remove` is reflected without a relog or a config reload. Nothing is copied into
the player's saved data — the rules stay attached to the team or tag, never to the player.

#### How the pieces combine

- A vanilla player is on **at most one** team, so at most one team rule can apply.
- A player can have **many** tags, and every matching tag rule is applied.
- Tags a player has that RestrictedInventory has no rule for are ignored — an unrelated `afk` tag from
  another mod never causes the global restrictions to be dropped.
- When several sources claim the same slot, the winner is:

  ```
  player's own restrictions  >  tag  >  team  >  global fallback
  ```

  So a `special_weapon` tag putting `minecraft:diamond_sword` in slot 9 overrides a `prisoners` team
  rule putting `minecraft:barrier` there.
- When two matching **tags** claim the same slot, the tag names are sorted alphabetically and applied
  in that order, so the later name wins. The same player with the same tags and the same config always
  resolves to the same result, no matter what order the game stores their tags in.
- "The player's own restrictions" only exist in per-client mode (`useClientRestriction: true`), where
  each player supplies their own set; those entries stay the most specific layer and are laid over the
  team/tag result. With `useClientRestriction: false` every player shares one config, so a matching
  team/tag rule simply replaces it.
- A profile with an empty `restrictedSlots` still counts as a match, which is the way to give a team or
  tag *no* restrictions at all while everyone else keeps the global ones.

Configured teams and tags do not have to exist. A `boss_fight` team rule is valid before anybody has
run `/team add boss_fight`; it simply starts applying once a player joins a team with that name.

Resolution happens on the server, which stays authoritative — the client is told what applies to it
through the same channel that already carries restrictions, and never inspects teams or tags itself.

## Compatibility

This mod works by intercepting the standard `Slot#mayPlace` and `Inventory#addResource` methods via Mixin. Any code path that goes through these methods will respect slot restrictions correctly.

However, there are cases where restrictions **may not be enforced**:

- **Direct inventory insertion** — Some mods, modded menus, or block entities insert items directly into specific inventory indices without going through the normal slot placement logic. These bypasses cannot be caught by this mod.
- **Conflicting Mixins** — Mods that also mixin into `Slot`, `Inventory`, or `AbstractContainerScreen` in ways that conflict with this mod's patches may cause unexpected behavior or make restrictions stop working entirely.
- **Custom inventory implementations** — Mods that replace or wrap the vanilla `Inventory` class with their own implementation will not be affected by this mod's logic.

If you encounter an incompatibility, please open an issue and include the mod that is causing the conflict.