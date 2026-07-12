package alku.spd.fabric;

import alku.spd.Spd;
import alku.spd.entity.AbyssalErodedSilverfishEntity;
import alku.spd.entity.AbyssalFoxEntity;
import alku.spd.entity.AbyssalTurtleEntity;
import alku.spd.entity.AbyssalWolfEntity;
import alku.spd.entity.FalseMotherEntity;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.registry.SpdBiomes;
import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdTags;
import alku.spd.world.SpdTerraBlender;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import software.bernie.geckolib.GeckoLib;

public final class SpdFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        GeckoLib.initialize();
        Spd.init();
        SpdTerraBlender.register();
        FabricDefaultAttributeRegistry.register(SpdEntities.ABYSSAL_ERODED_SILVERFISH.get(), AbyssalErodedSilverfishEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SpdEntities.FALSE_MOTHER.get(), FalseMotherEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SpdEntities.ABYSSAL_FOX.get(), AbyssalFoxEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SpdEntities.ABYSSAL_WOLF.get(), AbyssalWolfEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SpdEntities.ABYSSAL_TURTLE.get(), AbyssalTurtleEntity.createAttributes());
        SpawnPlacements.register(
                SpdEntities.ABYSSAL_FOX.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbyssalFoxEntity::checkSpawnRules);
        SpawnPlacements.register(
                SpdEntities.ABYSSAL_WOLF.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbyssalWolfEntity::checkSpawnRules);
        SpawnPlacements.register(
                SpdEntities.ABYSSAL_TURTLE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbyssalTurtleEntity::checkSpawnRules);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, SpdEntities.ABYSSAL_ERODED_SILVERFISH.get(), 45, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, SpdEntities.MOLD_ZOMBIE.get(), 80, 1, 4);
        BiomeModifications.addSpawn(BiomeSelectors.tag(SpdTags.ABYSSAL_FOX_SPAWNS), MobCategory.CREATURE, SpdEntities.ABYSSAL_FOX.get(), 10, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.tag(SpdTags.ABYSSAL_WOLF_SPAWNS), MobCategory.CREATURE, SpdEntities.ABYSSAL_WOLF.get(), 8, 2, 3);
        addOverworldOre("ore_blazing_vein_upper");
        addOverworldOre("ore_blazing_vein_middle");
        addOverworldOre("ore_blazing_vein_small");
        addOverworldOre("ore_blood_ash");
        addOverworldOre("ore_blood_ash_buried");
        addChromeCaveOre("ore_blazing_vein_chrome_caves");
    }

    private static void addOverworldOre(String name) {
        ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Spd.MOD_ID, name));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, key);
    }

    private static void addChromeCaveOre(String name) {
        ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Spd.MOD_ID, name));
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(SpdBiomes.CHROME_SEABED_CAVES),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                key);
    }
}
