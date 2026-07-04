package alku.spd.fabric;

import alku.spd.Spd;
import alku.spd.entity.AbyssalLizardEntity;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.registry.SpdEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import software.bernie.geckolib.GeckoLib;

public final class SpdFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        GeckoLib.initialize();
        Spd.init();
        FabricDefaultAttributeRegistry.register(SpdEntities.ABYSSAL_LIZARD.get(), AbyssalLizardEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieEntity.createAttributes());
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, SpdEntities.ABYSSAL_LIZARD.get(), 20, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, SpdEntities.MOLD_ZOMBIE.get(), 80, 1, 4);
        addOverworldOre("ore_blazing_vein_upper");
        addOverworldOre("ore_blazing_vein_middle");
        addOverworldOre("ore_blazing_vein_small");
    }

    private static void addOverworldOre(String name) {
        ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Spd.MOD_ID, name));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, key);
    }
}
