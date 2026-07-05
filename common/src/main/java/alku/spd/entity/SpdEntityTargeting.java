package alku.spd.entity;

import alku.spd.Spd;
import alku.spd.registry.SpdTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class SpdEntityTargeting {
    private SpdEntityTargeting() {
    }

    public static boolean isSpdEntity(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id != null && Spd.MOD_ID.equals(id.getNamespace());
    }

    public static boolean isAbyssalEntity(LivingEntity entity) {
        return entity.getType().is(SpdTags.ABYSSAL_ENTITIES) || isSpdEntity(entity);
    }

    public static boolean isMoldEntity(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id != null && Spd.MOD_ID.equals(id.getNamespace())
                && (id.getPath().contains("mold") || id.getPath().contains("fungal"));
    }

    public static boolean isNonSpdLiving(LivingEntity entity) {
        return entity.isAlive() && !isSpdEntity(entity);
    }
}
