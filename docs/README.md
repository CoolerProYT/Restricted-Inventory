# RestrictedInventory wiki

VitePress site for the mod. It covers every supported Minecraft branch, so keep version-specific notes (the availability tables, the `tag`/`components` split) up to date when a branch changes.

```bash
cd docs
npm install
npm run dev       # serves http://localhost:5173/Restricted-Inventory/
npm run build     # builds to .vitepress/dist
npm run preview
```

Pushes to the repository's default branch publish the site through `.github/workflows/docs.yml`.

## Item icons

Pages draw items with two components, registered in `.vitepress/theme/index.ts`:

- `<ItemSlot id="minecraft:torch" label />` draws one slot. A `#tag` id cycles through the members listed in `TAG_MEMBERS` (`.vitepress/theme/restricted.ts`). `variant="healing"` picks a potion, tipped arrow or enchanted book variant (`sharpness_5`). `ghost` dims it like an empty restricted slot.
- `<InventoryGrid :slots='{ "0": "minecraft:torch" }' />` draws the 36-slot player inventory from a `restrictedSlots` map, exactly as written in the config. Pass `:groups` for slots restricted to a group.

Vanilla icons load from the hosted renders at `https://storage.googleapis.com/coolerpromc/textures/`; Mojang's textures are not bundled. Items from other mods, and vanilla items missing there, fall back to initials, except the barrier, which the page draws itself.
