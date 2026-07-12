# Blazing Vein Dagger Implementation Plan

> For agentic workers: REQUIRED SUB-SKILL: Use superpowers:executing-plans or superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Add spd:blazing_vein_dagger, a fireproof 5-damage, 2.1-speed sword with per-stack Swift Edge state, Searing Pulse proc, sprint dodge, no sweep attack, a GeckoLib item model, and a crafting recipe.

**Architecture:** Keep combat state and item behavior in common. Extract state transitions into a pure Java type so timing rules are unit-tested without a running Minecraft server. Keep loader-specific item renderer plumbing in fabric and forge: Fabric supplies GeckoLib RenderProvider, while Forge supplies IClientItemExtensions; both use the same common GeckoLib model and renderer.

**Tech Stack:** Java 17, Minecraft 1.20.1, Architectury 9.2.14, GeckoLib 4.4.9, Fabric, Forge 47.4.20, JUnit 5, Gradle 8.12.1.

---

## File Map

New common code:

- common/src/main/java/alku/spd/item/BlazingVeinDaggerState.java: pure state transition and attack-speed multiplier calculation.
- common/src/main/java/alku/spd/item/BlazingVeinDaggerItem.java: sword behavior, NBT persistence, Searing Pulse proc, instant-slash damage, repair rule, and main-hand speed modifier lifecycle.
- common/src/main/java/alku/spd/platform/SpdPlatform.java: ExpectPlatform hooks that isolate Fabric-only renderer-provider calls from common code.
- common/src/main/java/alku/spd/client/model/BlazingVeinDaggerModel.java: GeckoLib resource locations.
- common/src/main/java/alku/spd/client/renderer/BlazingVeinDaggerRenderer.java: shared GeckoLib item renderer.
- common/src/test/java/alku/spd/item/BlazingVeinDaggerStateTest.java: state-machine tests.

New loader code:

- fabric/src/main/java/alku/spd/platform/SpdPlatformImpl.java: Fabric RenderProvider and GeoItem.makeRenderer bridge.
- forge/src/main/java/alku/spd/platform/SpdPlatformImpl.java: Forge no-op counterpart for Fabric-only renderer hooks.

New resources:

- common/src/main/resources/assets/spd/geo/blazing_vein_dagger.geo.json
- common/src/main/resources/assets/spd/textures/item/blazing_vein_dagger.png
- common/src/main/resources/assets/spd/models/item/blazing_vein_dagger.json
- common/src/main/resources/data/spd/recipes/blazing_vein_dagger.json

Modified code and resources:

- common/src/main/java/alku/spd/registry/SpdItems.java
- common/src/main/java/alku/spd/registry/SpdCreativeTabs.java
- common/src/main/java/alku/spd/mixin/LivingEntityMixin.java
- common/src/main/java/alku/spd/mixin/PlayerMixin.java
- common/src/main/java/alku/spd/effect/SearingPulseEffect.java
- forge/src/main/java/alku/spd/forge/client/SpdForgeClient.java
- common/src/main/resources/assets/spd/lang/zh_cn.json
- common/src/main/resources/assets/spd/lang/en_us.json

### Task 1: Define and test Swift Edge state transitions

**Files:**
- Create: common/src/test/java/alku/spd/item/BlazingVeinDaggerStateTest.java
- Create: common/src/main/java/alku/spd/item/BlazingVeinDaggerState.java

- [ ] **Step 1: Write the failing state-transition tests.**

~~~java
@Test
void timeoutStartsAFreshSwiftEdgeChain() {
    BlazingVeinDaggerState.HitResult result = BlazingVeinDaggerState.onHit(3, 100L, 117L);
    assertEquals(1, result.layers());
    assertFalse(result.instantSlash());
}

@Test
void fifthStoredLayerMakesTheNextHitAnInstantSlash() {
    BlazingVeinDaggerState.HitResult result = BlazingVeinDaggerState.onHit(5, 100L, 116L);
    assertEquals(0, result.layers());
    assertTrue(result.instantSlash());
}

@Test
void fiveLayersUseMultiplicativeAttackSpeed() {
    assertEquals(Math.pow(1.04D, 5), BlazingVeinDaggerState.attackSpeedMultiplier(5), 1.0E-9D);
}
~~~

