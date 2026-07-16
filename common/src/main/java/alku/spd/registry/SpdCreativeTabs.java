package alku.spd.registry;

import alku.spd.Spd;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public final class SpdCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Spd.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.spd.main"))
            .icon(() -> SpdItems.SPD_TAB_ICON.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(SpdItems.SACRED_STIGMA.get());
                output.accept(SpdItems.BLAZING_VEIN_STONE.get());
                output.accept(SpdItems.PULSE_CORE.get());
                output.accept(SpdItems.BLAZING_RAW_ORE.get());
                output.accept(SpdItems.BLAZING_SHARD.get());
                output.accept(SpdItems.BLAZING_CARBON_STEEL_INGOT.get());
                output.accept(SpdItems.BLAZING_EMBER_SWORD.get());
                output.accept(SpdItems.BLAZING_EMBER_AXE.get());
                output.accept(SpdItems.BLAZING_EMBER_HOE.get());
                output.accept(SpdItems.BLAZING_EMBER_SHOVEL.get());
                output.accept(SpdItems.BLAZING_EMBER_PICKAXE.get());
                output.accept(SpdItems.BLAZING_VEIN_GREATSWORD.get());
                output.accept(SpdItems.BLAZING_VEIN_PIERCING_SPEAR.get());
                output.accept(SpdItems.BLAZING_VEIN_DAGGER.get());
                output.accept(SpdItems.BLOOD_ASH_ORE.get());
                output.accept(SpdItems.BLOOD_ASH_RAW_ORE.get());
                output.accept(SpdItems.BLOOD_ASH_INGOT.get());
                output.accept(SpdItems.BLOOD_ASH_INGOT_BLOCK.get());
                output.accept(SpdItems.EMBER_HANDLE.get());
                output.accept(SpdItems.FUNGAL_RESIDUE.get());
                output.accept(SpdItems.CHROME_DUST.get());
                output.accept(SpdItems.CORRUPTED_SAND_SCALE_HIDE.get());
                output.accept(SpdItems.ABYSSAL_HEART_SPORE.get());
                output.accept(SpdItems.ETCHED_TURTLE_SCUTE.get());
                output.accept(SpdItems.LIQUID_GOLD.get());
                output.accept(SpdItems.CULTURE_MEDIUM.get());
                output.accept(SpdItems.NAMELESS_SWORD.get());
                output.accept(SpdItems.ABYSSAL_BLOOD_SAND.get());
                output.accept(SpdItems.ABYSSAL_TURTLE_EGG.get());
                output.accept(SpdItems.GUIDE_BOOK.get());
                output.accept(SpdItems.VINE_PLAGUE_NODE.get());
                output.accept(SpdItems.WIDESPREAD_EPIDEMIC.get());
                output.accept(SpdItems.ABYSSAL_HEART_FORGE.get());
                output.accept(SpdItems.CRUCIBLE_WALL.get());
                output.accept(SpdItems.MOLTEN_CHROME_NOZZLE.get());
                output.accept(SpdItems.MASCOT.get());
                output.accept(SpdItems.ABYSSAL_BLAZING_RUNE_STELE.get());
                output.accept(SpdItems.ABYSSAL_ERODED_SILVERFISH_SPAWN_EGG.get());
                output.accept(SpdItems.MOLD_ZOMBIE_SPAWN_EGG.get());
                output.accept(SpdItems.ABYSSAL_FOX_SPAWN_EGG.get());
                output.accept(SpdItems.ABYSSAL_WOLF_SPAWN_EGG.get());
                output.accept(SpdItems.ABYSSAL_TURTLE_SPAWN_EGG.get());
            })
            .build());

    private SpdCreativeTabs() {
    }

    public static void register() {
        TABS.register();
    }
}
