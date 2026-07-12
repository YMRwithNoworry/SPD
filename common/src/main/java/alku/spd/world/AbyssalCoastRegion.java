package alku.spd.world;

import alku.spd.registry.SpdBiomes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public final class AbyssalCoastRegion extends Region {
    private static final Climate.Parameter HOT = Climate.Parameter.span(0.55F, 1.0F);
    private static final Climate.Parameter DRY = Climate.Parameter.span(-1.0F, -0.35F);
    private static final Climate.Parameter FULL = Climate.Parameter.span(-1.0F, 1.0F);
    private static final Climate.Parameter SURFACE = Climate.Parameter.span(0.0F, 1.0F);

    public AbyssalCoastRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        addBiome(mapper, HOT, DRY, Climate.Parameter.span(-0.19F, -0.11F), FULL, FULL, SURFACE,
                0.0F, SpdBiomes.ABYSSAL_COAST);
        addBiome(mapper, HOT, DRY, Climate.Parameter.span(-1.0F, -0.46F), FULL, FULL, SURFACE,
                0.0F, SpdBiomes.FUNGAL_SHALLOWS);
        addBiome(mapper, HOT, DRY, Climate.Parameter.span(-1.0F, 0.2F), FULL, FULL,
                Climate.Parameter.span(-1.0F, -0.1F), 0.0F, SpdBiomes.CHROME_SEABED_CAVES);
    }
}
