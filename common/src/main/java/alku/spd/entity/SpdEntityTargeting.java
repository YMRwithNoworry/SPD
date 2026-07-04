package alku.spd.entity;

import alku.spd.Spd;
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

    public static boolean isNonSpdLiving(LivingEntity entity) {
        return entity.isAlive() && !isSpdEntity(entity);
    }
}
