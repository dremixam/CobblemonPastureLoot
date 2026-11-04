package com.dremixam.pastureloot.loot;

import java.util.Collection;
import java.util.Set;

/** Decides which item ids a pastured pokémon is allowed to drop. */
public final class ItemFilter {

    private final Set<String> blacklist;

    public ItemFilter(Collection<String> blacklist) {
        this.blacklist = Set.copyOf(blacklist);
    }

    public boolean allows(String itemId) {
        return !blacklist.contains(itemId);
    }
}
