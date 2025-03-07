# CobblemonPastureLoot
 
> Want to abandon the hunter-gatherer lifestyle and have your pastured pokémon do the work for you? This mod is for you!

Cobblemon mod to make pastured pokémon randomly drop items from their loot table.
The items are dropped at the pokémon's location.

Currently only available for Cobblemon 1.6.1 on Fabric for Minecraft 1.21.1

## Settings

Edit the `config/PastureLoot.json` file to change the settings.

- `tick_per_minute`: The number of ticks per minute. Should stay at 1200.
- `drop_chance_per_minute`: The chance for each pokémon dropping an item each minute.
- `item_blacklist`: A list of items that should not be dropped by the pokémons.

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
  ]
}

```
