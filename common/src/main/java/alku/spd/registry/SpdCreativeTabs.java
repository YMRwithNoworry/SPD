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
                output.accept(SpdItems.ABYSSAL_FUNGAL_VINES.get());
                output.accept(SpdItems.ABYSSAL_HEART_FORGE.get());
                output.accept(SpdItems.MASCOT.get());
                output.accept(SpdItems.ABYSSAL_LIZARD_SPAWN_EGG.get());
                output.accept(SpdItems.MOLD_ZOMBIE_SPAWN_EGG.get());
            })
            .build());

    private SpdCreativeTabs() {
    }

    public static void register() {
        TABS.register();
    }
}