- [ ] **Step 2: Run the focused test and verify it fails because BlazingVeinDaggerState does not exist.**

~~~powershell
gradle :common:test --tests alku.spd.item.BlazingVeinDaggerStateTest --console=plain
~~~

Expected: compilation fails with an unresolved BlazingVeinDaggerState reference.

- [ ] **Step 3: Add the minimal pure state class.**

~~~java
public final class BlazingVeinDaggerState {
    public static final int MAX_LAYERS = 5;
    public static final long COMBO_TIMEOUT_TICKS = 16L;

    public static HitResult onHit(int layers, long lastHitTick, long currentTick) {
        int activeLayers = currentTick - lastHitTick > COMBO_TIMEOUT_TICKS ? 0 : clamp(layers);
        return activeLayers >= MAX_LAYERS
                ? new HitResult(0, true)
                : new HitResult(activeLayers + 1, false);
    }

    public static double attackSpeedMultiplier(int layers) {
        return Math.pow(1.04D, clamp(layers));
    }

    public record HitResult(int layers, boolean instantSlash) { }
}
~~~

Keep clamp private and constrain values to 0..MAX_LAYERS.

- [ ] **Step 4: Re-run the focused test and then the full common test task.**

~~~powershell
gradle :common:test --tests alku.spd.item.BlazingVeinDaggerStateTest --console=plain
gradle :common:test --console=plain
~~~

Expected: both commands succeed.

### Task 2: Implement the common dagger behavior

**Files:**
- Create: common/src/main/java/alku/spd/item/BlazingVeinDaggerItem.java
- Modify: common/src/main/java/alku/spd/registry/SpdItems.java
- Modify: common/src/main/java/alku/spd/effect/SearingPulseEffect.java

- [ ] **Step 1: Add a failing clamp test before item code.**

~~~java
@Test
void malformedStoredLayersCannotExceedTheInstantSlashThreshold() {
    BlazingVeinDaggerState.HitResult result = BlazingVeinDaggerState.onHit(99, 40L, 41L);
    assertTrue(result.instantSlash());
    assertEquals(0, result.layers());
}
~~~

- [ ] **Step 2: Verify it fails, implement the clamp, and re-run the focused test.**

~~~powershell
gradle :common:test --tests alku.spd.item.BlazingVeinDaggerStateTest --console=plain
~~~

Expected before implementation: the test fails if the stored value is not clamped. Expected after implementation: all state tests pass.

- [ ] **Step 3: Create BlazingVeinDaggerItem with normal sword semantics.**

The constructor calls super(tier, 3, -1.9F, properties.durability(1900).fireResistant()), yielding final attack damage 5 with the existing BLAZING_EMBER_TIER attack bonus of 2 and attack speed 2.1 from Minecraft's base speed 4.0. It implements GeoItem, constructs its cache with GeckoLibUtil.createInstanceCache(this), registers no animation controllers, and accepts only SpdItems.BLAZING_CARBON_STEEL_INGOT for repairs.

Store BlazingVeinDaggerLayers and BlazingVeinDaggerLastHit on the individual ItemStack. On a server-side hurtEnemy:

~~~java
BlazingVeinDaggerState.HitResult result = BlazingVeinDaggerState.onHit(layers, lastHit, gameTime);
setLayers(stack, result.layers());
tag.putLong(LAST_HIT_TAG, gameTime);
if (result.instantSlash()) {
    target.hurt(level.damageSources().playerAttack(player), calculateInstantSlashDamage(player, stack, target));
}
if (player.getRandom().nextFloat() < 0.30F) {
    target.addEffect(new MobEffectInstance(SpdEffects.SEARING_PULSE.get(), 40, 0), player);
}
~~~

calculateInstantSlashDamage uses 70% of the player's attack attribute plus standard item enchantment damage bonus. Do not call hurtEnemy manually for the second strike, so it cannot recurse, add a second proc, or consume another durability point.

- [ ] **Step 4: Apply and remove the temporary attack-speed modifier while the dagger is in the main hand.**

