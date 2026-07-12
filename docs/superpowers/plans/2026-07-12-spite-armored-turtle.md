# Spite-Armored Turtle Implementation Plan

> For agentic workers: REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

Goal: Add spd:spite_armored_turtle with inherited vanilla turtle navigation, water torpedo combat, land shell defense, pollution, pseudo-nesting, dedicated drops, and Forge/Fabric registration.

Architecture: Keep the entity and registries in common. Implement SpiteArmoredTurtleEntity extends Turtle for shared behavior, and keep loader files limited to attributes, spawn placement, biome spawn injection, and client renderer registration. Implement pseudo-nesting as a timed spite_nodule block entity that converts nearby sand to rust_sand on the server.

Tech Stack: Java 17, Minecraft 1.20.1, Architectury, Forge/Fabric, GeckoLib 4, Gradle 8.12.1, and Node.js static resource verification.

---

## File Map

New Java files:
- common/src/main/java/alku/spd/entity/SpiteArmoredTurtleEntity.java: entity state, goals, attacks, drops, GeckoLib controller, and NBT.
- common/src/main/java/alku/spd/block/SpiteNoduleBlock.java: timed block and server ticker.
- common/src/main/java/alku/spd/block/entity/SpiteNoduleBlockEntity.java: lifetime, expiry conversion, and NBT.
- common/src/main/java/alku/spd/client/model/SpiteArmoredTurtleModel.java: model, texture, and animation paths.
- common/src/main/java/alku/spd/client/renderer/SpiteArmoredTurtleRenderer.java: GeckoLib renderer.
- scripts/verify-spite-armored-turtle.cjs: static resource, registry, and JAR verifier.

Modified Java files:
- common/src/main/java/alku/spd/registry/SpdEntities.java
- common/src/main/java/alku/spd/registry/SpdItems.java
- common/src/main/java/alku/spd/registry/SpdBlocks.java
- common/src/main/java/alku/spd/registry/SpdBlockEntities.java
- common/src/main/java/alku/spd/registry/SpdCreativeTabs.java
- common/src/main/java/alku/spd/mixin/CactusBlockMixin.java
- forge/src/main/java/alku/spd/forge/SpdForge.java
- forge/src/main/java/alku/spd/forge/client/SpdForgeClient.java
- fabric/src/main/java/alku/spd/fabric/SpdFabric.java
- fabric/src/main/java/alku/spd/fabric/client/SpdFabricClient.java

Runtime resources:
- common/src/main/resources/assets/spd/geo/spite_armored_turtle.geo.json
- common/src/main/resources/assets/spd/animations/spite_armored_turtle.animation.json
- common/src/main/resources/assets/spd/textures/entity/spite_armored_turtle.png
- common/src/main/resources/assets/spd/textures/block/spite_nodule.png
- common/src/main/resources/assets/spd/textures/block/rust_sand.png
- common/src/main/resources/assets/spd/models/item/heavy_spite_scute.json
- common/src/main/resources/assets/spd/models/item/residual_malice.json
- common/src/main/resources/assets/spd/models/item/spite_armored_turtle_spawn_egg.json
- common/src/main/resources/assets/spd/models/item/spite_nodule.json
- common/src/main/resources/assets/spd/models/item/rust_sand.json
- common/src/main/resources/assets/spd/blockstates/spite_nodule.json
- common/src/main/resources/assets/spd/blockstates/rust_sand.json
- common/src/main/resources/assets/spd/models/block/spite_nodule.json
- common/src/main/resources/assets/spd/models/block/rust_sand.json
- common/src/main/resources/assets/spd/lang/zh_cn.json
- common/src/main/resources/assets/spd/lang/en_us.json

Data resources:
- forge/src/main/resources/data/spd/forge/biome_modifier/add_spite_armored_turtle.json
- common/src/main/resources/data/spd/tags/blocks/rust_sand.json

---

### Task 1: Add the static verifier and supplied GeckoLib assets

Files:
- Create scripts/verify-spite-armored-turtle.cjs
- Create common/src/main/resources/assets/spd/geo/spite_armored_turtle.geo.json
- Create common/src/main/resources/assets/spd/animations/spite_armored_turtle.animation.json
- Create common/src/main/resources/assets/spd/textures/entity/spite_armored_turtle.png

- [ ] Step 1: Copy the supplied assets without changing their JSON structure.

