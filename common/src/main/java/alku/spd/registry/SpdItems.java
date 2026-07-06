package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.item.GuideBookItem;
import alku.spd.item.NamelessSwordItem;
import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;

public final class SpdItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Spd.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> SACRED_STIGMA = ITEMS.register("sacred_stigma", () ->
            new BlockItem(SpdBlocks.SACRED_STIGMA.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_VEIN_STONE = ITEMS.register("blazing_vein_stone", () ->
            new BlockItem(SpdBlocks.BLAZING_VEIN_STONE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_RAW_ORE = ITEMS.register("blazing_raw_ore", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_SHARD = ITEMS.register("blazing_shard", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_CARBON_STEEL_INGOT = ITEMS.register("blazing_carbon_steel_ingot", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> NAMELESS_SWORD = ITEMS.register("nameless_sword", () ->
            new NamelessSwordItem(Tiers.NETHERITE, new Item.Properties()
                    .durability(2600)
                    .fireResistant()));

    public static final RegistrySupplier<Item> ABYSSAL_BLOOD_SAND = ITEMS.register("abyssal_blood_sand", () ->
            new BlockItem(SpdBlocks.ABYSSAL_BLOOD_SAND.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SPD_TAB_ICON = ITEMS.register("spd_tab_icon", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> GUIDE_BOOK = ITEMS.register("guide_book", () ->
            new GuideBookItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> ABYSSAL_FUNGAL_VINES = ITEMS.register("abyssal_fungal_vines", () ->
            new BlockItem(SpdBlocks.ABYSSAL_FUNGAL_VINES.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> VINE_PLAGUE_NODE = ITEMS.register("vine_plague_node", () ->
            new BlockItem(SpdBlocks.VINE_PLAGUE_NODE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> WIDESPREAD_EPIDEMIC = ITEMS.register("widespread_epidemic", () ->
            new BlockItem(SpdBlocks.WIDESPREAD_EPIDEMIC.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ABYSSAL_HEART_FORGE = ITEMS.register("abyssal_heart_forge", () ->
            new BlockItem(SpdBlocks.ABYSSAL_HEART_FORGE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> MASCOT = ITEMS.register("mascot", () ->
            new BlockItem(SpdBlocks.MASCOT.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ABYSSAL_LIZARD_SPAWN_EGG = ITEMS.register("abyssal_lizard_spawn_egg", () ->
            new ArchitecturySpawnEggItem(SpdEntities.ABYSSAL_LIZARD, 0x302832, 0x8D8272, new Item.Properties()));

    public static final RegistrySupplier<Item> MOLD_ZOMBIE_SPAWN_EGG = ITEMS.register("mold_zombie_spawn_egg", () ->
            new ArchitecturySpawnEggItem(SpdEntities.MOLD_ZOMBIE, 0x5A6B45, 0x2E3A27, new Item.Properties()));

    private SpdItems() {
    }

    public static void register() {
        ITEMS.register();
    }
}