Use a fixed UUID and AttributeModifier.Operation.MULTIPLY_TOTAL; its amount is BlazingVeinDaggerState.attackSpeedMultiplier(layers) - 1.0D. In inventoryTick, clear stale layers once gameTime - lastHit > 16, then update the modifier for the selected main-hand stack. When no dagger remains in the main hand, remove the fixed UUID modifier. Applying immediately in hurtEnemy keeps the next attack's cooldown authoritative.

- [ ] **Step 5: Change SearingPulseEffect to deal 0.5 fire damage per moving one-second tick.**

~~~java
entity.hurt(entity.damageSources().onFire(), 0.5F);
~~~

- [ ] **Step 6: Register the item and compile common.**

~~~java
public static final RegistrySupplier<Item> BLAZING_VEIN_DAGGER = ITEMS.register("blazing_vein_dagger", () ->
        new BlazingVeinDaggerItem(BLAZING_EMBER_TIER, new Item.Properties()));
~~~

~~~powershell
gradle :common:compileJava :common:test --console=plain
~~~

Expected: the item compiles and all state tests are green.

### Task 3: Wire combat hooks without changing other weapons

**Files:**
- Modify: common/src/main/java/alku/spd/mixin/PlayerMixin.java
- Modify: common/src/main/java/alku/spd/mixin/LivingEntityMixin.java

- [ ] **Step 1: Add the dagger helper import and cancel only dagger sweep attacks.**

Inject at the head of Player.sweepAttack():

~~~java
@Inject(method = "sweepAttack", at = @At("HEAD"), cancellable = true)
private void spd$disableDaggerSweep(CallbackInfo ci) {
    if (BlazingVeinDaggerItem.isBlazingVeinDagger(((Player) (Object) this).getMainHandItem())) {
        ci.cancel();
    }
}
~~~

This preserves sword enchantment compatibility while preventing the damage-area call itself.

- [ ] **Step 2: Add a server-authoritative sprint dodge at the start of LivingEntity.hurt.**

Before difficulty damage immunity handling, cancel only direct living-entity melee hits:

~~~java
if (!level.isClientSide()
        && target instanceof Player player
        && player.isSprinting()
        && BlazingVeinDaggerItem.isBlazingVeinDagger(player.getMainHandItem())
        && source.getEntity() instanceof LivingEntity attacker
        && source.getDirectEntity() == attacker
        && attacker != target
        && target.getRandom().nextFloat() < 0.10F) {
    cir.setReturnValue(false);
    return;
}
~~~

This excludes projectiles, environmental damage, and non-direct indirect damage sources.

- [ ] **Step 3: Compile common and inspect Mixin configuration.**

~~~powershell
gradle :common:compileJava --console=plain
gradle :common:test --console=plain
~~~

Expected: no new mixin configuration entry is needed because both hooks remain in already-registered mixin classes.

### Task 4: Add GeckoLib model, renderer bridges, and game resources

**Files:**
- Create: common/src/main/java/alku/spd/client/model/BlazingVeinDaggerModel.java
- Create: common/src/main/java/alku/spd/client/renderer/BlazingVeinDaggerRenderer.java
- Create: common/src/main/java/alku/spd/platform/SpdPlatform.java
- Create: fabric/src/main/java/alku/spd/platform/SpdPlatformImpl.java
- Create: forge/src/main/java/alku/spd/platform/SpdPlatformImpl.java
- Modify: forge/src/main/java/alku/spd/forge/client/SpdForgeClient.java
- Create: common/src/main/resources/assets/spd/geo/blazing_vein_dagger.geo.json
- Create: common/src/main/resources/assets/spd/textures/item/blazing_vein_dagger.png
- Create: common/src/main/resources/assets/spd/models/item/blazing_vein_dagger.json
- Create: common/src/main/resources/data/spd/recipes/blazing_vein_dagger.json

- [ ] **Step 1: Convert the supplied Bedrock geometry to a GeckoLib-compatible resource.**

Copy the supplied geometry and change only format_version from 1.21.110 to 1.12.0; preserve the 32x32 UV coordinates, bone pivot, cubes, and rotations. Copy the supplied PNG to textures/item/blazing_vein_dagger.png. The model class returns:

~~~java
new ResourceLocation(Spd.MOD_ID, "geo/blazing_vein_dagger.geo.json");
new ResourceLocation(Spd.MOD_ID, "textures/item/blazing_vein_dagger.png");
~~~

