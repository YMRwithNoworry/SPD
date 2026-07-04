package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.effect.MoldCurseEffect;
import alku.spd.effect.SubjugationEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public final class SpdEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Spd.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> SUBJUGATION = EFFECTS.register("subjugation", SubjugationEffect::new);
    public static final RegistrySupplier<MobEffect> MOLD_CURSE = EFFECTS.register("mold_curse", MoldCurseEffect::new);

    private SpdEffects() {
    }

    public static void register() {
        EFFECTS.register();
    }
}
