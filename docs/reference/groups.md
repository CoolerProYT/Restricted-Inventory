# Groups and displays

Two optional additions to a slot rule:

- **`display`** changes the icon a restricted slot draws, without changing what the slot accepts.
- **`group`** points a slot at a named set of entries and accepts anything in that set.

Both exist for mods that hide many different items behind one item ID and tell them apart with variant data. TaCZ is the common example: every gun is `tacz:modern_kinetic_gun`, so the bare item ID is both too broad to be a useful rule and useless as an icon.

::: info Availability
| Minecraft | First build with groups and displays |
| --- | --- |
| 1.20.1 | 20.1.8 |
| 1.21.1 | 21.1.9 |
| 26.1.2 | 26.1.2.6 |
| 26.2 | 26.2.0.6 |
| 26.3 | 26.3.0.0 |

Older builds reject the `display` and `group` fields and reset the config entry to its default.
:::

## Variant field by version

Anywhere these pages show variant data — inside a rule, inside a `display`, or inside a group member — the field name follows the branch, exactly as in [variant filters](/reference/component-filters):

| Minecraft | Field | Contents |
| --- | --- | --- |
| 1.20.1 | `tag` | Item NBT. `nbt` is accepted as an alias on read; `tag` is what gets written back |
| 1.21.1 and newer | `components` | Serialized data components |

The examples below use `components`. On 1.20.1, substitute `tag` and NBT.

## Custom display

A rule may name a separate stack to draw:

```json
{
  "restrictedSlots": {
    "0": {
      "item": "tacz:modern_kinetic_gun",
      "display": {
        "item": "tacz:modern_kinetic_gun",
        "components": { "tacz:gun_id": "tacz:m4a1" }
      }
    }
  }
}
```

This slot accepts **every** TaCZ gun, because the rule itself carries no variant filter, and draws the M4 rather than the generic base item.

A display may also be written as a bare item ID:

```json
{ "item": "#minecraft:swords", "display": "minecraft:diamond_sword" }
```

Both slots below accept any sword. Slot 0 has no display and cycles through the tag; slot 1 always draws the diamond sword.

<InventoryGrid :slots='{ "0": "#minecraft:swords", "1": { "item": "#minecraft:swords", "display": "minecraft:diamond_sword" } }' />

A display must name a single item. A `#tag` is rejected, because a tag describes a set rather than one stack to draw.

::: warning A display never affects matching
`display` is read only by the renderer. A slot that draws an M4 still accepts a shotgun if its rule allows one. To narrow what the slot takes, put the variant data on the rule itself.
:::

Leave `display` out and the slot renders as it always has: the item's own icon, or a cycle through a tag's members. If the display names an item that is not loaded, the slot falls back to that same default rather than drawing a missing-item placeholder.

::: tip Get the variant data right
Rather than writing component or NBT values from memory, select the real variant in the config screen and middle-click the slot to read back exactly what that stack carries.
:::

## Restriction groups

A group is a named set of entries. A slot restricted to a group accepts an item when **any** entry in the group matches.

Groups are declared in the common config under `groups`:

```json
{
  "groups": {
    "handguns": [
      { "item": "tacz:modern_kinetic_gun", "components": { "tacz:gun_id": "tacz:glock_17" } },
      { "item": "tacz:modern_kinetic_gun", "components": { "tacz:gun_id": "tacz:m1911" } }
    ]
  },
  "restrictedSlots": {
    "0": { "group": "restrictedinventory:handguns" }
  }
}
```

That slot takes a Glock or an M1911 and rejects an M4 or a shotgun.

The same idea works with vanilla variants. This group accepts three healing-type potions, and the slot cycles through them:

```json
{
  "groups": {
    "healing_potions": [
      { "item": "minecraft:potion", "components": { "minecraft:potion_contents": { "potion": "minecraft:healing" } } },
      { "item": "minecraft:potion", "components": { "minecraft:potion_contents": { "potion": "minecraft:strong_healing" } } },
      { "item": "minecraft:potion", "components": { "minecraft:potion_contents": { "potion": "minecraft:regeneration" } } }
    ]
  },
  "restrictedSlots": {
    "8": { "group": "healing_potions" }
  }
}
```

<InventoryGrid :slots='{ "8": { "group": "healing_potions" } }' :groups='{
  "healing_potions": [
    { "item": "minecraft:potion", "components": { "minecraft:potion_contents": { "potion": "minecraft:healing" } } },
    { "item": "minecraft:potion", "components": { "minecraft:potion_contents": { "potion": "minecraft:strong_healing" } } },
    { "item": "minecraft:potion", "components": { "minecraft:potion_contents": { "potion": "minecraft:regeneration" } } }
  ]
}' />

### Group names

A name written without a namespace is read as `restrictedinventory:<name>`, so `handguns` and `restrictedinventory:handguns` are the same group. Two keys that resolve to the same id are reported in the log and only the first is kept.

### Group members

Members use the same forms a slot rule does, minus groups themselves:

| Member | Example | Preview |
| --- | --- | --- |
| Item ID | `"minecraft:diamond_sword"` | <ItemSlot id="minecraft:diamond_sword" /> |
| Item tag | `"#minecraft:swords"` | <ItemSlot id="#minecraft:swords" /> |
| Item with variant data | `{ "item": "minecraft:potion", "components": { ... } }` | <ItemSlot id="minecraft:potion" variant="healing" /> |

A group cannot contain another group, so group resolution can never cycle. A group must contain at least one entry.

### Group displays

The object form of a group takes a group-wide `display`:

```json
{
  "groups": {
    "flashlights": {
      "entries": ["someflashlightmod:flashlight", "anothermod:torchlight"],
      "display": "someflashlightmod:flashlight"
    }
  }
}
```

The icon a group slot draws is chosen in this order:

1. the `display` on the slot rule, if it has one;
2. the group's own `display`;
3. otherwise a cycle through the members' icons.

### Cross-mod groups

Groups do not care which mod an entry comes from. A `flashlights` group can collect flashlight items from several mods that share no item tag.

## Groups or item tags?

Item tags remain the recommended tool for grouping normal, distinct item IDs. A datapack tag such as `#restrictedinventory:handguns` works in a slot rule with no extra configuration, is editable by any datapack, and is shared with the rest of the game.

Use a RestrictedInventory group when a tag cannot express what you need — above all when the members differ only by their NBT or data components, which item tags do not look at.

| Situation | Use |
| --- | --- |
| Several distinct item IDs | Item tag |
| Several variants of one item ID | RestrictedInventory group |
| A mix of both | RestrictedInventory group (tags are valid members) |

## Where groups live

Groups are defined in the common config only, and the server sends them to each connecting client. That keeps one meaning for a group id on both sides, including when `useClientRestriction` is enabled and the rules themselves come from each player's client config.

A rule pointing at a group that does not exist accepts nothing and is reported in the log, so a typo locks the slot rather than silently opening it.

## In the config screen

The picker offers items and tags only; groups are written in the config file. A slot already restricted to a group still previews and saves normally through an editor session — it just has no single variant filter to middle-click. Applying an item or tag to that slot replaces the group rule, as it would any other.
