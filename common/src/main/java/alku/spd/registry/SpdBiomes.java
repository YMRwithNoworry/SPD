package alku.spd.registry;

import alku.spd.Spd;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public final class SpdBiomes {
    public static final ResourceKey<Biome> ABYSSAL_BLOOD_DESERT = key("abyssal_blood_desert");
    public static final ResourceKey<Biome> ABYSSAL_COAST = key("abyssal_coast");
    public static final ResourceKey<Biome> FUNGAL_SHALLOWS = key("fungal_shallows");
    public static final ResourceKey<Biome> CHROME_SEABED_CAVES = key("chrome_seabed_caves");

    private SpdBiomes() {
    }

    private static ResourceKey<Biome> key(String id) {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(Spd.MOD_ID, id));
    }
}
