package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.item.BlazingVeinPiercingSpearItem;
import alku.spd.item.AbyssalBlazingRuneSteleItem;
import alku.spd.item.BlazingVeinGreatswordItem;
import alku.spd.item.BlazingVeinDaggerItem;
import alku.spd.item.GuideBookItem;
import alku.spd.item.NamelessSwordItem;
import alku.spd.item.RumorItem;
import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

public final class SpdItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Spd.MOD_ID, Registries.ITEM);
    private static final Tier BLAZING_EMBER_TIER = new Tier() {
        @Override
        public int getUses() {
            return Tiers.IRON.getUses();
        }

        @Override
        public float getSpeed() {
            return Tiers.GOLD.getSpeed();
        }

        @Override
        public float getAttackDamageBonus() {
            return Tiers.IRON.getAttackDamageBonus();
        }

        @Override
        public int getLevel() {
            return Tiers.IRON.getLevel();
        }

        @Override
        public int getEnchantmentValue() {
            return Tiers.GOLD.getEnchantmentValue();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(BLAZING_CARBON_STEEL_INGOT.get());
        }
    };

    public static final RegistrySupplier<Item> SACRED_STIGMA = ITEMS.register("sacred_stigma", () ->
            new BlockItem(SpdBlocks.SACRED_STIGMA.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_VEIN_STONE = ITEMS.register("blazing_vein_stone", () ->
            new BlockItem(SpdBlocks.BLAZING_VEIN_STONE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> PULSE_CORE = ITEMS.register("pulse_core", () ->
            new BlockItem(SpdBlocks.PULSE_CORE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> TIER_ONE_COMPOSITE_BLOCK = ITEMS.register("tier_one_composite_block", () ->
            new BlockItem(SpdBlocks.TIER_ONE_COMPOSITE_BLOCK.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> TIER_TWO_COMPOSITE_BLOCK = ITEMS.register("tier_two_composite_block", () ->
            new BlockItem(SpdBlocks.TIER_TWO_COMPOSITE_BLOCK.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> TIER_THREE_COMPOSITE_BLOCK = ITEMS.register("tier_three_composite_block", () ->
            new BlockItem(SpdBlocks.TIER_THREE_COMPOSITE_BLOCK.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> TIER_FOUR_COMPOSITE_BLOCK = ITEMS.register("tier_four_composite_block", () ->
            new BlockItem(SpdBlocks.TIER_FOUR_COMPOSITE_BLOCK.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_RAW_ORE = ITEMS.register("blazing_raw_ore", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_SHARD = ITEMS.register("blazing_shard", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_CARBON_STEEL_INGOT = ITEMS.register("blazing_carbon_steel_ingot", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_EMBER_SWORD = ITEMS.register("blazing_ember_sword", () ->
            new SwordItem(BLAZING_EMBER_TIER, 3, -2.4F, new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_EMBER_AXE = ITEMS.register("blazing_ember_axe", () ->
            new AxeItem(BLAZING_EMBER_TIER, 6.0F, -3.1F, new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_EMBER_HOE = ITEMS.register("blazing_ember_hoe", () ->
            new HoeItem(BLAZING_EMBER_TIER, -2, -1.0F, new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_EMBER_SHOVEL = ITEMS.register("blazing_ember_shovel", () ->
            new ShovelItem(BLAZING_EMBER_TIER, 1.5F, -3.0F, new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_EMBER_PICKAXE = ITEMS.register("blazing_ember_pickaxe", () ->
            new PickaxeItem(BLAZING_EMBER_TIER, 1, -2.8F, new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_VEIN_GREATSWORD = ITEMS.register("blazing_vein_greatsword", () ->
            new BlazingVeinGreatswordItem(BLAZING_EMBER_TIER, new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_VEIN_PIERCING_SPEAR = ITEMS.register("blazing_vein_piercing_spear", () ->
            new BlazingVeinPiercingSpearItem(BLAZING_EMBER_TIER, new Item.Properties()));

    public static final RegistrySupplier<Item> BLAZING_VEIN_DAGGER = ITEMS.register("blazing_vein_dagger", () ->
            new BlazingVeinDaggerItem(BLAZING_EMBER_TIER, new Item.Properties()));

    public static final RegistrySupplier<Item> BLOOD_ASH_ORE = ITEMS.register("blood_ash_ore", () ->
            new BlockItem(SpdBlocks.BLOOD_ASH_ORE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> BLOOD_ASH_RAW_ORE = ITEMS.register("blood_ash_raw_ore", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> BLOOD_ASH_INGOT = ITEMS.register("blood_ash_ingot", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> BLOOD_ASH_INGOT_BLOCK = ITEMS.register("blood_ash_ingot_block", () ->
            new BlockItem(SpdBlocks.BLOOD_ASH_INGOT_BLOCK.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> EMBER_HANDLE = ITEMS.register("ember_handle", () ->
            new Item(new Item.Properties()) {
                @Override
                public Component getName(ItemStack stack) {
                    return super.getName(stack).copy().withStyle(ChatFormatting.GOLD);
                }
            });

    public static final RegistrySupplier<Item> FUNGAL_RESIDUE = ITEMS.register("fungal_residue", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> CHROME_DUST = ITEMS.register("chrome_dust", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> CORRUPTED_SAND_SCALE_HIDE = ITEMS.register("corrupted_sand_scale_hide", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> ABYSSAL_HEART_SPORE = ITEMS.register("abyssal_heart_spore", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> ETCHED_TURTLE_SCUTE = ITEMS.register("etched_turtle_scute", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> LIQUID_GOLD = ITEMS.register("liquid_gold", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> CULTURE_MEDIUM = ITEMS.register("culture_medium", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> RUMOR = ITEMS.register("rumor", () ->
            new RumorItem(new Item.Properties()));

    public static final RegistrySupplier<Item> PULSING_HATRED = ITEMS.register("pulsing_hatred", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> CONTRADICTION_DYE = ITEMS.register("contradiction_dye", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> NAMELESS_SWORD = ITEMS.register("nameless_sword", () ->
            new NamelessSwordItem(Tiers.NETHERITE, new Item.Properties()
                    .durability(2600)
                    .fireResistant()));

    public static final RegistrySupplier<Item> ABYSSAL_BLOOD_SAND = ITEMS.register("abyssal_blood_sand", () ->
            new BlockItem(SpdBlocks.ABYSSAL_BLOOD_SAND.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ABYSSAL_TURTLE_EGG = ITEMS.register("abyssal_turtle_egg", () ->
            new BlockItem(SpdBlocks.ABYSSAL_TURTLE_EGG.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SPD_TAB_ICON = ITEMS.register("spd_tab_icon", () ->
            new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> GUIDE_BOOK = ITEMS.register("guide_book", () ->
            new GuideBookItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> VINE_PLAGUE_NODE = ITEMS.register("vine_plague_node", () ->
            new BlockItem(SpdBlocks.VINE_PLAGUE_NODE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> WIDESPREAD_EPIDEMIC = ITEMS.register("widespread_epidemic", () ->
            new BlockItem(SpdBlocks.WIDESPREAD_EPIDEMIC.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ABYSSAL_HEART_FORGE = ITEMS.register("abyssal_heart_forge", () ->
            new BlockItem(SpdBlocks.ABYSSAL_HEART_FORGE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> CRUCIBLE_WALL = ITEMS.register("crucible_wall", () ->
            new BlockItem(SpdBlocks.CRUCIBLE_WALL.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> MOLTEN_CHROME_NOZZLE = ITEMS.register("molten_chrome_nozzle", () ->
            new BlockItem(SpdBlocks.MOLTEN_CHROME_NOZZLE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> MASCOT = ITEMS.register("mascot", () ->
            new BlockItem(SpdBlocks.MASCOT.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ABYSSAL_BLAZING_RUNE_STELE = ITEMS.register("abyssal_blazing_rune_stele", () ->
            new AbyssalBlazingRuneSteleItem(SpdBlocks.ABYSSAL_BLAZING_RUNE_STELE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ABYSSAL_ERODED_SILVERFISH_SPAWN_EGG = ITEMS.register("abyssal_eroded_silverfish_spawn_egg", () ->
            new ArchitecturySpawnEggItem(SpdEntities.ABYSSAL_ERODED_SILVERFISH, 0x311724, 0x8E2A40, new Item.Properties()));

    public static final RegistrySupplier<Item> MOLD_ZOMBIE_SPAWN_EGG = ITEMS.register("mold_zombie_spawn_egg", () ->
            new ArchitecturySpawnEggItem(SpdEntities.MOLD_ZOMBIE, 0x5A6B45, 0x2E3A27, new Item.Properties()));

    public static final RegistrySupplier<Item> ABYSSAL_FOX_SPAWN_EGG = ITEMS.register("abyssal_fox_spawn_egg", () ->
            new ArchitecturySpawnEggItem(SpdEntities.ABYSSAL_FOX, 0x3B171D, 0x8D2C3F, new Item.Properties()));

    public static final RegistrySupplier<Item> ABYSSAL_WOLF_SPAWN_EGG = ITEMS.register("abyssal_wolf_spawn_egg", () ->
            new ArchitecturySpawnEggItem(SpdEntities.ABYSSAL_WOLF, 0x32171B, 0x9A3741, new Item.Properties()));

    public static final RegistrySupplier<Item> ABYSSAL_TURTLE_SPAWN_EGG = ITEMS.register("abyssal_turtle_spawn_egg", () ->
            new ArchitecturySpawnEggItem(SpdEntities.ABYSSAL_TURTLE, 0x311719, 0x8A3037, new Item.Properties()));

    private SpdItems() {
    }

    public static void register() {
        ITEMS.register();
    }
}
