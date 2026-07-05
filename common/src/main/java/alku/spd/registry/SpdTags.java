package alku.spd.registry;

import alku.spd.Spd;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class SpdTags {
    public static final TagKey<EntityType<?>> ABYSSAL_ENTITIES = TagKey.create(
            Registries.ENTITY_TYPE,
            new ResourceLocation(Spd.MOD_ID, "abyssal_entities"));

    private SpdTags() {
    }
}
