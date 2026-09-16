---
layout: home

hero:
  name: RestrictedInventory
  text: Every slot has a purpose.
  tagline: Lock player inventory slots to an item, tag, or exact component variant while keeping Minecraft's normal inventory behavior. For Fabric, NeoForge and Forge.
  image:
    src: /logo.png
    alt: RestrictedInventory
  actions:
    - theme: brand
      text: Get started
      link: /guide/getting-started
    - theme: alt
      text: Configuration reference
      link: /reference/configuration

features:
  - icon: '🔒'
    title: Slot-level control
    details: Restrict any hotbar or main-inventory slot (0–35) while leaving every other slot untouched.
  - icon: '#'
    title: Items and tags
    details: Match one registered item or an entire item tag, with a cycling ghost preview in-game.
  - icon: '🧪'
    title: Variant-aware
    details: Pin a slot to exact variants such as an enchanted book, potion, or tipped arrow.
  - icon: '🗂️'
    title: Groups and displays
    details: Accept any of several variant-specific entries at once, and choose the icon a slot draws.
  - icon: '👥'
    title: Teams and tags
    details: Give different players different rules through vanilla scoreboard teams and entity tags.
  - icon: '🎒'
    title: Smart pickups
    details: Picked-up and shift-clicked items go to their reserved slot first, then fall back to the rest of the inventory.
---

<div class="vp-doc ri-home-section">

## A hotbar that stays organized

Reserve slots once and every pickup lands where it belongs. Empty restricted slots show a dimmed preview of what goes there.

<InventoryGrid title="Example loadout" :slots='{
  "0": "#minecraft:swords",
  "1": "minecraft:bow",
  "2": "#minecraft:arrows",
  "3": "minecraft:ender_pearl",
  "4": { "item": "minecraft:potion", "components": { "minecraft:potion_contents": { "potion": "minecraft:healing" } } },
  "7": "minecraft:cooked_beef",
  "8": "minecraft:totem_of_undying",
  "9": "minecraft:torch",
  "17": "minecraft:water_bucket"
}' />

</div>
