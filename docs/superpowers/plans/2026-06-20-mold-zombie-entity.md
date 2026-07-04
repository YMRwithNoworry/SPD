# Mold Zombie Entity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a GeckoLib animated mold zombie entity to the Minecraft 1.20.1 Architectury mod, with common-first entity logic and Fabric/Forge platform adapters.

**Architecture:** Entity registration, entity behavior, animation state selection, and shared client model/renderer classes live in `common`. Fabric and Forge modules only add platform-specific attribute registration, renderer registration, GeckoLib initialization, and biome spawn injection. Resources are exported from the `.bbmodel` source into standard GeckoLib runtime paths under `common/src/main/resources/assets/spd`.

**Tech Stack:** Minecraft 1.20.1, Java 17, Architectury API 9.2.14, Fabric API, Forge 47, GeckoLib 4 for Fabric/Forge, official Mojang mappings.

---

## File Structure

- Modify `gradle.properties`: add `geckolib_version`.
- Modify `build.gradle`: add GeckoLib maven repository for all subprojects.
- Modify `common/build.gradle`: add the common GeckoLib dependency used by shared source.
- Modify `fabric/build.gradle`: add Fabric GeckoLib dependency.
- Modify `forge/build.gradle`: add Forge GeckoLib dependency.
- Modify `fabric/src/main/resources/fabric.mod.json`: declare GeckoLib dependency.
- Modify `forge/src/main/resources/META-INF/mods.toml`: declare GeckoLib dependency.
- Modify `common/src/main/java/alku/spd/Spd.java`: initialize registries.
- Create `common/src/main/java/alku/spd/registry/SpdEntities.java`: entity type registration.
- Create `common/src/main/java/alku/spd/registry/SpdItems.java`: spawn egg registration.
- Create `common/src/main/java/alku/spd/entity/MoldZombieEntity.java`: Zombie subclass and GeckoLib animation controller.
- Create `common/src/main/java/alku/spd/client/model/MoldZombieModel.java`: GeckoLib resource path provider.
- Create `common/src/main/java/alku/spd/client/renderer/MoldZombieRenderer.java`: GeckoLib renderer.
- Modify `fabric/src/main/java/alku/spd/fabric/SpdFabric.java`: initialize GeckoLib, attributes, and biome spawning.
- Modify `fabric/src/main/java/alku/spd/fabric/client/SpdFabricClient.java`: register renderer.
- Modify `forge/src/main/java/alku/spd/forge/SpdForge.java`: initialize GeckoLib, attributes, renderer, and biome spawning.
- Create `common/src/main/resources/assets/spd/geo/mold_zombie.geo.json`: runtime model exported from `.bbmodel`.
- Create `common/src/main/resources/assets/spd/animations/mold_zombie.animation.json`: runtime animations exported from `.bbmodel`.
- Create `common/src/main/resources/assets/spd/textures/entity/mold_zombie.png`: runtime texture extracted from `.bbmodel`.
- Create `common/src/main/resources/assets/spd/lang/en_us.json`: English display names.
- Create `common/src/main/resources/assets/spd/lang/zh_cn.json`: Chinese display names.

---

### Task 1: Add GeckoLib Dependencies

**Files:**
- Modify: `gradle.properties`
- Modify: `build.gradle`
- Modify: `common/build.gradle`
- Modify: `fabric/build.gradle`
- Modify: `forge/build.gradle`
- Modify: `fabric/src/main/resources/fabric.mod.json`
- Modify: `forge/src/main/resources/META-INF/mods.toml`

- [ ] **Step 1: Add the GeckoLib version property**

Append this to `gradle.properties` under the dependency section:

```properties
geckolib_version=4.4.9
```

- [ ] **Step 2: Add the GeckoLib Maven repository**

In root `build.gradle`, inside `subprojects { repositories { ... } }`, add:

```gradle
maven { url "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/" }
```

The resulting block should include:

```gradle
repositories {
    maven { url "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/" }
}
```

- [ ] **Step 3: Add the common GeckoLib dependency**

In `common/build.gradle`, add this dependency after Architectury:

```gradle
modImplementation "software.bernie.geckolib:geckolib-common-1.20.1:$rootProject.geckolib_version"
```

- [ ] **Step 4: Add the Fabric GeckoLib dependency**

In `fabric/build.gradle`, add:

```gradle
modImplementation "software.bernie.geckolib:geckolib-fabric-1.20.1:$rootProject.geckolib_version"
```

- [ ] **Step 5: Add the Forge GeckoLib dependency**

In `forge/build.gradle`, add:

