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
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.function.Consumer;

public final class AbyssalCoastRegion extends Region {
    private static final Climate.Parameter HOT = Climate.Parameter.span(0.55F, 1.0F);
    private static final Climate.Parameter DRY = Climate.Parameter.span(-1.0F, -0.35F);
    private static final Climate.Parameter FULL = Climate.Parameter.span(-1.0F, 1.0F);
    private static final Climate.Parameter SURFACE_DEPTH = Climate.Parameter.span(-0.05F, 0.05F);
    private static final Climate.Parameter UNDERGROUND_DEPTH = Climate.Parameter.span(0.2F, 0.9F);

    public AbyssalCoastRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        VanillaParameterOverlayBuilder overlay = new VanillaParameterOverlayBuilder();
        overlay.add(parameters(Climate.Parameter.span(-0.19F, -0.11F), SURFACE_DEPTH),
                SpdBiomes.ABYSSAL_COAST);
        overlay.add(parameters(Climate.Parameter.span(-1.0F, -0.46F), SURFACE_DEPTH),
                SpdBiomes.FUNGAL_SHALLOWS);
        overlay.add(parameters(Climate.Parameter.span(-1.0F, 0.2F), UNDERGROUND_DEPTH),
                SpdBiomes.CHROME_SEABED_CAVES);
        overlay.build().forEach(mapper::accept);
    }

    private static Climate.ParameterPoint parameters(Climate.Parameter continentalness,
                                                      Climate.Parameter depth) {
        return Climate.parameters(HOT, DRY, continentalness, FULL, depth, FULL, 0.0F);
    }
}
