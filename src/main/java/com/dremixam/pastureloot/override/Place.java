package com.dremixam.pastureloot.override;

import java.util.Set;

/** Where a pasture stands: its biome id, the ids of the biome tags (without "#") and its dimension id. */
public record Place(String biomeId, Set<String> biomeTags, String dimensionId) {
}