~~~powershell
Copy-Item -LiteralPath 'E:\代码\MC模型\转换后\霉染海龟   - Converted.geo.json' -Destination 'common\src\main\resources\assets\spd\geo\spite_armored_turtle.geo.json'
Copy-Item -LiteralPath 'E:\代码\MC模型\转换后\海龟动画.json' -Destination 'common\src\main\resources\assets\spd\animations\spite_armored_turtle.animation.json'
Copy-Item -LiteralPath 'E:\代码\MC模型\转换后\sea_turtle.png' -Destination 'common\src\main\resources\assets\spd\textures\entity\spite_armored_turtle.png'
~~~

Expected: all three target files exist, the PNG is 128x128, and the animation document contains idle, walk, and attack clips.

- [ ] Step 2: Write the verifier before registry changes.

The CommonJS verifier must resolve the repository root from __dirname, assert required paths, parse both JSON files, assert that geo.json has a minecraft:geometry array, and assert that animation keys contain terminal names idle, walk, and attack. It must also check the literal registry markers SPITE_ARMORED_TURTLE, HEAVY_SPITE_SCUTE, RESIDUAL_MALICE, SPITE_NODULE, and RUST_SAND after those files exist. A --resources-only option skips source markers. A --jar path option reads the ZIP entry list and checks all runtime resource paths. Every failure must print the missing path or marker and exit nonzero.

Run:

~~~powershell
node scripts/verify-spite-armored-turtle.cjs --resources-only
~~~

Expected: PASS for the three supplied files and their JSON structure.

- [ ] Step 3: Run git diff --check and confirm only the copied resources and verifier are present before continuing.

---

### Task 2: Register the entity, items, blocks, and creative entries

Files:
- Modify common/src/main/java/alku/spd/registry/SpdEntities.java
- Modify common/src/main/java/alku/spd/registry/SpdItems.java
- Modify common/src/main/java/alku/spd/registry/SpdBlocks.java
- Modify common/src/main/java/alku/spd/registry/SpdBlockEntities.java
- Modify common/src/main/java/alku/spd/registry/SpdCreativeTabs.java

- [ ] Step 1: Add the entity type and import SpiteArmoredTurtleEntity.

~~~java
public static final RegistrySupplier<EntityType<SpiteArmoredTurtleEntity>> SPITE_ARMORED_TURTLE = ENTITIES.register("spite_armored_turtle", () ->
        EntityType.Builder.of(SpiteArmoredTurtleEntity::new, MobCategory.CREATURE)
                .sized(1.2F, 0.4F)
                .clientTrackingRange(10)
                .build("spite_armored_turtle"));
~~~

Keep this in the creature registration section and do not change existing IDs.

- [ ] Step 2: Add the two drops and spawn egg.

~~~java
public static final RegistrySupplier<Item> HEAVY_SPITE_SCUTE = ITEMS.register("heavy_spite_scute", () ->
        new Item(new Item.Properties()));

public static final RegistrySupplier<Item> RESIDUAL_MALICE = ITEMS.register("residual_malice", () ->
        new Item(new Item.Properties()));

public static final RegistrySupplier<Item> SPITE_ARMORED_TURTLE_SPAWN_EGG = ITEMS.register("spite_armored_turtle_spawn_egg", () ->
        new ArchitecturySpawnEggItem(SpdEntities.SPITE_ARMORED_TURTLE, 0x4A4D4B, 0xA8242F, new Item.Properties()));
~~~

Do not change liquid_gold registration or its pickup mixin.

- [ ] Step 3: Register blocks and block entity.

~~~java
public static final RegistrySupplier<Block> SPITE_NODULE = BLOCKS.register("spite_nodule", () ->
        new SpiteNoduleBlock(BlockBehaviour.Properties.copy(Blocks.NETHERRACK)
                .strength(1.0F)
                .sound(SoundType.NETHERRACK)));

public static final RegistrySupplier<Block> RUST_SAND = BLOCKS.register("rust_sand", () ->
        new AbyssalBloodSandBlock(BlockBehaviour.Properties.copy(Blocks.SAND)
                .isViewBlocking((state, level, pos) -> true)));

public static final RegistrySupplier<BlockEntityType<SpiteNoduleBlockEntity>> SPITE_NODULE = BLOCK_ENTITIES.register("spite_nodule", () ->
        BlockEntityType.Builder.of(SpiteNoduleBlockEntity::new, SpdBlocks.SPITE_NODULE.get()).build(null));
~~~

Use distinct class imports and the existing DeferredRegister pattern.

- [ ] Step 4: Add HEAVY_SPITE_SCUTE, RESIDUAL_MALICE, SPITE_NODULE, RUST_SAND, and SPITE_ARMORED_TURTLE_SPAWN_EGG to SpdCreativeTabs.

