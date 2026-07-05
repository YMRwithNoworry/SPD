package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.effect.AbyssalBoostEffect;
import alku.spd.effect.AbyssalPressureEffect;
import alku.spd.effect.ErosionSuppressionEffect;
import alku.spd.effect.MoldCurseEffect;
import alku.spd.effect.SearingPulseEffect;
import alku.spd.effect.SubjugationEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public final class SpdEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Spd.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> SUBJUGATION = EFFECTS.register("subjugation", SubjugationEffect::new);
    public static final RegistrySupplier<MobEffect> MOLD_CURSE = EFFECTS.register("mold_curse", MoldCurseEffect::new);
    public static final RegistrySupplier<MobEffect> SEARING_PULSE = EFFECTS.register("searing_pulse", SearingPulseEffect::new);
    public static final RegistrySupplier<MobEffect> EROSION_SUPPRESSION = EFFECTS.register("erosion_suppression", ErosionSuppressionEffect::new);
    public static final RegistrySupplier<MobEffect> ABYSSAL_PRESSURE = EFFECTS.register("abyssal_pressure", AbyssalPressureEffect::new);
    public static final RegistrySupplier<MobEffect> ABYSSAL_BOOST = EFFECTS.register("abyssal_boost", AbyssalBoostEffect::new);

    private SpdEffects() {
    }

    public static void register() {
        EFFECTS.register();
    }
}
