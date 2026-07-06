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
                output.accept(SpdItems.BLOOD_ASH_ORE.get());
                output.accept(SpdItems.BLOOD_ASH_RAW_ORE.get());
                output.accept(SpdItems.BLOOD_ASH_INGOT.get());
                output.accept(SpdItems.EMBER_HANDLE.get());
                output.accept(SpdItems.NAMELESS_SWORD.get());
                output.accept(SpdItems.ABYSSAL_BLOOD_SAND.get());
                output.accept(SpdItems.GUIDE_BOOK.get());
                output.accept(SpdItems.ABYSSAL_FUNGAL_VINES.get());
                output.accept(SpdItems.VINE_PLAGUE_NODE.get());
                output.accept(SpdItems.WIDESPREAD_EPIDEMIC.get());
                output.accept(SpdItems.ABYSSAL_HEART_FORGE.get());
                output.accept(SpdItems.CRUCIBLE_WALL.get());
                output.accept(SpdItems.MOLTEN_CHROME_NOZZLE.get());
                output.accept(SpdItems.MASCOT.get());
                output.accept(SpdItems.ABYSSAL_LIZARD_SPAWN_EGG.get());
                output.accept(SpdItems.ABYSSAL_ERODED_SILVERFISH_SPAWN_EGG.get());
                output.accept(SpdItems.MOLD_ZOMBIE_SPAWN_EGG.get());
            })
            .build());

    private SpdCreativeTabs() {
    }

    public static void register() {
        TABS.register();
    }
}
