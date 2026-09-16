# Commands and permissions

RestrictedInventory provides one command:

```text
/restrictedinventory config
```

It opens the in-game restriction editor on the client.

## Permission behavior

| Mode | Who can run the command |
| --- | --- |
| Server-wide (`useClientRestriction: false`) | Administrators with permission level 4 |
| Per-player (`useClientRestriction: true`) | Every player |

The server updates command permissions when the common configuration reloads, so changing modes does not require manually re-granting access.

If the command is unavailable, verify the server's `useClientRestriction` value and your operator permission level.

## Vanilla commands

RestrictedInventory adds no command for assigning rules to groups of players. When [teams and tags](/reference/teams-and-tags) are enabled, membership is managed with vanilla's own `/team` and `/tag`, and the mod reads the result:

```text
/team join prisoners Steve
/tag Steve add no_weapons
```
