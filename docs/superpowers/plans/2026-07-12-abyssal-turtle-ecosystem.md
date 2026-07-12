# Abyssal Turtle Ecosystem Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the locateable abyssal coast ecosystem, a fully animated neutral abyssal turtle, hatchable abyssal turtle eggs, infection, combat abilities, and associated drops and resources.

**Architecture:** Keep gameplay and data resources in `common`, extending the existing TerraBlender region and surface-rule integration. Isolate deterministic cooldown, infection, and state-selection calculations in small helpers so they can be tested without constructing a Minecraft world; keep loader modules limited to attributes, spawn placement, biome additions, and renderer registration.

**Tech Stack:** Java 17, Architectury, Minecraft 1.20.1, TerraBlender 3.0.1.10, GeckoLib 4, Fabric API, Forge biome modifiers, JUnit 5, Gson.

---

## File Map

- `common/src/main/java/alku/spd/world/AbyssalCoastRegion.java`: TerraBlender climate mapping for coast, shallows, and underground caves.
- `common/src/main/java/alku/spd/world/SpdTerraBlender.java`: region registration and biome-specific surface rules.
- `common/src/main/java/alku/spd/entity/AbyssalTurtleEntity.java`: turtle AI, combat state, water/land adaptation, egg laying, and GeckoLib controller.
- `common/src/main/java/alku/spd/entity/AbyssalTurtleMechanics.java`: pure calculations for damage, cooldowns, armor, infection, and animation state.
- `common/src/main/java/alku/spd/block/AbyssalTurtleEggBlock.java`: stacking, hatching, trampling, and nearby-adult anger.
- `common/src/main/java/alku/spd/world/AbyssalTurtleInfectionEvents.java`: persistent vanilla turtle infection and conversion.
- `common/src/main/java/alku/spd/world/AbyssalTurtleInfectionCarrier.java`: loader-independent access to persistent infection progress.
- `common/src/main/java/alku/spd/mixin/TurtleMixin.java`: stores infection progress in turtle NBT.
- `common/src/main/java/alku/spd/effect/SporeSluggishnessEffect.java`: 15% attack-speed penalty.
- `common/src/main/java/alku/spd/client/model/AbyssalTurtleModel.java`: GeckoLib resource paths.
- `common/src/main/java/alku/spd/client/renderer/AbyssalTurtleRenderer.java`: texture and shadow configuration.
- Registry classes and loader entrypoints: register and wire the new content without moving shared behavior into loader modules.
- `common/src/main/resources/data/spd/worldgen/**`: biome and chrome-cave ore definitions.
- `common/src/main/resources/assets/spd/**`: model, animation, texture, blockstate, item model, effect icon, and translations.

## Task 1: Worldgen Contract and Biome Keys

**Files:**
- Create: `common/src/test/java/alku/spd/world/AbyssalCoastWorldgenTest.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdBiomes.java`
- Create: `common/src/main/java/alku/spd/world/AbyssalCoastRegion.java`
- Modify: `common/src/main/java/alku/spd/world/SpdTerraBlender.java`
- Create: `common/src/main/resources/data/spd/worldgen/biome/abyssal_coast.json`
- Create: `common/src/main/resources/data/spd/worldgen/biome/fungal_shallows.json`
- Create: `common/src/main/resources/data/spd/worldgen/biome/chrome_seabed_caves.json`
- Modify: `common/src/main/resources/data/minecraft/tags/worldgen/biome/is_overworld.json`
- Create: `common/src/main/resources/data/spd/tags/worldgen/biome/abyssal_turtle_spawns.json`
- Modify: `common/src/main/resources/data/spd/tags/worldgen/biome/is_abyssal_blood_desert.json`

- [ ] **Step 1: Write the failing worldgen resource test**

Parse all three biome JSON files with Gson and assert `effects.water_color == 0x2A070B` for coast/shallows, all keys appear in `is_overworld`, all three appear in the existing abyssal biome tag, and all three appear in `abyssal_turtle_spawns`. Assert `SpdBiomes` exposes `ABYSSAL_COAST`, `FUNGAL_SHALLOWS`, and `CHROME_SEABED_CAVES`.

- [ ] **Step 2: Verify RED**

Run: `gradle :common:test --tests alku.spd.world.AbyssalCoastWorldgenTest --console=plain`

Expected: compilation/resource failure because the new keys and JSON files do not exist.

