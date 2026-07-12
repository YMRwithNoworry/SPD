# SPD

> A Minecraft 1.20.1 adventure mod about abyssal corruption, hostile ecology, and Blazing Vein forging. Available for Forge and Fabric.

SPD transforms parts of the Overworld into dangerous regions shaped by Abyssal Stone, fungal corruption, and dark blood-colored water. Players explore these areas for rare materials, manage a growing corruption threat, construct processing equipment, and forge weapons made to fight infected creatures.

The project is currently versioned as `1.0-SNAPSHOT`. Exact values, drops, and balance can change during development. In-game tooltips and JEI recipes are the authoritative source for the installed build.

## Gameplay Loop

1. Explore the Overworld for Blazing Vein Stone, Blood Ash Ore, and abyssal terrain.
2. Process Blazing Raw Ore in a blast furnace to obtain Blazing Shards.
3. Use the Abyssal Heart Forge to turn Blazing Shards and fuel into Blazing Carbon Steel Ingots.
4. Combine steel, Ember Handles, and infection materials to craft tools, melee weapons, and functional blocks.
5. Survive Abyssal Pressure while fighting infected creatures and enduring special weather events.

## Abyssal Regions

SPD adds four abyssal biomes that can be found with `/locate biome`:

| Biome | Identity |
| --- | --- |
| Abyssal Blood Desert | A broad desert made from Abyssal Blood Sand, dead wood, Blazing Vein resources, and infection-themed spawns. |
| Abyssal Coast | A coast surrounded by dark blood-colored water and an important habitat for Abyssal Turtles. |
| Fungal Shallows | A shallow-water biome with corrupted visual treatment and dangerous aquatic exploration. |
| Chrome Seabed Caves | Underwater caves with greatly increased Blazing Vein ore generation. |

Important environmental blocks include Abyssal Stone, Abyssal Blood Sand, Vine Plague Nodes, Widespread Epidemic, and Abyssal Turtle Eggs. Blood Sand impedes creatures outside the SPD faction, while corrupted areas continuously create environmental pressure.

### Abyssal Pressure

**Abyssal Pressure** is SPD's stack-based corruption effect. Abyssal environments, infected attacks, and spore clouds can increase its level. Leaving corruption sources allows the effect to decay over time.

- Higher levels reduce combat and movement capability and deal ongoing corrosion damage.
- At maximum pressure, some vanilla foxes, wolves, and zombies can transform into their corresponding SPD variants.
- SPD faction creatures are immune to Abyssal Pressure.
- Purification mechanics and resistance equipment provide ways to reduce the danger.

## Infected Ecology

| Creature | Role |
| --- | --- |
| Mold Zombie | A hostile fungal undead. Some variants leap at nearby targets. |
| Abyssal Eroded Silverfish | A fast, small hostile creature that attacks non-SPD mobs. |
| Abyssal Fox | A neutral-to-hostile predator that can dash, call nearby allies, and steal items. |
| Abyssal Wolf | A pack hunter with howls, pounces, and rending attacks. |
| Abyssal Turtle | A heavily armored coastal creature with spore defense, shell guarding, and underwater charges. |
| False Mother | A high-health infection entity that spreads Widespread Epidemic and creates local threats. |

These creatures provide materials such as Fungal Residue, Chrome Dust, Abyssal Heart Spores, Corrupted Sand Scale Hide, and Etched Turtle Scutes. SPD creatures can also drop Liquid Gold; a player must carry Culture Medium to collect it.

## Blazing Vein Forging

The primary progression path is:

```text
Blazing Vein Stone -> Blazing Raw Ore -> Blazing Shard -> Blazing Carbon Steel Ingot -> Equipment
```

- **Blazing Vein Stone** is a key Overworld and Chrome Seabed Caves resource.
- **Blood Ash Ore** can be smelted into Blood Ash Ingots and crafted into Ember Handles.
- **Abyssal Heart Forge** is the central machine for making Blazing Carbon Steel Ingots and can yield bonus byproducts.
- **Crucible Walls and Molten Chrome Nozzles** are used to construct and operate a multiblock crucible.
- **Culture Medium** is crafted from glass, sugar, and a water bottle, and is required for handling Liquid Gold.
- **Abyssal Blazing Rune Stele** is a two-block-tall functional structure crafted from four obsidian around an amethyst shard.

## Weapons and Tools

SPD includes a Blazing Ember tool set and several melee weapons with different combat rhythms:

- **Blazing Vein Greatsword** builds heat stacks and rewards sustained combat with a close-range burst.
- **Blazing Vein Piercing Spear** can be charged with right-click and released as a long linear strike.
- **Blazing Vein Dagger** builds Swift Edge through quick attacks; a full stack triggers an additional instant slash.
- **Nameless Sword** has a charged abyss-slaying attack and is especially effective against abyssal enemies.

Weapons can apply or interact with effects such as Searing Pulse, Rending, and Erosion Suppression, creating counterplay around Abyssal Pressure and enemy defenses.

## Abyssal Gloom

The Overworld can experience **Abyssal Gloom**, a special weather event that pushes exposed creatures with wind, increases pressure on players under open skies, and can create Abyssal Tornadoes.

Administrators can control it with:

```text
/spd weather abyssal_gloom
/spd weather abyssal_gloom true
/spd weather abyssal_gloom false
/spd weather abyssal_gloom tornado <duration_seconds>
```

SPD also has an independent world difficulty system. Use `/spd difficulty` or `/dif world` to view or change it.

## Installation

| Component | Requirement |
| --- | --- |
| Minecraft | `1.20.1` |
| Java | `21` or later |
| Loader | Forge `47.4.x` or Fabric Loader `0.19.3+` |
| External dependencies | Architectury API, GeckoLib, TerraBlender, and Fabric API on Fabric. |

The current Forge and Fabric builds embed **Photon** and **LDLib2** through Jar-in-Jar. Installing the SPD release JAR normally does not require separate copies of these two dependencies unless another mod needs them independently.

## Development Build

The project uses Architectury. Shared gameplay code is in `common/`; loader entrypoints are in `fabric/` and `forge/`.

```powershell
gradle :forge:build --console=plain
gradle :fabric:build --console=plain
```

Build outputs are written to the relevant module's `build/libs/` directory.

## License

All Rights Reserved.
