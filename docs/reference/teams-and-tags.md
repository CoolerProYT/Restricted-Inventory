# Teams and tags

Normally every player under a server-wide ruleset gets the same rules. Team and tag restrictions let you hand different rules to different players, using the vanilla scoreboard teams and entity tags you already manage with `/team` and `/tag`.

The feature is **opt-in and off by default**. Nothing changes until you turn it on.

::: info Availability
| Minecraft | First build with team and tag restrictions |
| --- | --- |
| 1.20.1 | 20.1.9 |
| 1.21.1 | 21.1.10 |
| 26.1.2 | 26.1.2.7 |
| 26.2 | 26.2.0.7 |
| 26.3 | 26.3.0.0 |

Older builds ignore the `useTeamAndTagRestrictions`, `teamRestrictions`, and `tagRestrictions` fields and reset those config entries to their defaults.
:::

## Turning it on

```json
{
  "useTeamAndTagRestrictions": true
}
```

| Value | Behavior |
| --- | --- |
| `false` (default) | No team or tag is looked up. Every player uses the rules described in [configuration](/reference/configuration), exactly as before |
| `true` | A player matched by a configured team or tag rule uses **that** ruleset instead of `restrictedSlots`. A player matched by nothing falls back to `restrictedSlots` |

An existing config that has never seen this key loads normally and the key defaults to `false`, so upgrading changes nothing until you edit the file.

## Team restrictions

`teamRestrictions` maps a vanilla scoreboard team name to a set of slot rules:

```json
{
  "useTeamAndTagRestrictions": true,
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
  }
}
```

Membership is managed entirely with vanilla commands:

```text
/team add prisoners
/team join prisoners Steve
```

Steve now uses the `prisoners` rules. Moving him with `/team join guards Steve` switches him to the `guards` rules; `/team leave Steve` drops him back to the global `restrictedSlots`.

A vanilla player belongs to at most one team, so at most one team rule can ever apply.

## Tag restrictions

`tagRestrictions` maps a vanilla entity tag to a set of slot rules:

```json
{
  "useTeamAndTagRestrictions": true,
  "tagRestrictions": {
    "no_weapons": {
      "restrictedSlots": {
        "0": "minecraft:barrier",
        "1": "minecraft:barrier"
      }
    },
    "prisoner": {
      "restrictedSlots": {
        "20": "minecraft:barrier"
      }
    }
  }
}
```

```text
/tag Steve add no_weapons
/tag Steve remove no_weapons
```

A player can carry many tags, and **every** matching tag rule is applied.

::: tip Unrelated tags are harmless
Tags with no rule in `tagRestrictions` are skipped. A player carrying `afk` and some other mod's internal tag matches nothing, so they keep the global `restrictedSlots` — an unrelated tag never drops a player out of the shared ruleset.
:::

## Rule contents

A profile's `restrictedSlots` is the same map the global `restrictedSlots` is, so every rule form works inside a team or tag profile with no extra configuration:

| Form | Example |
| --- | --- |
| Item ID | `"minecraft:diamond_sword"` |
| Item tag | `"#minecraft:swords"` |
| Item with [variant data](/reference/component-filters) | `{ "item": "...", "components": { ... } }` |
| Custom [display](/reference/groups) | `{ "item": "...", "display": "..." }` |
| [Restriction group](/reference/groups) | `{ "group": "restrictedinventory:handguns" }` |

Slot keys follow the usual numbering: `0`–`8` for the hotbar, `9`–`35` for the main inventory. An index outside that range is rejected and resets the entry, the same as in the global map.

## Precedence

When more than one source claims the same slot, the most specific wins:

```text
player's own rules  >  tag  >  team  >  global fallback
```

- A tag rule overrides a team rule on the same slot. A player on `prisoners` (slot 9 → <ItemSlot id="minecraft:barrier" />) who also carries `special_weapon` (slot 9 → <ItemSlot id="minecraft:diamond_sword" />) ends up with the diamond sword.
- Slots that only one source claims are simply combined. A `prisoners` team rule on slot 20 plus a `no_weapons` tag rule on slot 0 gives the player both.
- "Player's own rules" exist only in per-player mode (`useClientRestriction: true`), where each player supplies their own set from their client config. Those entries stay the most specific layer and are laid over the team and tag result. Under a shared ruleset there is nothing player-owned to layer, so a matching team or tag rule simply replaces the shared set.

