## 26.1.2.5
- Restricted slots can now filter by data components on top of the item or tag
- Config screen item list is now built from the creative tabs, so component variants such as enchanted books, potions and tipped arrows can be picked directly (items that no creative tab lists are still included)
- Search now matches item tooltips and ids, so `sharpness` finds the right enchanted book
- Middle-click a filled slot in the config screen to edit its component filter, with a live preview and validation
- Slot tooltips in the config screen now show the component filter
- Config entries accept `{"item": "minecraft:diamond_sword", "components": {...}}` alongside the plain `"minecraft:diamond_sword"` form, existing configs keep working
