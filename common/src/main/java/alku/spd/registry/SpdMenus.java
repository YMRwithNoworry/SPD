package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.menu.AbyssalHeartForgeMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public final class SpdMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Spd.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<AbyssalHeartForgeMenu>> ABYSSAL_HEART_FORGE = MENUS.register("abyssal_heart_forge", () ->
            MenuRegistry.ofExtended(AbyssalHeartForgeMenu::new));

    private SpdMenus() {
    }

    public static void register() {
        MENUS.register();
    }
}