```gradle
modImplementation "software.bernie.geckolib:geckolib-forge-1.20.1:$rootProject.geckolib_version"
```

- [ ] **Step 6: Declare Fabric runtime dependency**

In `fabric/src/main/resources/fabric.mod.json`, add GeckoLib to `depends`:

```json
"geckolib": ">=4.4.9"
```

The `depends` object should include:

```json
"architectury": ">=9.2.14",
"fabric-api": "*",
"geckolib": ">=4.4.9"
```

- [ ] **Step 7: Declare Forge runtime dependency**

In `forge/src/main/resources/META-INF/mods.toml`, append:

```toml
[[dependencies.spd]]
modId = "geckolib"
mandatory = true
versionRange = "[4.4.9,)"
ordering = "AFTER"
side = "BOTH"
```

- [ ] **Step 8: Verify dependency resolution**

Run:

```bash
./gradlew :common:compileJava --refresh-dependencies
```

Expected: Gradle resolves GeckoLib and compiles or fails only on not-yet-created entity code if later tasks have partially started.

---

### Task 2: Add Common Registries

**Files:**
- Modify: `common/src/main/java/alku/spd/Spd.java`
- Create: `common/src/main/java/alku/spd/registry/SpdEntities.java`
- Create: `common/src/main/java/alku/spd/registry/SpdItems.java`

- [ ] **Step 1: Create entity registry**

Create `common/src/main/java/alku/spd/registry/SpdEntities.java`:

```java
package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.entity.MoldZombieEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class SpdEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Spd.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<MoldZombieEntity>> MOLD_ZOMBIE = ENTITIES.register("mold_zombie", () ->
            EntityType.Builder.of(MoldZombieEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build("mold_zombie"));

    private SpdEntities() {
    }

    public static void register() {
        ENTITIES.register();
    }
}
```

- [ ] **Step 2: Create item registry**

Create `common/src/main/java/alku/spd/registry/SpdItems.java`:

```java
package alku.spd.registry;

import alku.spd.Spd;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class SpdItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Spd.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> MOLD_ZOMBIE_SPAWN_EGG = ITEMS.register("mold_zombie_spawn_egg", () ->
            new SpawnEggItem(SpdEntities.MOLD_ZOMBIE.get(), 0x5A6B45, 0x2E3A27, new Item.Properties().arch$tab(CreativeModeTabs.SPAWN_EGGS)));

    private SpdItems() {
    }

    public static void register() {
        ITEMS.register();
    }
}
```

- [ ] **Step 3: Initialize registries from common init**

Modify `common/src/main/java/alku/spd/Spd.java`:

```java
package alku.spd;

import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdItems;

public final class Spd {
    public static final String MOD_ID = "spd";

    public static void init() {
        SpdEntities.register();
        SpdItems.register();
    }
}
```

- [ ] **Step 4: Compile common to surface missing entity class**

Run:

```bash
./gradlew :common:compileJava
```

Expected: compilation fails because `alku.spd.entity.MoldZombieEntity` does not exist yet. This confirms the registry code is being compiled.

---

### Task 3: Implement MoldZombieEntity

**Files:**
- Create: `common/src/main/java/alku/spd/entity/MoldZombieEntity.java`

- [ ] **Step 1: Create the entity class**

Create `common/src/main/java/alku/spd/entity/MoldZombieEntity.java`:

```java
package alku.spd.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MoldZombieEntity extends Zombie implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.mold_zombie.idle");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.mold_zombie.run");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.mold_zombie.attack");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MoldZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.276D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (this.swinging) {
                state.setAndContinue(ATTACK);
                return PlayState.CONTINUE;
            }

            if (state.isMoving()) {
                state.setAndContinue(RUN);
                return PlayState.CONTINUE;
            }

            state.setAndContinue(IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
```

- [ ] **Step 2: Compile common**

Run:

```bash
./gradlew :common:compileJava
```

Expected: common compiles, or GeckoLib API mismatches are reported with exact import/signature errors to fix against the resolved version.

---

### Task 4: Add Common GeckoLib Client Classes

**Files:**
- Create: `common/src/main/java/alku/spd/client/model/MoldZombieModel.java`
- Create: `common/src/main/java/alku/spd/client/renderer/MoldZombieRenderer.java`

- [ ] **Step 1: Create model class**

Create `common/src/main/java/alku/spd/client/model/MoldZombieModel.java`:

```java
package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.MoldZombieEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MoldZombieModel extends GeoModel<MoldZombieEntity> {
    @Override
    public ResourceLocation getModelResource(MoldZombieEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/mold_zombie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MoldZombieEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/entity/mold_zombie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MoldZombieEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/mold_zombie.animation.json");
    }
}
```

