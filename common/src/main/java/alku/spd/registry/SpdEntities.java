package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.entity.AbyssalErodedSilverfishEntity;
import alku.spd.entity.AbyssalFoxEntity;
import alku.spd.entity.AbyssalLightWaveEntity;
import alku.spd.entity.AbyssalLizardEntity;
import alku.spd.entity.AbyssalTornadoEntity;
import alku.spd.entity.AbyssalWolfEntity;
import alku.spd.entity.EpxCloudEntity;
import alku.spd.entity.EpxEntity;
import alku.spd.entity.FalseMotherEntity;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.entity.NamelessSlashEntity;
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

    public static final RegistrySupplier<EntityType<FalseMotherEntity>> FALSE_MOTHER = ENTITIES.register("false_mother", () ->
            EntityType.Builder.of(FalseMotherEntity::new, MobCategory.MONSTER)
                    .sized(2.2F, 2.8F)
                    .clientTrackingRange(10)
                    .build("false_mother"));

    public static final RegistrySupplier<EntityType<AbyssalLizardEntity>> ABYSSAL_LIZARD = ENTITIES.register("abyssal_lizard", () ->
            EntityType.Builder.of(AbyssalLizardEntity::new, MobCategory.MONSTER)
                    .sized(2.4F, 3.2F)
                    .clientTrackingRange(10)
                    .build("abyssal_lizard"));

    public static final RegistrySupplier<EntityType<AbyssalErodedSilverfishEntity>> ABYSSAL_ERODED_SILVERFISH = ENTITIES.register("abyssal_eroded_silverfish", () ->
            EntityType.Builder.of(AbyssalErodedSilverfishEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.45F)
                    .clientTrackingRange(8)
                    .build("abyssal_eroded_silverfish"));

    public static final RegistrySupplier<EntityType<AbyssalFoxEntity>> ABYSSAL_FOX = ENTITIES.register("abyssal_fox", () ->
            EntityType.Builder.of(AbyssalFoxEntity::new, MobCategory.CREATURE)
                    .sized(0.7F, 0.7F)
                    .clientTrackingRange(8)
                    .build("abyssal_fox"));

    public static final RegistrySupplier<EntityType<AbyssalWolfEntity>> ABYSSAL_WOLF = ENTITIES.register("abyssal_wolf", () ->
            EntityType.Builder.of(AbyssalWolfEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 0.9F)
                    .clientTrackingRange(8)
                    .build("abyssal_wolf"));

    public static final RegistrySupplier<EntityType<AbyssalLightWaveEntity>> ABYSSAL_LIGHT_WAVE = ENTITIES.register("abyssal_light_wave", () ->
            EntityType.Builder.<AbyssalLightWaveEntity>of(AbyssalLightWaveEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build("abyssal_light_wave"));

    public static final RegistrySupplier<EntityType<AbyssalTornadoEntity>> ABYSSAL_TORNADO = ENTITIES.register("abyssal_tornado", () ->
            EntityType.Builder.<AbyssalTornadoEntity>of(AbyssalTornadoEntity::new, MobCategory.MISC)
                    .sized(5.75F, 9.0F)
                    .clientTrackingRange(12)
                    .updateInterval(2)
                    .build("abyssal_tornado"));

    public static final RegistrySupplier<EntityType<NamelessSlashEntity>> NAMELESS_SLASH = ENTITIES.register("nameless_slash", () ->
            EntityType.Builder.<NamelessSlashEntity>of(NamelessSlashEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.8F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("nameless_slash"));

    public static final RegistrySupplier<EntityType<EpxEntity>> EPX = ENTITIES.register("epx", () ->
            EntityType.Builder.<EpxEntity>of(EpxEntity::new, MobCategory.MISC)
                    .sized(0.45F, 0.45F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build("epx"));

    public static final RegistrySupplier<EntityType<EpxCloudEntity>> EPX_CLOUD = ENTITIES.register("epx_cloud", () ->
            EntityType.Builder.<EpxCloudEntity>of(EpxCloudEntity::new, MobCategory.MISC)
                    .sized(3.5F, 1.2F)
                    .clientTrackingRange(8)
                    .updateInterval(4)
                    .build("epx_cloud"));

    private SpdEntities() {
    }

    public static void register() {
        ENTITIES.register();
    }
}
