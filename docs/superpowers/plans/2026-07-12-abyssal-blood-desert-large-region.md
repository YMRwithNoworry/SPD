# 血漠大区域生成 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make `spd:abyssal_blood_desert` generate as a large independent hot, dry blood-sand desert while leaving vanilla desert generation unchanged.

**Architecture:** Keep the existing shared `SpdTerraBlender` registration and blood-desert biome resource. Replace the region's exact vanilla-desert parameter copying with one explicit broad climate parameter point, and keep the surface rule gated by the SPD biome key so only blood-desert terrain receives blood sand.

**Tech Stack:** Minecraft 1.20.1, Architectury common module, TerraBlender 3.0.1.10, Java 17, JSON worldgen resources.

---

### Task 1: Prove the current region mapping is the old implementation

**Files:**
- Test: temporary PowerShell assertions against `common/src/main/java/alku/spd/world/AbyssalBloodDesertRegion.java` and `common/src/main/java/alku/spd/world/SpdTerraBlender.java`

- [ ] **Step 1: Run the failing static assertion**

Run from `E:\代码\MC模组\SPD`:

```powershell
$region = Get-Content -Raw -LiteralPath 'common/src/main/java/alku/spd/world/AbyssalBloodDesertRegion.java'
$terra = Get-Content -Raw -LiteralPath 'common/src/main/java/alku/spd/world/SpdTerraBlender.java'
if ($region -match 'addBiomeSimilar') { throw 'Blood desert still copies vanilla desert climate points' }
if ($region -notmatch 'Climate\.Parameter\.span') { throw 'Blood desert has no broad climate parameter range' }
if ($terra -notmatch 'BLOOD_DESERT_REGION_WEIGHT = 10') { throw 'Blood desert region weight is not the planned large-region weight' }
if ($terra -notmatch 'SurfaceRules\.isBiome\(SpdBiomes\.ABYSSAL_BLOOD_DESERT\)') { throw 'Surface rule is not gated to the blood desert biome' }
```

Expected result: the command fails on the existing `addBiomeSimilar` implementation. This proves the assertion detects the missing feature before code changes.

### Task 2: Map a broad independent TerraBlender climate region

**Files:**
- Modify: `common/src/main/java/alku/spd/world/AbyssalBloodDesertRegion.java`
- Modify: `common/src/main/java/alku/spd/world/SpdTerraBlender.java`

- [ ] **Step 1: Replace copied vanilla points with explicit hot/dry ranges**

Use TerraBlender's protected `addBiome` overload and `Climate.Parameter.span` in `AbyssalBloodDesertRegion.addBiomes`:

```java
@Override
public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
    addBiome(
            mapper,
            Climate.Parameter.span(0.55F, 1.0F),
            Climate.Parameter.span(-1.0F, -0.35F),
            Climate.Parameter.span(0.0F, 1.0F),
            Climate.Parameter.span(-1.0F, 1.0F),
            Climate.Parameter.span(-1.0F, 1.0F),
            Climate.Parameter.span(-1.0F, 1.0F),
            0.0F,
            SpdBiomes.ABYSSAL_BLOOD_DESERT);
}
```

The six ranges are temperature, humidity, continentalness, erosion, weirdness, and depth in the TerraBlender method signature. The range is hot and dry, includes broad inland terrain, and leaves cool or wet climates to vanilla biome selection.

- [ ] **Step 2: Increase the region selection weight**

Change `BLOOD_DESERT_REGION_WEIGHT` from `3` to `10` in `SpdTerraBlender`. Keep the existing `spd:abyssal_blood_desert` resource location and registration guard unchanged.

- [ ] **Step 3: Keep the surface rule biome-scoped**

Retain the `SurfaceRules.isBiome(SpdBiomes.ABYSSAL_BLOOD_DESERT)` gate and the floor/under-floor/deep-under-floor blood-sand sequence. Do not add a global rule that checks for vanilla desert or the generic sand block.

### Task 3: Verify source and resources before building

**Files:**
- Test: modified Java source and existing blood-desert JSON resources

- [ ] **Step 1: Run the static green assertion**

Run the assertion from Task 1 again. Expected result: no exception, with `addBiomeSimilar` absent, `Climate.Parameter.span` present, region weight `10`, and the biome-scoped surface rule present.

- [ ] **Step 2: Validate JSON and whitespace**

Run:

```powershell
Get-ChildItem -LiteralPath 'common/src/main/resources' -Recurse -Filter '*.json' | ForEach-Object {
    Get-Content -Raw -LiteralPath $_.FullName | ConvertFrom-Json -AsHashtable | Out-Null
}
git diff --check
```

Expected result: exit code `0`; all resource JSON parses and no whitespace errors are reported.

### Task 4: Build, inspect, deploy, commit, and push

**Files:**
- Output: `forge/build/libs/spd-forge-1.0-SNAPSHOT.jar`
- Output: `fabric/build/libs/spd-fabric-1.0-SNAPSHOT.jar`
- Deploy: `E:/GAME1/MC/pcl2-beta/.minecraft/versions/真斗蛐蛐/mods/spd-forge-1.0-SNAPSHOT.jar`

- [ ] **Step 1: Build both loaders**

Run:

```powershell
& 'C:/tmp/gradle-8121/gradle-8.12.1/bin/gradle.bat' :forge:build :fabric:build --rerun-tasks --console=plain
```

Expected result: `BUILD SUCCESSFUL` for both `:forge:build` and `:fabric:build`.

- [ ] **Step 2: Inspect the Forge jar**

Confirm `jar tf forge/build/libs/spd-forge-1.0-SNAPSHOT.jar` contains `alku/spd/world/AbyssalBloodDesertRegion.class`, `alku/spd/world/SpdTerraBlender.class`, `data/spd/worldgen/biome/abyssal_blood_desert.json`, and the blood-sand block resources.

- [ ] **Step 3: Deploy and compare hashes**

Copy the Forge jar to the configured instance mods directory and compare SHA-256 hashes of source and deployed jars. Expected result: hashes match.

- [ ] **Step 4: Commit only the feature files**

Stage the two modified Java files and this implementation plan, then commit:

```powershell
git add -- 'common/src/main/java/alku/spd/world/AbyssalBloodDesertRegion.java' 'common/src/main/java/alku/spd/world/SpdTerraBlender.java' 'docs/superpowers/plans/2026-07-12-abyssal-blood-desert-large-region.md'
git commit -m '-（扩大血漠群系生成区域）'
```

Leave unrelated untracked `CLAUDE.md` untouched.

- [ ] **Step 5: Push and verify the remote head**

Run:

```powershell
git push git@github.com:YMRwithNoworry/SPD.git master
```

Then compare `git rev-parse HEAD` with `git ls-remote git@github.com:YMRwithNoworry/SPD.git refs/heads/master`. Expected result: both hashes are identical.
