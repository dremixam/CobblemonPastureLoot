[![Modrinth Downloads](https://img.shields.io/modrinth/dt/cobblemon-pasture-loot)](https://modrinth.com/mod/cobblemon-pasture-loot)
![GitHub Release Date](https://img.shields.io/github/release-date/dremixam/CobblemonPastureLoot)
[![Discord](https://discordapp.com/api/guilds/745755550180048906/widget.png?style=shield)](https://discord.dremixam.com)
[![Twitch Status](https://img.shields.io/twitch/status/dremixam)](https://twitch.tv/dremixam)

# CobblemonPastureLoot

> Want to abandon the hunter-gatherer lifestyle and have your pastured pokémon do the work for you? This mod is for you!

Cobblemon mod to make pastured pokémon randomly drop items from their loot table.
The items are dropped at the pokémon's location.

Currently available for Cobblemon 1.7.1 on Fabric for Minecraft 1.21.1

## How it works

### Drop triggering

Each tick (~50 ms, 1 200 ticks/minute), every tethered Pokémon independently rolls a random number.
The probability used per tick is derived from `drop_chance_per_minute` so that the *per-minute* probability is correct
regardless of how many ticks there are in a minute:

```
dropChancePerTick = 1 − (1 − drop_chance_per_minute) ^ (1 / tick_per_minute)
```

With the default value of `0.05` this translates to roughly a **5% chance every minute** per Pokémon.

The check is skipped when the Pokémon is fainted or when its entity is not currently loaded into the world.

### Item selection

When the tick check succeeds, the mod queries Cobblemon's own **drop table** for the Pokémon's current form
(form-aware, so e.g. Hisuian Voltorb and regular Voltorb use different tables).

Internally this calls `DropTable.getDrops()` with the table's configured roll amount, which already applies
Cobblemon's per-entry percentage constraints.  One `DropEntry` is then picked at random from the resulting list.
Only `ItemDropEntry` instances produce an actual item; other entry types and blacklisted items are silently ignored.

### Item quantity — `legacy_flatten_item_quantity`

| Mode | Behaviour | When to use |
|------|-----------|-------------|
| `false` *(default)* | Delegates to `ItemDropEntry.drop()` — quantity is drawn from Cobblemon's `quantityRange` for that entry (e.g. Gimmighoul can drop 24–48 Relic Coins in one event) | Keeps Cobblemon's intended drop quantities |
| `true` *(legacy)* | Forces quantity = 1 for every drop, ignoring `quantityRange` | Flatter economy / balance tuning |

### Comparison with vanilla Cobblemon

| | Vanilla Cobblemon | CobblemonPastureLoot |
|-|-------------------|----------------------|
| **Trigger** | Pokémon is defeated in battle | Passive — time-based tick while in pasture |
| **Frequency** | Once per defeat | ~5 % chance per minute per Pokémon (configurable) |
| **Drop table used** | Cobblemon's native form-aware table | Same — `FormData.getDrops()` |
| **Quantity** | Full `quantityRange` support | Configurable: native range *or* flattened to 1 |
| **Form awareness** | ✔ | ✔ |
| **Pokémon must be alive** | N/A (just defeated) | ✔ — fainted Pokémon do not drop |

The key difference is that this mod turns the standard *kill loot* system into a **passive income** mechanism for living, pastured Pokémon, while reusing Cobblemon's drop-table infrastructure entirely.

---

## Settings

Edit the `config/PastureLoot.json` file to change the settings.

- `tick_per_minute`: The number of ticks per minute. Should stay at 1200.
- `drop_chance_per_minute`: The chance for each pokémon dropping an item each minute.
- `item_blacklist`: A list of items that should not be dropped by the pokémons.
- `legacy_flatten_item_quantity`: False (default) for using Cobblemon loot table logic, true to have all items on the
  loot table have their quantities flattened to one (legacy behavior).

```json
{
  "tick_per_minute": 1200,
  "drop_chance_per_minute": 0.015,
  "item_blacklist": [
    "minecraft:porkchop",
    "minecraft:beef",
    "minecraft:chicken",
    "minecraft:mutton",
    "minecraft:rabbit",
    "minecraft:fish",
    "minecraft:cooked_porkchop",
    "minecraft:cooked_beef",
    "minecraft:cooked_chicken",
    "minecraft:cooked_mutton",
    "minecraft:cooked_rabbit",
    "minecraft:cooked_fish",
    "minecraft:leather",
    "minecraft:bone",
    "minecraft:spider_eye",
    "minecraft:rotten_flesh",
    "minecraft:rabbit_hide",
    "minecraft:rabbit_foot",
    "minecraft:cod",
    "minecraft:pufferfish",
    "minecraft:bone_block",
    "minecraft:bone_meal",
    "cobblemon:sharp_beak",
    "minecraft:honey_bottle",
    "minecraft:salmon",
    "minecraft:white_wool"
  ],
  "legacy_flatten_item_quantity": false
}

```
