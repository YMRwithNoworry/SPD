package alku.spd.mixin;

import alku.spd.registry.SpdBiomes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(MultiNoiseBiomeSourceParameterList.Preset.class)
public abstract class MultiNoiseBiomeSourceParameterListPresetMixin {
    @Inject(method = "generateOverworldBiomes", at = @At("RETURN"), cancellable = true)
    private static <T> void spd$addAbyssalBloodDesert(Function<ResourceKey<Biome>, T> biomeGetter, CallbackInfoReturnable<Climate.ParameterList<T>> cir) {
        List<Pair<Climate.ParameterPoint, T>> modified = new ArrayList<>();
        T desert = biomeGetter.apply(Biomes.DESERT);
        T bloodDesert = biomeGetter.apply(SpdBiomes.ABYSSAL_BLOOD_DESERT);
        int desertCount = 0;

        for (Pair<Climate.ParameterPoint, T> entry : cir.getReturnValue().values()) {
            if (entry.getSecond().equals(desert)) {
                desertCount++;
                if (desertCount % 3 == 0) {
                    modified.add(Pair.of(entry.getFirst(), bloodDesert));
                    continue;
                }
            }
            modified.add(entry);
        }

        if (desertCount == 0) {
            modified.add(Pair.of(Climate.parameters(
                    Climate.Parameter.span(0.55F, 1.0F),
                    Climate.Parameter.span(-1.0F, -0.35F),
                    Climate.Parameter.span(-0.11F, 1.0F),
                    Climate.Parameter.span(-1.0F, 1.0F),
                    Climate.Parameter.point(0.0F),
                    Climate.Parameter.span(-1.0F, 1.0F),
                    0.0F), bloodDesert));
        }

        cir.setReturnValue(new Climate.ParameterList<>(modified));
    }
}
