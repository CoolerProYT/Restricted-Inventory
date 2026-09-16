# Compatibility

RestrictedInventory integrates with the standard Minecraft inventory paths. It supports Fabric across all maintained branches, Forge on Minecraft 1.20.1, and NeoForge on Minecraft 1.21.1 and newer.

## Expected to work

Normal clicks, shift-moving behavior that validates destination slots, and pickups routed through vanilla player inventory methods respect restrictions.

## Known limitations

- **Direct insertion:** a mod that writes straight to an inventory index can bypass normal slot validation.
- **Custom inventories:** replacements or wrappers that do not use the vanilla player `Inventory` behavior may not be intercepted.
- **Mixin conflicts:** mods patching `Slot`, `Inventory`, or `AbstractContainerScreen` at the same locations can change or disable expected behavior.
- **Unlisted slots:** armor, offhand, and mod-added equipment slots are outside the supported 0–35 player inventory range.

## Reporting a conflict

Include the following when reporting an incompatibility:

1. Minecraft, RestrictedInventory, loader, and dependency versions.
2. The other mod's name and version.
3. Whether the failure happens during manual placement, pickup routing, or rendering.
4. A minimal mod list and the relevant game log.
5. Exact steps that reproduce the behavior.
