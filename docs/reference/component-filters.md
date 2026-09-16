# Variant filters

RestrictedInventory can restrict a slot to an exact item variant. This is useful when the item ID alone is too broad—for example, potions and enchanted books use additional data to distinguish their contents.

| Minecraft | Variant system | Config field |
| --- | --- | --- |
| 1.20.1 | Item NBT | `tag` |
| 1.21.1, 26.1.2, 26.2, 26.3 | Data components | `components` |

Some items that only make sense with a variant filter:

<div class="ri-inline">
  <ItemSlot id="minecraft:potion" variant="healing" label />
  <ItemSlot id="minecraft:potion" variant="strong_swiftness" label />
  <ItemSlot id="minecraft:tipped_arrow" variant="slowness" label />
  <ItemSlot id="minecraft:enchanted_book" variant="mending_1" label />
</div>

## Use the editor

The safest workflow is:

1. Select the desired variant from the **Items** list.
2. Apply it to a slot. The editor carries the creative stack's variant data into the rule.
3. Middle-click the filled slot to inspect or change the NBT/components.
4. Save only after the editor's live validation accepts the data.

The item search includes tooltip text, so queries such as an enchantment or potion name can locate component variants.

## Minecraft 1.21.1 and newer

A component-aware entry uses this form:

```json
{
  "restrictedSlots": {
    "4": {
      "item": "minecraft:potion",
      "components": {
        "minecraft:potion_contents": { "potion": "minecraft:healing" }
      }
    }
  }
}
```

The `components` object uses Minecraft's serialized data-component representation. Exact keys and values depend on the Minecraft version and item. This slot takes Potions of Healing and nothing else: not a Potion of Swiftness, and not a plain water bottle.

<InventoryGrid :slots='{ "4": { "item": "minecraft:potion", "components": { "minecraft:potion_contents": { "potion": "minecraft:healing" } } } }' />

## Minecraft 1.20.1

The 1.20.1 branch stores the same kind of restriction using item NBT under `tag`:

```json
{
  "restrictedSlots": {
    "4": {
      "item": "minecraft:potion",
      "tag": {
        "Potion": "minecraft:healing"
      }
    }
  }
}
```

Prefer selecting a real item variant in the config screen rather than writing either format from memory.

::: info Variant data must match
The configured filter is checked in addition to the item or tag. A stack with the right item but different filtered NBT or component values will not match.
:::

To remove variant matching while retaining the item/tag rule, middle-click the slot and clear the NBT or component object.

The same variant data can also be attached to a rule's `display`, where it selects the icon the slot draws instead of narrowing what the slot accepts. See [groups and displays](/reference/groups).