- [ ] Step 5: Run common compilation.

~~~powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :common:compileJava --console=plain
~~~

Expected: registration classes compile once the typed class shells exist.

---

### Task 3: Implement SpiteArmoredTurtleEntity

File:
- Create common/src/main/java/alku/spd/entity/SpiteArmoredTurtleEntity.java

- [ ] Step 1: Define the class, synced state, constants, and attributes.

The class extends Turtle and implements GeoEntity. Define a synced Boolean SHELL_DEFENSIVE. Keep cooldowns server-side and save them in NBT. Use these constants:

~~~java
private static final int SHELL_DEFENSIVE_TICKS = 40;
private static final int TORPEDO_COOLDOWN_TICKS = 60;
private static final int POLLUTION_INTERVAL_TICKS = 4;
private static final int NODULE_MIN_COUNT = 2;
private static final int NODULE_MAX_COUNT = 4;
~~~

Return Turtle.createAttributes() with MAX_HEALTH 32, ATTACK_DAMAGE 8, ARMOR 8, KNOCKBACK_RESISTANCE 0.65, MOVEMENT_SPEED 0.45, and FOLLOW_RANGE 24.

- [ ] Step 2: Preserve vanilla goals and add SPD goals.

Call super.registerGoals() first, then add:

~~~java
this.goalSelector.addGoal(0, new TorpedoAttackGoal(this));
this.goalSelector.addGoal(1, new ShellDefenseGoal(this));
this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
        this, LivingEntity.class, 24, true, false, SpdEntityTargeting::isNonSpdLiving));
~~~

TorpedoAttackGoal uses MOVE, LOOK, and JUMP flags, starts only in water with a non-SPD target 3-12 blocks away, stops navigation during windup, applies horizontal velocity once, and ends on hit, landing, or 20 flight ticks. ShellDefenseGoal pauses movement while the synchronized shell flag is true.

- [ ] Step 3: Implement ordinary melee, torpedo hit, and pollution.

Ordinary doHurtTarget uses damageSources().mobAttack(this), adds RendingEffect for 60 ticks at amplifier 0, and returns the hurt result. The torpedo hit deals 8 damage, knocks the target back one block, and applies the same RendingEffect.

Every four server ticks while in water and moving horizontally, scan an AABB of 1.5 blocks around the previous position. Each alive non-SPD LivingEntity receives MobEffects.POISON for 40 ticks at amplifier 0, with at most one direct damage event per entity per interval. Spawn a small CRIMSON_SPORE batch for the visual trail; do not create a persistent entity.

- [ ] Step 4: Implement shell defense in hurt.

When on land and hit by a direct living attacker, set SHELL_DEFENSIVE, set shellTicks to 40, reduce incoming damage to 30%, and retaliate once per 20-tick cooldown. Retaliation scans four blocks, excludes SPD entities and the turtle, applies two damage and one block of knockback. While shell defensive is true, doHurtTarget returns false and navigation is stopped. Halve fire damage and double damage tagged SpdTags.PURIFICATION_DAMAGE before shell reduction.

- [ ] Step 5: Replace vanilla egg laying without removing vanilla home behavior.

Use the overridable 1.20.1 Turtle egg-laying hook. If that hook is not overridable in the current mappings, replace only the vanilla lay-egg goal. On a valid home beach, place 2-4 spite_nodule blocks above sand, red sand, or blood sand, create their block entities, and never place ordinary turtle eggs. Leave vanilla home position and travel behavior intact.

- [ ] Step 6: Add NBT and GeckoLib controller.

Save and load shellTicks, torpedoCooldown, pollutionTicks, and shellRetaliationCooldown under Spite-prefixed keys. Controller priority is death, attack, movement, idle:

~~~java
if (this.isDeadOrDying()) {
    state.setAndContinue(DEATH);
} else if (this.swinging) {
    state.setAndContinue(ATTACK);
} else if (state.isMoving() || horizontalSpeedSquared() > 1.0E-5D) {
    state.setAndContinue(WALK);
} else {
    state.setAndContinue(IDLE);
}
return PlayState.CONTINUE;
~~~

Use the actual resource clip names after inspecting the copied JSON, preserving terminal names idle, walk, and attack.

- [ ] Step 7: Run common compilation and source/resource verification.

~~~powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :common:compileJava --console=plain
node scripts/verify-spite-armored-turtle.cjs
~~~

