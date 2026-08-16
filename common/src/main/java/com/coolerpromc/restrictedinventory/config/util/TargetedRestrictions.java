package com.coolerpromc.restrictedinventory.config.util;

import java.util.Map;

/**
 * The restrictions a player gets from the vanilla team and vanilla tags they currently have.
 *
 * <p>The server resolves this from live player state and stores it on the player, then sends it with
 * {@code ClientBoundTargetedRestrictionsSyncPacket} — the client never inspects teams or tags itself.
 * It is deliberately not persisted: it is recomputed every server tick, so a {@code /team join} or
 * {@code /tag remove} can never leave a stale rule behind.
 *
 * @param matched     whether any configured team or tag rule applied to the player at all. It is
 *                    kept separate from {@code restrictions} so that a profile that deliberately
 *                    restricts nothing still replaces the global set instead of falling back to it.
 * @param restrictions the effective targeted restrictions, empty when nothing matched
 */
public record TargetedRestrictions(boolean matched, Map<Integer, Restriction> restrictions) {
    public static final TargetedRestrictions NONE = new TargetedRestrictions(false, Map.of());
}
