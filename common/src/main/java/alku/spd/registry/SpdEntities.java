package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.entity.AbyssalLightWaveEntity;
import alku.spd.entity.AbyssalLizardEntity;
import alku.spd.entity.MoldZombieEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class SpdEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Spd.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<MoldZombieEntity>> MOLD_ZOMBIE = ENTITIES.register("mold_zombie", () ->
            EntityType.Builder.of(MoldZombieEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build("mold_zombie"));

    public static final RegistrySupplier<EntityType<AbyssalLizardEntity>> ABYSSAL_LIZARD = ENTITIES.register("abyssal_lizard", () ->
            EntityType.Builder.of(AbyssalLizardEntity::new, MobCategory.MONSTER)
                    .sized(2.4F, 3.2F)
                    .clientTrackingRange(10)
                    .build("abyssal_lizard"));

    public static final RegistrySupplier<EntityType<AbyssalLightWaveEntity>> ABYSSAL_LIGHT_WAVE = ENTITIES.register("abyssal_light_wave", () ->
            EntityType.Builder.<AbyssalLightWaveEntity>of(AbyssalLightWaveEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build("abyssal_light_wave"));

    private SpdEntities() {
    }

    public static void register() {
        ENTITIES.register();
    }
}
