package alku.spd.registry;

import alku.spd.Spd;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public final class SpdBiomes {
    public static final ResourceKey<Biome> ABYSSAL_BLOOD_DESERT = ResourceKey.create(
            Registries.BIOME,
            new ResourceLocation(Spd.MOD_ID, "abyssal_blood_desert"));

    private SpdBiomes() {
    }
}