Expected: compilation passes, source markers and all resource assertions pass.

---

### Task 4: Implement the timed nodule and rust-sand conversion

Files:
- Create common/src/main/java/alku/spd/block/SpiteNoduleBlock.java
- Create common/src/main/java/alku/spd/block/entity/SpiteNoduleBlockEntity.java
- Modify common/src/main/java/alku/spd/mixin/CactusBlockMixin.java

- [ ] Step 1: Make SpiteNoduleBlock an EntityBlock with a server-only ticker.

Return SpiteNoduleBlockEntity from newBlockEntity. Return a ticker only when the level is server-side and blockEntityType equals SpdBlockEntities.SPITE_NODULE. No client ticker is allowed.

- [ ] Step 2: Implement persistent lifetime and silk-touch drops.

Initialize remainingTicks to 20 * 60, save it as RemainingTicks, and restore it in load. Use the block destroy hook to detect a player holding Silk Touch. Drop 1-2 RESIDUAL_MALICE only for Silk Touch and ensure the normal block loot path is not duplicated.

- [ ] Step 3: Convert nearby sand on expiry.

At zero ticks, scan radius four and vertical range two. Replace only Blocks.SAND, Blocks.RED_SAND, and SpdBlocks.ABYSSAL_BLOOD_SAND with SpdBlocks.RUST_SAND. Never replace block entities, bedrock, fluids, or existing SPD blocks. Set flags to 3 and remove the nodule once.

- [ ] Step 4: Prevent plants on rust sand.

Extend CactusBlockMixin so rust_sand is never accepted as a valid cactus base while preserving the existing neighbor-solid check and blood-sand behavior. Add rust_sand to a shared block tag if later plant hooks use that tag; do not change ordinary sand behavior.

- [ ] Step 5: Add block resources.

Copy vine_plague_node.png to spite_nodule.png and abyssal_blood_sand.png to rust_sand.png for this release. Add cube-all block models, default blockstates, and item models with independent IDs.

- [ ] Step 6: Run focused block verification.

~~~powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :common:compileJava --console=plain
node scripts/verify-spite-armored-turtle.cjs
~~~

Expected: both block classes compile and the verifier finds both block registrations and block resources.

---

### Task 5: Add Forge/Fabric attributes, spawns, and renderers

Files:
- Modify forge/src/main/java/alku/spd/forge/SpdForge.java
- Modify forge/src/main/java/alku/spd/forge/client/SpdForgeClient.java
- Modify fabric/src/main/java/alku/spd/fabric/SpdFabric.java
- Modify fabric/src/main/java/alku/spd/fabric/client/SpdFabricClient.java
- Create forge/src/main/resources/data/spd/forge/biome_modifier/add_spite_armored_turtle.json
- Create common/src/main/java/alku/spd/client/model/SpiteArmoredTurtleModel.java
- Create common/src/main/java/alku/spd/client/renderer/SpiteArmoredTurtleRenderer.java

- [ ] Step 1: Register Forge attributes and placement.

Add this in registerAttributes:

~~~java
event.put(SpdEntities.SPITE_ARMORED_TURTLE.get(), SpiteArmoredTurtleEntity.createAttributes().build());
~~~

Register SpawnPlacements.Type.ON_GROUND with Heightmap.Types.MOTION_BLOCKING_NO_LEAVES and SpiteArmoredTurtleEntity::checkSpawnRules in commonSetup.

- [ ] Step 2: Add the Forge biome modifier.

Use forge:add_spawns with biome selector #minecraft:is_ocean and spawner:

~~~json
{
  "type": "spd:spite_armored_turtle",
  "weight": 3,
  "minCount": 1,
  "maxCount": 2
}
~~~

The entity spawn predicate also allows beaches for natural spawns.

- [ ] Step 3: Register Fabric attributes, placement, and spawn injection.

Use FabricDefaultAttributeRegistry.register and the same spawn placement. Add BiomeModifications for BiomeSelectors.tag(BiomeTags.IS_OCEAN), category MobCategory.CREATURE, weight 3, min 1, max 2. The entity predicate remains the final ocean/beach surface gate.

- [ ] Step 4: Add model and renderer.

SpiteArmoredTurtleModel returns these paths:

~~~java
new ResourceLocation(Spd.MOD_ID, "geo/spite_armored_turtle.geo.json");
new ResourceLocation(Spd.MOD_ID, "textures/entity/spite_armored_turtle.png");
new ResourceLocation(Spd.MOD_ID, "animations/spite_armored_turtle.animation.json");
~~~

