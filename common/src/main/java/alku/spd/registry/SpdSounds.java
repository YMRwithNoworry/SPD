package alku.spd.registry;

import alku.spd.Spd;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class SpdSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Spd.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> BREATHING_MUSIC = SOUND_EVENTS.register("music.breathing", () ->
            SoundEvent.createVariableRangeEvent(new ResourceLocation(Spd.MOD_ID, "music.breathing")));

    private SpdSounds() {
    }

    public static void register() {
        SOUND_EVENTS.register();
    }
}
