package alku.spd.world;

import alku.spd.registry.SpdBiomes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;

public final class AbyssalBloodDesertRegion extends Region {
    public AbyssalBloodDesertRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        addModifiedVanillaOverworldBiomes(mapper, builder ->
                builder.replaceBiome(Biomes.DESERT, SpdBiomes.ABYSSAL_BLOOD_DESERT));
    }
}