getAnimationResource returns null because the item has no attack animation.

- [ ] **Step 2: Add BlazingVeinDaggerRenderer.**

~~~java
public final class BlazingVeinDaggerRenderer extends GeoItemRenderer<BlazingVeinDaggerItem> {
    public BlazingVeinDaggerRenderer() {
        super(new BlazingVeinDaggerModel());
    }
}
~~~

- [ ] **Step 3: Keep Fabric-only RenderProvider code out of common.**

SpdPlatform declares two ExpectPlatform methods using only java.util.function types. Fabric returns GeoItem.makeRenderer(item) and supplies a RenderProvider whose getCustomRenderer() returns one cached BlazingVeinDaggerRenderer. Forge returns an empty supplier and no-ops the provider callback. BlazingVeinDaggerItem delegates its Fabric-only createRenderer and getRenderProvider requirements to these methods.

- [ ] **Step 4: Register Forge custom item rendering.**

Subscribe to RegisterClientExtensionsEvent in SpdForgeClient and call event.registerItem(...) for SpdItems.BLAZING_VEIN_DAGGER.get(). The IClientItemExtensions caches and returns BlazingVeinDaggerRenderer from getCustomRenderer().

- [ ] **Step 5: Add the item model and recipe.**

Use builtin/entity as the item model parent so the custom renderer is invoked in GUI, ground, first-person, and third-person contexts. Preserve the supplied knife display transforms in the model JSON. Add a shaped recipe with one spd:blazing_carbon_steel_ingot and one spd:ember_handle:

~~~json
{
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "pattern": ["I", "H"],
  "key": {
    "I": { "item": "spd:blazing_carbon_steel_ingot" },
    "H": { "item": "spd:ember_handle" }
  },
  "result": { "item": "spd:blazing_vein_dagger" }
}
~~~

- [ ] **Step 6: Add creative-tab and language entries, then compile both loaders.**

Add the dagger after the spear in SpdCreativeTabs, add item.spd.blazing_vein_dagger as 炽脉疾锋刀 and Blazing Vein Swiftblade, and run:

~~~powershell
gradle :fabric:compileJava :forge:compileJava --console=plain
~~~

Expected: both loader modules compile without a cross-loader client class linkage failure.

### Task 5: Verify artifacts, commit, and push

**Files:**
- Stage only the dagger plan, source, resources, and test files above.
- Do not stage .superpowers, CLAUDE.md, build outputs, or run directories.

- [ ] **Step 1: Run focused and full verification.**

~~~powershell
gradle :common:test --console=plain
gradle build --console=plain
git diff --check
~~~

Expected: all tests pass, all modules build, and the diff has no whitespace errors.

- [ ] **Step 2: Inspect the built JARs for the new runtime resources.**

~~~powershell
$forgeJar = Get-ChildItem forge\build\libs -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$fabricJar = Get-ChildItem fabric\build\libs -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
jar tf $forgeJar.FullName | Select-String 'blazing_vein_dagger'
jar tf $fabricJar.FullName | Select-String 'blazing_vein_dagger'
~~~

Expected: each JAR contains the model, texture, item model, and recipe.

- [ ] **Step 3: Commit and push with the repository's required format.**

~~~powershell
git add docs/superpowers/plans/2026-07-12-blazing-vein-dagger.md common fabric forge
git commit -m "-（添加炽脉疾锋刀）"
git push origin master
git ls-remote origin refs/heads/master
~~~

Expected: local HEAD equals remote master and .superpowers plus CLAUDE.md stay untracked.

## Self-Review

- Scope coverage: base attributes, fire resistance, repair material, enchantment-compatible sword inheritance, no sweep attack, Swift Edge timing and speed multiplier, instant slash, Searing Pulse proc, sprint dodge, custom model rendering, both loaders, recipe, language, creative tab, build, and remote push are each assigned to a task.
- Cross-loader boundary: no Forge or Fabric API appears in common gameplay code; only platform implementations and SpdForgeClient refer to loader APIs.
- TDD coverage: combo timing, maximum layer, instant-slash trigger, and multiplicative speed behavior are proven by red-green unit tests before item code is introduced.

