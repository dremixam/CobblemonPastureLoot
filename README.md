# Cobblemon Pasture Loot

> Want to abandon the hunter-gatherer lifestyle, embrace the farmer life and have your pastured pokémon do the work for you? This mod is for you!

Cobblemon Pasture Loot makes the pokémon in a [Cobblemon](https://modrinth.com/mod/cobblemon) pasture drop items from time to time. The items come from the drop table of the pokémon, the same one Cobblemon uses when you defeat a wild pokémon, and they fall where the pokémon stands. Put some hoppers on the ground and you have a farm.

Items a pokémon can only give by dying (meat, fish, leather, bones...) are not dropped by default.

## How it works

- Each pokémon in a pasture drops **3 times per hour** on average by default (as long as the chunk is loaded).
- One drop is one roll of the drop table of the pokémon, so it can give several items at once, with the quantities and chances Cobblemon defines.
- **Regional forms and other forms are supported**: an Alolan Vulpix drops what an Alolan Vulpix drops, a roaming Gimmighoul drops one coin and not a chest worth of them.
- **The place matters**: some pokémon have special drops when they spawn in the Nether or in the End (Gastly and its ghast tears, Elgyem and its chorus fruits...). The mod gives these drops when the **pasture** stands in the same place. A Gastly in a Nether pasture drops ghast tears, the same Gastly in the Overworld does not.
- **Friendship matters**: the drop rate grows with the friendship of the pokémon, up to twice the rate at maximum friendship by default.
- To protect your server, a pasture stops dropping while too many items lie around it.

## Configuration

The settings are in `config/pastureloot.toml`. The file is created on the first start, with a comment on every setting.

- **`drops.per_hour`** (`3.0`): average number of drops per hour, for each pokémon. `0` disables drops, the maximum is `3600` (one drop per second).
- **`filter.mode`** (`"blacklist"`): with `"blacklist"`, the items of the list are never dropped. With `"whitelist"`, only the items of the list are dropped.
- **`filter.items`**: item ids (`"minecraft:bone"`) or item tags with a `#` (`"#c:foods/raw_meat"`). By default it contains the items a pokémon gives by dying, plus honey bottles and white wool that Cobblemon already lets you harvest on your pokémon.
- **`friendship.max_bonus`** (`1.0`): extra drop rate at maximum friendship. `1.0` means twice the rate at friendship 255, `0` disables the bonus.
- **`anti_lag.max_items_nearby`** (`32`): a pasture stops dropping while this many item entities or more lie in the area where its pokémon wander. `0` disables the limit. The size of the area comes from the Cobblemon config (`pastureMaxWanderDistance`).

A missing setting takes its default value. A wrong value is replaced by its default or its limit, with a warning in the log.

## Loot table overrides

To change what a pokémon drops, use `config/pastureloot-overrides.toml`. The file is created with explanations and examples, and without any active rule.

Each `[[override]]` block is one rule. Every rule that matches a pokémon is used, **in the order of the file**: `replace` starts again from nothing, `add` adds drops, `remove` takes items out of what the rules above it gave.

```toml
# Alolan Vulpix also drops ice
[[override]]
pokemon = "vulpix alolan"
drops = [{ item = "minecraft:ice", quantity = "1-2", chance = 10 }]

# Gastly drops soul sand, but only in a Nether pasture
[[override]]
pokemon = "gastly"
biomes = ["#minecraft:is_nether"]
drops = [{ item = "minecraft:soul_sand", chance = 5 }]

# Mareep only drops wool
[[override]]
pokemon = "mareep"
mode = "replace"
drops = [{ item = "minecraft:white_wool", quantity = "1-2" }]

# Farfetch'd keeps its feathers
[[override]]
pokemon = "farfetchd"
mode = "remove"
items = ["minecraft:feather"]

# Mewtwo does not drop anything in a pasture
[[override]]
pokemon = "mewtwo"
rate_multiplier = 0
```

- **`pokemon`** (required): pokémon properties, like in `/pokegive`: `"vulpix"`, `"vulpix alolan"`, `"shiny=true"`, `"type=fire"`...
- **`mode`**: `"add"` (default), `"replace"` or `"remove"`.
- **`drops`**: for `add` and `replace`, a list of `{ item, quantity, chance }`. `quantity` is a number or a range (`"1-3"`), 1 by default. `chance` is in percent, 100 by default. Each drop is rolled on its own.
- **`items`**: for `remove`, item ids or item tags.
- **`rate_multiplier`**: multiplies the drop rate of this pokémon, 1 by default.
- **`biomes`** and **`dimensions`**: optional, the pasture must stand in one of them. Biomes accept ids and tags.

The filter of `pastureloot.toml` only applies to the drops of Cobblemon, not to the drops you add in this file. A broken rule is skipped with a warning in the log that tells which rule it is.

## Commands

These commands are for operators only.

- **`/pastureloot reload`**: reads both files again, without restarting. The warnings are shown in the chat.
- **`/pastureloot preview <properties>`**: shows what a pokémon would drop in a pasture where you stand: its rate, the Cobblemon table, what the filter and the overrides remove, and what the overrides add. Example: `/pastureloot preview vulpix alolan`.
- **`/pastureloot inspect <entity>`**: the same for a pokémon of the world, with its real friendship and the place of its pasture. Example: `/pastureloot inspect @e[type=cobblemon:pokemon,limit=1,sort=nearest]`.

## Supported versions & compatibility

Supports **Cobblemon 1.8** for **Minecraft 1.21.1**, on **Fabric** and on **NeoForge**. For Cobblemon 1.7, use the 1.0.x versions of the mod, they will not receive new updates.

### Updating from 1.0.x

The old `config/PastureLoot.json` is converted to `config/pastureloot.toml` on the first start, then renamed `PastureLoot.json.old`. Your drop rate and your blacklist are kept.

Some things work differently in 2.0:

- The drop chance per minute became a number of drops per hour: `0.05` per minute is `3` per hour.
- A drop now gives every item rolled from the drop table, where 1.0.x picked only one of them. With the same rate, pokémon drop a bit more than before.
- The `legacy_flatten_item_quantity` setting is gone, quantities always follow Cobblemon.

## Support

If you encounter any issue, open a [new issue on GitHub](https://github.com/dremixam/CobblemonPastureLoot/issues/new) with as many details as possible, or get support on [Discord](https://discord.gg/kbNXRSQR94). `/pastureloot preview` and `/pastureloot inspect` are a good start to explain what you see.

## License

The mod is under the [GNU Affero General Public License v3.0](LICENSE.md).

Please do not repost the mod on CurseForge.

---

## My other mods

![DrEmixam minecraft mods](https://cdn.modrinth.com/data/cached_images/b4351711698522dd4ab28de1745a180b1b22a21b.png)

| Project | Description |
| :---: | :--- |
| <a href="https://modrinth.com/mod/create-transcontinental"><img src="https://cdn.modrinth.com/data/7jTxKiJC/333ade865a26fd715f0aea07e5c85630b70f7832_96.webp" alt="Create: Transcontinental"></a> | **[Create: Transcontinental](https://modrinth.com/mod/create-transcontinental)**<br>_Generates a performant, deterministic Create railway network during world generation, connecting regions across continents with terrain-adaptive tracks._ |
| <a href="https://modrinth.com/mod/create-immediate-departure"><img src="https://cdn.modrinth.com/data/ouOYlNGh/2a964e0926a05c88924245fbe595032c37faba61_96.webp" alt="Create: Immediate Departure"></a> | **[Create: Immediate Departure](https://modrinth.com/mod/create-immediate-departure)**<br>_Fast-travel between Create train stations, once you've discovered them and connected your rail network._ |
| <a href="https://modrinth.com/mod/XP2jcAo0"><img src="https://cdn.modrinth.com/data/XP2jcAo0/f770de201522a55950b47d62ec0c4bef484afa6d_96.webp" alt="Cobblemon Pasture Loot"></a> | **[Cobblemon Pasture Loot](https://modrinth.com/mod/XP2jcAo0)**<br>_Want to abandon the hunter-gatherer lifestyle, embrace the farmer life and have your pastured pokémon do the work for you? This mod is for you!_ |
| <a href="https://modrinth.com/mod/leonlerobot"><img src="https://cdn.modrinth.com/data/b0KkkJDp/a70169bad1e156743e1f5dfeed55ff175130a6dd_96.webp" alt="Leon Le Robot"></a> | **[Leon Le Robot](https://modrinth.com/mod/leonlerobot)**<br>_Let the french Twitch streamer robot Leon Le Robot land in your Minecraft server._ |
| <a href="https://modrinth.com/mod/pOTfVxmX"><img src="https://cdn.modrinth.com/data/pOTfVxmX/d932f6371312f6afc912be91463869e2d11eff5c_96.webp" alt="Community Leaders"></a> | **[Community Leaders](https://modrinth.com/mod/pOTfVxmX)**<br>_A mod designed to help server admins delegate community management to trusted "community leaders". Server admins can grant invitation permissions to streamers, content creators, or community managers…_ |
| <a href="https://modrinth.com/mod/server-rules"><img src="https://cdn.modrinth.com/data/1tSdJG3n/e564d703abc38c152fc7986b828624639b8e52fa.png" alt="Server Rules"></a> | **[Server Rules](https://modrinth.com/mod/server-rules)**<br>_Mod designed to enforce server rules by displaying a mandatory rules screen to new players. Players must read and accept the rules before they can join the game, ensuring a friendly and respectful community environment._ |

### Streams and other creations

I stream on [Twitch](https://www.twitch.tv/dremixam), in French. You can find my other creations on [GitHub](https://github.com/dremixam) and [my personal website](https://dremixam.com/).
