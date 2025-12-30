[![Modrinth Downloads](https://img.shields.io/modrinth/dt/cobblemon-pasture-loot)](https://modrinth.com/mod/cobblemon-pasture-loot)
![GitHub Release Date](https://img.shields.io/github/release-date/dremixam/CobblemonPastureLoot)
[![Discord](https://discordapp.com/api/guilds/745755550180048906/widget.png?style=shield)](https://discord.dremixam.com)
[![Twitch Status](https://img.shields.io/twitch/status/dremixam)](https://twitch.tv/dremixam)

# CobblemonPastureLoot

> Want to abandon the hunter-gatherer lifestyle and have your pastured pokémon do the work for you? This mod is for you!

Cobblemon mod to make pastured pokémon randomly drop items from their loot table.
The items are dropped at the pokémon's location.

Currently available for Cobblemon 1.7.1 on Fabric for Minecraft 1.21.1

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
