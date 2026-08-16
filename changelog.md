## 26.1.2.7
- Added optional team and tag restrictions: set `useTeamAndTagRestrictions` to `true` in the common config to give different players different rules based on the vanilla scoreboard team they are on and the vanilla entity tags they carry
- `teamRestrictions` maps a scoreboard team name to its own `restrictedSlots`, and `tagRestrictions` does the same for an entity tag, both written in the common config
- Team and tag rules use the exact same entries a normal slot rule does, so item ids, `#tags`, component filters, restriction groups and custom `display` items all work inside them with no extra configuration
- A player matched by a team or tag rule uses that set **instead of** `restrictedSlots`; the global rules are a fallback for players that match nothing, not a layer merged underneath the targeted ones
- A vanilla player is on at most one team, so at most one team rule applies, while every matching tag rule is combined
- Tag rules override team rules when both claim the same slot, and matching tag names are applied in alphabetical order so two tags claiming one slot always resolve the same way
- With `useClientRestriction` enabled, each player's own rules stay the most specific layer and are applied over the team and tag result
- Tags a player carries that have no rule in `tagRestrictions` are ignored, so an unrelated tag from another mod never drops a player out of the global rules
- Membership is managed entirely with vanilla `/team` and `/tag`; no new commands were added and the existing `/restrictedinventory config` still edits the global `restrictedSlots`
- Team and tag changes apply immediately without a relog or a config reload, and nothing is copied into the player's saved data, so leaving a team removes its rules with it
- Configured teams and tags do not have to exist yet; a rule for a team nobody has created is valid and starts applying once a player joins a team with that name
- A profile with an empty `restrictedSlots` still counts as a match, which exempts that team or tag from the global rules entirely
- The server decides which rules apply and sends the result over the existing sync, so slot overlays, ghost previews and placement feedback keep matching what is actually enforced
- Fixed a crash on NeoForge when a `/reload` fired the datapack sync without a player

The feature is off by default. Existing configs, saved player restrictions, restriction entries and
commands keep working unchanged; a config written before this build loads with the three new fields
at their defaults and needs no migration.
