# Repository Guidelines

## Project Structure & Module Organization

This is an Architectury-based Minecraft mod for `spd`, targeting Minecraft `1.20.1` with shared code plus Fabric and Forge loaders.

- `common/`: shared Java sources, registries, entity logic, client model/renderer code, mixins, language files, GeckoLib models, textures, and animations.
- `fabric/`: Fabric entrypoints, loader metadata, and Fabric-specific build configuration.
- `forge/`: Forge entrypoints, `mods.toml`, Forge-specific biome modifiers, and Forge build configuration.
- `docs/superpowers/`: design specs and implementation plans.
- `杂物/`: source Blockbench `.bbmodel` files and generated reference assets. Treat these as art/source materials, not packaged runtime resources unless copied into `common/src/main/resources`.

## Build, Test, and Development Commands

This repository has Gradle build files but no checked-in `gradlew.bat`; use an installed `gradle` command or add a wrapper before relying on wrapper commands.

- `gradle build --console=plain`: builds all modules and remapped jars.
- `gradle :common:build --console=plain`: compiles shared code and resources.
- `gradle :fabric:build --console=plain`: builds the Fabric jar.
- `gradle :forge:build --console=plain`: builds the Forge jar.
- `gradle :fabric:runClient` / `gradle :forge:runClient`: launch loader-specific Minecraft clients when Loom run configs are available.

## Coding Style & Naming Conventions

Use Java 17. Match the existing package root `alku.spd`. Keep shared gameplay behavior in `common`, and restrict loader hooks to `fabric` or `forge`. Use 4-space indentation, braces on the same line, and clear class names such as `SpdEntities`, `MoldZombieEntity`, and `MoldZombieRenderer`. Resource IDs should be lowercase snake case, for example `mold_zombie`.

## Testing Guidelines

There is no dedicated test suite yet. At minimum, run the relevant Gradle build before submitting changes. For gameplay or renderer work, verify in the matching loader client and check logs for registration, asset, and GeckoLib animation errors. Name future tests after the behavior under test, not implementation details.

## Commit & Pull Request Guidelines

No commit history is available in this checkout, so use concise imperative commits such as `Add mold zombie renderer assets`. Pull requests should describe the changed module, list validation commands or manual client checks, link related docs in `docs/superpowers/`, and include screenshots or short clips for model, texture, or animation changes.

## Agent-Specific Instructions

Do not overwrite `AGENTS.md` if it already exists. Keep future edits scoped to the requested task, preserve loader separation, and avoid moving generated assets into packaged resources unless the change explicitly requires it.
