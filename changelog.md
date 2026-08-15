## 21.1.9
- Restrictions can name a separate `display` item that is used only for the slot icon, so mods like `TaCZ` that put many weapons behind one item id can show the right one instead of the generic base item
- A `display` is render-only and never changes what the slot accepts, so a slot can allow every variant of an item id while still drawing one specific variant
- Added restriction groups: name a set of entries under `groups` in the common config, then restrict a slot with `{"group": "restrictedinventory:handguns"}`. The slot accepts an item when any entry in the group matches
- Group members can be item ids, `#tags`, or item + components entries; a group cannot contain another group
- A group name written without a namespace is read as `restrictedinventory:<name>`
- Groups can carry their own `display`; the slot's display wins, then the group's, otherwise the slot cycles through the group's members
- Groups are defined in the common config and synced to every connecting client, so a group id means the same thing on both sides even with `useClientRestriction` enabled
- Unknown group ids, duplicate group names and malformed `display` entries are now reported in the log with the offending value instead of being ignored
- Existing configs and saved player restrictions keep working unchanged, both new fields are optional
- A slot restricted to a group keeps that rule through a config screen session, the item and tag picker is unchanged

Item tags are still the better tool for grouping normal, distinct item ids. Restriction groups are for
the combinations a tag cannot express, above all several component variants of the same item id.