- [ ] **Step 2: Create renderer class**

Create `common/src/main/java/alku/spd/client/renderer/MoldZombieRenderer.java`:

```java
package alku.spd.client.renderer;

import alku.spd.client.model.MoldZombieModel;
import alku.spd.entity.MoldZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MoldZombieRenderer extends GeoEntityRenderer<MoldZombieEntity> {
    public MoldZombieRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MoldZombieModel());
        this.shadowRadius = 0.5F;
    }
}
```

- [ ] **Step 3: Compile common**

Run:

```bash
./gradlew :common:compileJava
```

Expected: common compiles with client classes present in named source set.

---

### Task 5: Export GeckoLib Runtime Resources

**Files:**
- Read: `杂物/霉染僵尸1 (1) (1).bbmodel`
- Create: `common/src/main/resources/assets/spd/geo/mold_zombie.geo.json`
- Create: `common/src/main/resources/assets/spd/animations/mold_zombie.animation.json`
- Create: `common/src/main/resources/assets/spd/textures/entity/mold_zombie.png`
- Create: `common/src/main/resources/assets/spd/lang/en_us.json`
- Create: `common/src/main/resources/assets/spd/lang/zh_cn.json`

- [ ] **Step 1: Extract texture from bbmodel**

Use a script that reads the first texture `source` field from `杂物/霉染僵尸1 (1) (1).bbmodel`, decodes the base64 PNG, and writes:

```text
common/src/main/resources/assets/spd/textures/entity/mold_zombie.png
```

- [ ] **Step 2: Export model geometry**

Transform the `.bbmodel` groups and elements into GeckoLib runtime geometry JSON. The output file must use this top-level shape:

```json
{
  "format_version": "1.12.0",
  "minecraft:geometry": [
    {
      "description": {
        "identifier": "geometry.spd.mold_zombie",
        "texture_width": 128,
        "texture_height": 128,
        "visible_bounds_width": 1,
        "visible_bounds_height": 1,
        "visible_bounds_offset": [0, 0, 0]
      },
      "bones": []
    }
  ]
}
```

Each Blockbench group becomes one bone named exactly `Body`, `Head`, `RightArm`, `LeftArm`, `RightLeg`, or `LeftLeg`. Each cube child becomes one cube under its parent bone with `origin`, `pivot`, `rotation`, `size`, and `uv` values converted from Blockbench coordinates to Bedrock/GeckoLib geometry coordinates.

- [ ] **Step 3: Export animation JSON**

Transform `.bbmodel` animations into GeckoLib runtime animation JSON at:

```text
common/src/main/resources/assets/spd/animations/mold_zombie.animation.json
```

The output must include:

```json
{
  "format_version": "1.8.0",
  "animations": {
    "animation.mold_zombie.idle": {
      "loop": true,
      "animation_length": 2.0,
      "bones": {}
    },
    "animation.mold_zombie.run": {
      "loop": true,
      "animation_length": 0.8,
      "bones": {}
    },
    "animation.mold_zombie.attack": {
      "loop": false,
      "animation_length": 1.05,
      "bones": {}
    }
  }
}
```

For each animation, map animator UUIDs back to their bone `name`, and map keyframe channels:

```json
"0.0": { "post": [0.0, 0.0, 0.0] }
```

Use the channel names `rotation` and `position`. Preserve the authored keyframe times and values.

- [ ] **Step 4: Add language files**

Create `common/src/main/resources/assets/spd/lang/en_us.json`:

```json
{
  "entity.spd.mold_zombie": "Mold Zombie",
  "item.spd.mold_zombie_spawn_egg": "Mold Zombie Spawn Egg"
}
```

Create `common/src/main/resources/assets/spd/lang/zh_cn.json`:

```json
{
  "entity.spd.mold_zombie": "霉染僵尸",
  "item.spd.mold_zombie_spawn_egg": "霉染僵尸刷怪蛋"
}
```

- [ ] **Step 5: Validate resource JSON**

Run:

```bash
python -m json.tool common/src/main/resources/assets/spd/geo/mold_zombie.geo.json > /tmp/mold_zombie_geo_check.json
python -m json.tool common/src/main/resources/assets/spd/animations/mold_zombie.animation.json > /tmp/mold_zombie_animation_check.json
python -m json.tool common/src/main/resources/assets/spd/lang/en_us.json > /tmp/mold_zombie_en_check.json
python -m json.tool common/src/main/resources/assets/spd/lang/zh_cn.json > /tmp/mold_zombie_zh_check.json
```

Expected: all commands exit successfully.