SpiteArmoredTurtleRenderer extends GeoEntityRenderer<SpiteArmoredTurtleEntity> and sets shadowRadius to 0.65F.

- [ ] Step 5: Register the renderer on both clients.

Add EntityRendererRegistry.register in Fabric and event.registerEntityRenderer in Forge using SpiteArmoredTurtleRenderer::new next to the fox/wolf/zombie registrations.

- [ ] Step 6: Compile both loaders.

~~~powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :fabric:compileJava :forge:compileJava --console=plain
~~~

Expected: both loader modules compile without cross-loader API references.

---

### Task 6: Add models, language keys, and creative-facing resources

Files:
- Create item model files for heavy_spite_scute, residual_malice, spite_armored_turtle_spawn_egg, spite_nodule, and rust_sand.
- Create blockstates and block model files for spite_nodule and rust_sand.
- Modify common/src/main/resources/assets/spd/lang/zh_cn.json and en_us.json.

- [ ] Step 1: Use minecraft:item/generated for the two drops, minecraft:item/template_spawn_egg for the egg, and minecraft:block/cube_all for block items.

- [ ] Step 2: Map each default blockstate to block/<id>, bind spite_nodule to the copied fungus-node texture, and bind rust_sand to the copied blood-sand texture.

- [ ] Step 3: Add these Chinese keys exactly:

~~~json
"entity.spd.spite_armored_turtle": "怨甲海龟",
"item.spd.heavy_spite_scute": "沉重怨鳞",
"item.spd.residual_malice": "液态怨金残留",
"item.spd.spite_armored_turtle_spawn_egg": "怨甲海龟刷怪蛋",
"block.spd.spite_nodule": "怨金结核",
"block.spd.rust_sand": "铁锈沙"
~~~

Add corresponding English values: Spite-Armored Turtle, Heavy Spite-Scute, Residual Malice, Spite-Armored Turtle Spawn Egg, Spite Nodule, and Rust Sand.

- [ ] Step 4: Run resource verification.

~~~powershell
node scripts/verify-spite-armored-turtle.cjs --resources-only
~~~

Expected: PASS with no missing model, blockstate, language, animation, geometry, or texture path.

---

### Task 7: Build, inspect artifacts, commit, and push

Files:
- Only the files listed in this plan may be staged.
- Do not stage CLAUDE.md, .superpowers, build outputs, or run directories.

- [ ] Step 1: Run the complete build.

~~~powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' build --console=plain
~~~

Expected: BUILD SUCCESSFUL and both loader JARs under their build/libs directories.

- [ ] Step 2: Inspect final JAR entries.

~~~powershell
$forgeJar = Get-ChildItem forge/build/libs -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$fabricJar = Get-ChildItem fabric/build/libs -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
node scripts/verify-spite-armored-turtle.cjs --jar $forgeJar.FullName
node scripts/verify-spite-armored-turtle.cjs --jar $fabricJar.FullName
~~~

Expected: both JAR checks pass and contain the common entity/resource paths.

- [ ] Step 3: Run final checks.

~~~powershell
git diff --check
git status --short
git diff --stat
~~~

Expected: no whitespace errors and only planned files staged.

- [ ] Step 4: Commit using the repository format.

~~~powershell
git add common forge fabric scripts docs/superpowers/plans/2026-07-12-spite-armored-turtle.md
git commit -m "-（添加怨甲海龟）"
~~~

- [ ] Step 5: Push and verify remote tip.

~~~powershell
git push origin master
git rev-parse HEAD
git ls-remote origin refs/heads/master
~~~

Expected: local HEAD and the remote master hash are equal. Use the configured SSH-over-443 remote when HTTPS is unavailable.

---

## Self-Review Checklist

- Spec coverage: entity identity, inherited turtle behavior, combat modes, pollution trail, pseudo-nesting, rust sand, dedicated drops, liquid-gold compatibility, GeckoLib resources, Forge/Fabric registration, language/model resources, build, and JAR inspection each have a task.
- Placeholder scan: no deferred implementation step is required; all paths, IDs, constants, commands, and expected results are specified.
- Type consistency: SpiteArmoredTurtleEntity, SpiteNoduleBlock, SpiteNoduleBlockEntity, SPITE_ARMORED_TURTLE, SPITE_NODULE, RUST_SAND, HEAVY_SPITE_SCUTE, and RESIDUAL_MALICE are used consistently.
- Scope: no liquid-metal fluid, bucket, weapon tempering, or changes to vanilla turtles are included.