- [ ] **Step 3: Add biome keys and JSON definitions**

Define each key with `ResourceKey.create(Registries.BIOME, new ResourceLocation(Spd.MOD_ID, id))`. Base coast generation settings on beach, shallows on warm ocean, and caves on dripstone caves while explicitly listing the SPD ore placed feature for the cave biome. Set water color to decimal `2754315` (`0x2A070B`) and water fog color to `1116168` (`0x110808`).

- [ ] **Step 4: Implement TerraBlender mapping and surface rules**

Register `AbyssalCoastRegion` with weight 10. Map coast at low continentalness near the blood-desert temperature/erosion range, shallows at ocean continentalness, and chrome caves at negative depth. Add surface rules: coast floor is blood sand with sacred stigma below; shallows floor sequences blood sand then sacred stigma; cave biome replaces exposed stone surfaces with sacred stigma. Keep the existing blood desert rule intact.

- [ ] **Step 5: Verify GREEN and commit**

Run: `gradle :common:test --tests alku.spd.world.AbyssalCoastWorldgenTest --console=plain`

Commit: `git commit -m "-（添加渊蚀海岸群系）"`

## Task 2: Chrome Cave Ore Increase

**Files:**
- Extend test: `common/src/test/java/alku/spd/world/AbyssalCoastWorldgenTest.java`
- Create: `common/src/main/resources/data/spd/worldgen/configured_feature/ore_blazing_vein_chrome_caves.json`
- Create: `common/src/main/resources/data/spd/worldgen/placed_feature/ore_blazing_vein_chrome_caves.json`
- Modify: `fabric/src/main/java/alku/spd/fabric/SpdFabric.java`
- Create: `forge/src/main/resources/data/spd/forge/biome_modifier/add_chrome_cave_blazing_vein.json`

- [ ] **Step 1: Add failing assertions for exact ore density**

Assert configured feature size is 14, placed count is 50, height range is uniform from absolute -48 through absolute 40, and loader hooks select only `spd:chrome_seabed_caves`.

- [ ] **Step 2: Verify RED**

Run the worldgen test and confirm missing ore files fail it.

- [ ] **Step 3: Add dedicated ore resources and loader wiring**

Use the existing blazing-vein target rules with size 14. Fabric uses `BiomeSelectors.includeByKey(SpdBiomes.CHROME_SEABED_CAVES)`; Forge uses an `add_features` biome modifier with biome `spd:chrome_seabed_caves`, feature `spd:ore_blazing_vein_chrome_caves`, and step `underground_ores`.

- [ ] **Step 4: Verify GREEN and commit**

Run the focused test, then `gradle :fabric:build :forge:build --console=plain`.

Commit: `git commit -m "-（提升海底矿洞炽脉矿率）"`

## Task 3: Turtle Registration and Deterministic Mechanics

**Files:**
- Create: `common/src/test/java/alku/spd/entity/AbyssalTurtleMechanicsTest.java`
- Create: `common/src/main/java/alku/spd/entity/AbyssalTurtleMechanics.java`
- Create: `common/src/main/java/alku/spd/entity/AbyssalTurtleEntity.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdEntities.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdTags.java`
- Modify: `common/src/main/resources/data/spd/tags/entity_types/abyssal_entities.json`
- Modify: `fabric/src/main/java/alku/spd/fabric/SpdFabric.java`
- Modify: `forge/src/main/java/alku/spd/forge/SpdForge.java`

- [ ] **Step 1: Write failing mechanics tests**

Test exact difficulty damage `0/3/4/6`, land/water attack intervals `26/19`, land/water armor `10/8`, shell damage multiplier `0.3`, fire multiplier `0.6`, purification multiplier `2.0`, and infection progression `+20 per second in biome`, `-5 per second outside`, clamped to `0..6000`.

- [ ] **Step 2: Verify RED**

Run: `gradle :common:test --tests alku.spd.entity.AbyssalTurtleMechanicsTest --console=plain`

Expected: compile failure because `AbyssalTurtleMechanics` does not exist.

- [ ] **Step 3: Implement pure mechanics and base entity**

Make the helper package-visible and side-effect free. Implement `AbyssalTurtleEntity extends Turtle implements GeoEntity` with 30 health, 0.15 movement, 4 attack, 8 armor, 24 follow range, water travel acceleration targeting effective speed 0.32, 3-5 XP, fire/purification resistance rules, and a neutral target selector containing only retaliation and explicit nest anger. Do not add `NearestAttackableTargetGoal` with `SpdEntityTargeting::isNonSpdLiving`.

