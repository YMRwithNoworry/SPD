package alku.spd.registry;

import alku.spd.Spd;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public final class SpdTags {
    public static final TagKey<DamageType> ABYSSAL_CORROSION_DAMAGE = TagKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(Spd.MOD_ID, "abyssal_corrosion"));
    public static final TagKey<EntityType<?>> ABYSSAL_ENTITIES = TagKey.create(
            Registries.ENTITY_TYPE,
            new ResourceLocation(Spd.MOD_ID, "abyssal_entities"));
    public static final TagKey<Biome> ABYSSAL_BIOMES = TagKey.create(
            Registries.BIOME,
            new ResourceLocation(Spd.MOD_ID, "is_abyssal_blood_desert"));
    public static final TagKey<Biome> ABYSSAL_FOX_SPAWNS = TagKey.create(
            Registries.BIOME,
            new ResourceLocation(Spd.MOD_ID, "abyssal_fox_spawns"));
    public static final TagKey<Block> PURIFICATION_BLOCKS = TagKey.create(
            Registries.BLOCK,
            new ResourceLocation(Spd.MOD_ID, "purification_blocks"));
    public static final TagKey<DamageType> PURIFICATION_DAMAGE = TagKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(Spd.MOD_ID, "purification"));
    public static final TagKey<Item> FORGE_ORES = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("forge", "ores"));
    public static final TagKey<Item> COMMON_ORES = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("c", "ores"));
    public static final TagKey<Item> ABYSSAL_PRESSURE_RESISTANT_ARMOR = TagKey.create(
            Registries.ITEM,
            new ResourceLocation(Spd.MOD_ID, "abyssal_pressure_resistant_armor"));
    public static final TagKey<Item> ABYSSAL_PRESSURE_CURES = TagKey.create(
            Registries.ITEM,
            new ResourceLocation(Spd.MOD_ID, "abyssal_pressure_cures"));

    private SpdTags() {
    }
}
