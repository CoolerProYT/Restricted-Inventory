## 20.1.7
- Restricted slots can now filter by item NBT on top of the item or tag
- Config screen item list is now built from the creative tabs, so NBT variants such as enchanted books, potions and tipped arrows can be picked directly (items that no creative tab lists are still included)
- Search now matches item tooltips and ids, so `sharpness` finds the right enchanted book
- Middle-click a filled slot in the config screen to edit its NBT filter, with a live preview and validation
- Slot tooltips in the config screen now show the NBT filter
- Config entries accept `{"item": "minecraft:diamond_sword", "tag": {...}}` alongside the plain `"minecraft:diamond_sword"` form, existing configs keep working
- Fixed being unable to join a world whose player data was saved by an older version, restrictions saved on players are now read in both the old and new format
- Fixed the ghost item in restricted slots rendering fully opaque for block items
- Fixed the config screen item list duplicating itself every time the window is resized
- Updated `CoolerConfig` to 20.1.3

### Fabric
- `Forgotten Graves` compatibility now respects NBT filters