- [ ] **Step 4: Register entity and loader attributes/spawn placement**

Register `spd:abyssal_turtle` as `MobCategory.CREATURE`, size `1.2F x 0.5F`, tracking range 10. Add it to `abyssal_entities`; register attributes and `SpawnPlacements.Type.ON_GROUND` with `MOTION_BLOCKING_NO_LEAVES`, accepting spawn positions in `ABYSSAL_TURTLE_SPAWNS` on sand-like ground or in water.

- [ ] **Step 5: Verify GREEN and commit**

Run the focused test and `gradle :common:compileJava :fabric:compileJava :forge:compileJava --console=plain`.

Commit: `git commit -m "-（注册渊蚀海龟实体）"`

## Task 4: Combat Skills and Spore Sluggishness

**Files:**
- Extend test: `common/src/test/java/alku/spd/entity/AbyssalTurtleMechanicsTest.java`
- Create: `common/src/test/java/alku/spd/effect/SporeSluggishnessEffectTest.java`
- Create: `common/src/main/java/alku/spd/effect/SporeSluggishnessEffect.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdEffects.java`
- Modify: `common/src/main/java/alku/spd/entity/AbyssalTurtleEntity.java`
- Modify: `common/src/main/java/alku/spd/mixin/PlayerMixin.java`

- [ ] **Step 1: Write failing state-transition and modifier tests**

Test spore cooldown 100, fog duration 40, shell threshold 40%, shell duration 120, shell cooldown 400, charge minimum distance 4, charge cooldown 160, and sluggishness modifiers `-0.15` with `MULTIPLY_TOTAL`. Test player mining multiplier returns `0.85` only while the effect is present.

- [ ] **Step 2: Verify RED**

Run both focused test classes and confirm failures come from missing effect/state methods.

- [ ] **Step 3: Implement bite and spore fog**

Override `doHurtTarget` to apply difficulty damage and 2 pressure layers for 240 ticks. For the 20% armor-pierce roll, apply a transient `-2` armor modifier to the target only around the damage call and remove it in `finally`. On successful damage, start a 40-tick fog if its 100-tick cooldown is clear; every 20 ticks affect non-SPD living entities in a 2-block inflated bounding box with one pressure layer and 40 ticks of sluggishness. Apply one immediate pulse to a melee attacker.

- [ ] **Step 4: Implement shell guard and current charge**

Synchronize shell and charge booleans with `SynchedEntityData`. Shell guard stops navigation and attacks, emits a pulse every second, multiplies incoming damage by 0.3, and ends immediately for `PURIFICATION_DAMAGE`. Charge steers toward a waterborne target, resolves exactly once within 1.6 blocks for 6 damage, 2 pressure layers and knockback strength 2, then retreats for 10 ticks. All cooldowns persist through save/load.

- [ ] **Step 5: Register effect and mining hook**

Register `spore_sluggishness` as harmful color `0x35151B`; add an attack-speed attribute modifier with a fixed UUID and `MULTIPLY_TOTAL -0.15`. Extend the existing `PlayerMixin#getDestroySpeed` injection to multiply by `0.85F` while active.

- [ ] **Step 6: Verify GREEN and commit**

Run focused tests and common compile.

Commit: `git commit -m "-（实现渊蚀海龟战斗技能）"`

## Task 5: Hatchable Abyssal Turtle Eggs

**Files:**
- Create: `common/src/test/java/alku/spd/block/AbyssalTurtleEggLogicTest.java`
- Create: `common/src/main/java/alku/spd/block/AbyssalTurtleEggLogic.java`
- Create: `common/src/main/java/alku/spd/block/AbyssalTurtleEggBlock.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdBlocks.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdItems.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdCreativeTabs.java`
- Modify: `common/src/main/java/alku/spd/entity/AbyssalTurtleEntity.java`
- Create: `common/src/main/resources/data/spd/loot_tables/blocks/abyssal_turtle_egg.json`
- Create: `common/src/main/resources/data/minecraft/tags/blocks/turtle_eggs.json`

- [ ] **Step 1: Write failing egg logic tests**