---

### Task 6: Add Fabric Platform Wiring

**Files:**
- Modify: `fabric/src/main/java/alku/spd/fabric/SpdFabric.java`
- Modify: `fabric/src/main/java/alku/spd/fabric/client/SpdFabricClient.java`

- [ ] **Step 1: Register Fabric entity attributes and spawning**

Modify `fabric/src/main/java/alku/spd/fabric/SpdFabric.java`:

```java
package alku.spd.fabric;

import alku.spd.Spd;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.registry.SpdEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.MobCategory;
import software.bernie.geckolib.GeckoLib;

public final class SpdFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        GeckoLib.initialize();
        Spd.init();
        FabricDefaultAttributeRegistry.register(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieEntity.createAttributes());
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, SpdEntities.MOLD_ZOMBIE.get(), 80, 1, 4);
    }
}
```

- [ ] **Step 2: Register Fabric renderer**

Modify `fabric/src/main/java/alku/spd/fabric/client/SpdFabricClient.java`:

```java
package alku.spd.fabric.client;

import alku.spd.client.renderer.MoldZombieRenderer;
import alku.spd.registry.SpdEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class SpdFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieRenderer::new);
    }
}
```

- [ ] **Step 3: Compile Fabric**

Run:

```bash
./gradlew :fabric:compileJava
```

Expected: Fabric code compiles.

---

### Task 7: Add Forge Platform Wiring

**Files:**
- Modify: `forge/src/main/java/alku/spd/forge/SpdForge.java`
- Create: `forge/src/main/resources/data/spd/forge/biome_modifier/add_mold_zombie.json`

- [ ] **Step 1: Register Forge attributes and renderer**

Modify `forge/src/main/java/alku/spd/forge/SpdForge.java`:

```java
package alku.spd.forge;

import alku.spd.Spd;
import alku.spd.client.renderer.MoldZombieRenderer;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.registry.SpdEntities;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

@Mod(Spd.MOD_ID)
public final class SpdForge {
    public SpdForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(Spd.MOD_ID, modEventBus);
        GeckoLib.initialize();
        Spd.init();
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerRenderers);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieEntity.createAttributes().build());
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieRenderer::new);
    }
}
```

- [ ] **Step 2: Add Forge biome modifier JSON**

Create `forge/src/main/resources/data/spd/forge/biome_modifier/add_mold_zombie.json`:

```json
{
  "type": "forge:add_spawns",
  "biomes": "#minecraft:is_overworld",
  "spawners": {
    "type": "spd:mold_zombie",
    "weight": 80,
    "minCount": 1,
    "maxCount": 4
  }
}
```

- [ ] **Step 3: Compile Forge**

Run:

```bash
./gradlew :forge:compileJava
```

Expected: Forge code compiles.

---

### Task 8: Final Build and Runtime Checks

**Files:**
- Verify all changed files.

- [ ] **Step 1: Build all modules**

Run:

```bash
./gradlew build
```

Expected: build succeeds for common, Fabric, and Forge.

- [ ] **Step 2: Check generated resources are included**

Run:

```bash
./gradlew :fabric:processResources :forge:processResources
```

Expected: no JSON expansion errors.

- [ ] **Step 3: Manual Fabric runtime check**

Run Fabric client from IDE or Gradle if configured. In game, run:

```text
/summon spd:mold_zombie
```

Expected: entity appears with the mold zombie model and idle animation.

- [ ] **Step 4: Manual Forge runtime check**

Run Forge client from IDE or Gradle if configured. In game, run:

```text
/summon spd:mold_zombie
```

Expected: entity appears with the mold zombie model and idle animation.

- [ ] **Step 5: Manual animation check**

In either loader:

```text
/gamemode survival
/summon spd:mold_zombie ~ ~ ~
```

Expected: idle animation when stationary, run animation while pathing, attack animation when swinging at the player.

- [ ] **Step 6: Manual spawn check**

Wait in a valid dark overworld hostile spawning area or use a mob spawning test world.

Expected: mold zombies can appear as hostile mobs with group size 1-4 and do not replace all zombie spawns.

---

## Self-Review Notes

- Spec coverage: dependencies, common entity logic, GeckoLib animations, Fabric/Forge adapters, natural spawning, resources, and verification are covered.
- Placeholder scan: no vague future-work markers remain.
- Type consistency: `MoldZombieEntity`, `SpdEntities.MOLD_ZOMBIE`, `MoldZombieModel`, and `MoldZombieRenderer` names are consistent across tasks.
- Commit handling: no commit steps are included because the current repository instructions say not to commit unless explicitly requested.
