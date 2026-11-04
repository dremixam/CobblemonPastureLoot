package com.dremixam.pastureloot.loot;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Decides which items a pastured pokémon is allowed to drop, from item ids and item tags. */
public final class ItemFilter {

    /** Prefix of an item tag in the list, as in "#c:foods/raw_meat". */
    public static final String TAG_PREFIX = "#";

    public enum Mode {
        /** The listed items are never dropped. */
        BLACKLIST,
        /** Only the listed items are dropped. */
        WHITELIST
    }

    private final Set<String> itemIds = new HashSet<>();
    private final Set<String> tags = new HashSet<>();
    private final Mode mode;

    public ItemFilter(Collection<String> entries, Mode mode) {
        this.mode = mode;
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.startsWith(TAG_PREFIX)) {
                tags.add(trimmed.substring(TAG_PREFIX.length()));
            } else if (!trimmed.isEmpty()) {
                itemIds.add(trimmed);
            }
        }
    }

    /** Whether the item may drop, given its id and the ids of the tags it belongs to (without "#"). */
    public boolean allows(String itemId, Collection<String> itemTags) {
        boolean listed = itemIds.contains(itemId) || itemTags.stream().anyMatch(tags::contains);
        return mode == Mode.BLACKLIST ? !listed : listed;
    }
}