Test egg count clamps to 1-4, hatch stage clamps to 0-2, night is the only valid progression window, hatch count equals stacked egg count, nest intrusion radius is 4, and anger broadcast radius is 12.

- [ ] **Step 2: Verify RED**

Run the focused test and confirm the missing helper causes failure.

- [ ] **Step 3: Implement egg block**

Model behavior after vanilla `TurtleEggBlock`: `EGGS` 1-4, `HATCH` 0-2, random night ticks, entity fall/trample breaking, explosion/player break anger, and silk-touch-only block drops. Hatching creates one baby `ABYSSAL_TURTLE` per egg, sets age to `-24000`, and records the hatch position as home.

- [ ] **Step 4: Implement nesting behavior**

Replace inherited turtle egg-laying goal with a higher-priority custom goal that checks `hasEgg()`, navigates to home, places 1-2 abyssal turtle eggs on valid blood sand/sand substrate, clears carried-egg state, and never places vanilla turtle eggs. Nearby players within 4 blocks set adult turtles' target; damage to eggs broadcasts the responsible player within 12 blocks.

- [ ] **Step 5: Register block/item and verify GREEN**

Register both under `abyssal_turtle_egg`, add to creative tab and vanilla turtle egg block tag, then run focused tests and common compile.

Commit: `git commit -m "-（添加渊蚀龟蛋生态）"`

## Task 6: Infection and Loot

**Files:**
- Create: `common/src/test/java/alku/spd/world/AbyssalTurtleInfectionTest.java`
- Create: `common/src/main/java/alku/spd/world/AbyssalTurtleInfectionCarrier.java`
- Create: `common/src/main/java/alku/spd/world/AbyssalTurtleInfectionEvents.java`
- Create: `common/src/main/java/alku/spd/mixin/TurtleMixin.java`
- Modify: `common/src/main/java/alku/spd/Spd.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdItems.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdCreativeTabs.java`
- Modify: `common/src/main/resources/spd.mixins.json`
- Create: `common/src/main/resources/data/spd/loot_tables/entities/abyssal_turtle.json`

- [ ] **Step 1: Write failing infection and loot resource tests**

Test conversion threshold 6000, outside decay 5 per second, and preservation DTO fields for age, custom name, health ratio, home position, and carried egg. Parse loot JSON and assert scute 1-2 at 100%, spore 1 at 30%, residue 2 at 50%, hide 1 at 20%, and egg 1 at 5%.

- [ ] **Step 2: Verify RED**

Run both focused tests and confirm missing implementation/resources fail.

- [ ] **Step 3: Implement persistent infection**

On `SERVER_LEVEL_POST` every 20 ticks, find live vanilla `Turtle` instances excluding `AbyssalTurtleEntity`. Store progress in persistent NBT via a small `AbyssalTurtleInfectionCarrier` mixin interface rather than scoreboard tags. Add 20 in an abyssal biome, subtract 5 outside, convert at 6000, and explicitly copy all fields in the preservation DTO after `convertTo`.

- [ ] **Step 4: Add scute item and loot table**

Register `etched_turtle_scute`; create exact independent loot pools. Do not add liquid gold to this table because `LivingEntityMixin` already supplies it for every `spd` entity.

- [ ] **Step 5: Verify GREEN and commit**

Run focused tests and common compile.

Commit: `git commit -m "-（添加海龟感染与掉落）"`

## Task 7: GeckoLib Assets, Renderer, and Derived Textures

**Files:**
- Create: `common/src/test/java/alku/spd/entity/AbyssalTurtleAssetTest.java`
- Create: `common/src/main/java/alku/spd/client/model/AbyssalTurtleModel.java`
- Create: `common/src/main/java/alku/spd/client/renderer/AbyssalTurtleRenderer.java`
- Modify: `common/src/main/java/alku/spd/entity/AbyssalTurtleEntity.java`
- Modify: `fabric/src/main/java/alku/spd/fabric/client/SpdFabricClient.java`
- Modify: `forge/src/main/java/alku/spd/forge/client/SpdForgeClient.java`
- Create: `common/src/main/resources/assets/spd/geo/abyssal_turtle.geo.json`
- Create: `common/src/main/resources/assets/spd/animations/abyssal_turtle.animation.json`
- Create: `common/src/main/resources/assets/spd/textures/entity/abyssal_turtle.png`
- Create: egg blockstates/models/textures, item models/textures, and `textures/mob_effect/spore_sluggishness.png`