### Two tags on one slot

Matching tag names are sorted alphabetically and applied in that order, so the alphabetically later name wins a conflict. `no_weapons` and `prisoner` both claiming slot 20 always resolve to `prisoner`, on every server and after every restart. The same player with the same tags and the same config always produces the same result.

::: warning The global rules are a fallback, not a base layer
Once any team or tag rule matches, `restrictedSlots` is not part of the result at all — it is not merged underneath. Repeat any slot you still want in each profile that needs it.
:::

## Worked example

```json
{
  "useTeamAndTagRestrictions": true,
  "restrictedSlots": { "9": "minecraft:bread", "10": "minecraft:torch" },
  "teamRestrictions": { "prisoners": { "restrictedSlots": { "20": "minecraft:barrier" } } },
  "tagRestrictions":  { "no_weapons": { "restrictedSlots": { "0": "minecraft:barrier" } } }
}
```

| Steve's state | Effective rules |
| --- | --- |
| No configured team, no configured tag | slot 9 <ItemSlot id="minecraft:bread" /> slot 10 <ItemSlot id="minecraft:torch" /> |
| On `prisoners` | slot 20 <ItemSlot id="minecraft:barrier" /> Slots 9 and 10 are **not** restricted |
| On `prisoners`, tagged `no_weapons` | slot 0 <ItemSlot id="minecraft:barrier" /> slot 20 <ItemSlot id="minecraft:barrier" /> |
| Tagged `afk` only | slot 9 <ItemSlot id="minecraft:bread" /> slot 10 <ItemSlot id="minecraft:torch" /> |

Steve on `prisoners` and tagged `no_weapons` sees this. The global bread and torch slots are gone:

<InventoryGrid title="Steve's inventory" :slots='{ "0": "minecraft:barrier", "20": "minecraft:barrier" }' />

## Empty profiles

A profile with an empty `restrictedSlots` still counts as a match, which is how you exempt a group of players entirely:

```json
{
  "teamRestrictions": {
    "staff": { "restrictedSlots": {} }
  }
}
```

Everyone on `staff` has no restricted slots at all, while everyone else keeps the global ruleset.

## Missing teams and tags

Configured names are never checked against the current scoreboard when the config loads. A rule for a team or tag that nobody has created is valid and simply inert:

```json
{
  "teamRestrictions": { "boss_fight": { "restrictedSlots": { "0": "minecraft:barrier" } } }
}
```

This loads without an error before anyone runs `/team add boss_fight`, and starts applying the moment a player joins a team with that name. The same holds for a tag nobody currently carries.

## Live changes

Team and tag membership is re-read from the player's current state, so `/team join`, `/team leave`, `/tag add`, and `/tag remove` take effect immediately — no relog, no `/reload`, no config edit.

Nothing is written into the player's saved data. The rules stay attached to the team or tag, which means removing a player from a team removes those rules with it rather than leaving a copy behind on the player.

## Commands

RestrictedInventory adds no commands for this. Membership is vanilla's job:

| Task | Command |
| --- | --- |
| Create a team | `/team add <team>` |
| Put a player on a team | `/team join <team> <player>` |
| Take a player off their team | `/team leave <player>` |
| Give a player a tag | `/tag <player> add <tag>` |
| Take a tag away | `/tag <player> remove <tag>` |

`/restrictedinventory config` is unchanged and still edits the global `restrictedSlots`, not a team or tag profile. Team and tag profiles are written in the config file. See [commands and permissions](/reference/commands).

## Multiplayer

The server resolves which rules apply and sends the result to the client over the channel that already carries restrictions. Clients never inspect teams or tags themselves, so ghost previews, placement feedback, and slot overlays show what the server is actually enforcing.