- [ ] **Step 1: Write failing asset contract tests**

Assert the supplied model SHA-256, entity texture dimensions 128x128, spawn egg texture dimensions 16x16 and SHA-256 `b11f6d2ba82d8e6904936a6b8fe5b1534a4f902e38de776a5fd68d614b5b11ea`, model bones include `body`, `head`, `leg0..leg3`, animation keys are exactly `idle`, `walk`, `attack`, every animated bone exists in the model, and all blockstate/item model texture references resolve.

- [ ] **Step 2: Verify RED**

Run the asset test and confirm resources are missing.

- [ ] **Step 3: Import supplied resources**

Copy `E:\代码\MC模型\转换后\霉染海龟   - Converted.geo.json` to the geo path, `E:\代码\MC模型\转换后\海龟动画.json` to the animation path, `C:\Users\Administrator\AppData\Local\Temp\codex-clipboard-Pu4Fz3.png` to the entity texture path, and `C:\Users\Administrator\AppData\Local\Temp\codex-clipboard-KtT8hv.png` to `assets/spd/textures/item/abyssal_turtle_spawn_egg.png`. Preserve bytes for all supplied files.

- [ ] **Step 4: Generate supporting pixel textures**

Create egg, scute, and sluggishness textures from sampled dark-brown, blood-red, and calcified-white colors in the supplied entity texture. Keep generated item textures 16x16 and block textures 16x16; define stable blockstate variants for every `eggs=1..4,hatch=0..2` combination. Use the supplied spawn egg texture unchanged and use standard generated item models.

- [ ] **Step 5: Wire animation controller and renderers**

Map idle to `idle`, all land/water movement and charge motion to looped `walk`, bite to one-shot `attack`, and shell guard to looped `idle` with movement locked. Register renderer in both loaders with shadow radius 0.7. Register the custom spawn egg item using `ArchitecturySpawnEggItem` and the derived texture model.

- [ ] **Step 6: Verify GREEN and commit**

Run asset tests and both loader compiles.

Commit: `git commit -m "-（适配渊蚀海龟模型动画）"`

## Task 8: Language, Integration, and Release Verification

**Files:**
- Modify: `common/src/main/resources/assets/spd/lang/zh_cn.json`
- Modify: `common/src/main/resources/assets/spd/lang/en_us.json`
- Modify: `common/src/main/resources/assets/spd/texts/credits.txt` if it lists implemented creatures
- Modify: `common/src/main/java/alku/spd/registry/SpdCreativeTabs.java`

- [ ] **Step 1: Add translation/resource assertions**

Extend `AbyssalTurtleAssetTest` to require translations for all three biomes, entity, spawn egg, egg block, scute, and effect. Assert `block.spd.sacred_stigma` is exactly `渊浊石` in Chinese while registry ID remains unchanged.

- [ ] **Step 2: Verify RED, add translations, then verify GREEN**

Run the focused asset test before and after adding translations.

- [ ] **Step 3: Run complete verification**

Run:

```powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' build --console=plain
git diff --check
```

Expected: `BUILD SUCCESSFUL`, all tests pass, and `git diff --check` emits no errors.

- [ ] **Step 4: Audit final JARs**

Open both `fabric/build/libs/spd-fabric-1.0-SNAPSHOT.jar` and `forge/build/libs/spd-forge-1.0-SNAPSHOT.jar`. Assert each contains all three biome JSON files, turtle loot table, egg blockstate, geo, animation, entity texture, spawn egg texture, and effect icon. Hash the packaged geo/animation/entity texture and compare with source resources.

- [ ] **Step 5: Manual client acceptance when runtime is available**

Launch both loader clients separately. In a fresh world use `/locate biome spd:abyssal_coast`, `/locate biome spd:fungal_shallows`, and `/locate biome spd:chrome_seabed_caves`; inspect blood-black water, sacred-stigma terrain, ore density, land/water animations, neutral behavior, egg anger, hatching, bite, fog, shell, and charge. Record any unavailable manual check explicitly rather than treating build success as visual proof.

- [ ] **Step 6: Final commit and push**

Commit any final integration-only changes as `-（完成渊蚀海龟生态）`, push `master`, fetch it, and verify `git rev-parse HEAD` equals `git rev-parse origin/master`. Do not stage `.architectury-transformer/`, `.superpowers/`, `CLAUDE.md`, or `common/logs/`.
