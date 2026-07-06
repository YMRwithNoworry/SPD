package alku.spd.registry;

import alku.spd.Spd;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class SpdMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Spd.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<ModularUIContainerMenu>> ABYSSAL_HEART_FORGE = MENUS.register(
            "abyssal_heart_forge",
            () -> MenuRegistry.ofExtended(SpdMenus::createAbyssalHeartForgeMenu)
    );

    private SpdMenus() {
    }

    public static void register() {
        MENUS.register();
    }

    public static boolean openAbyssalHeartForge(ServerPlayer player, BlockPos pos) {
        BlockState blockState = player.level().getBlockState(pos);
        if (!(blockState.getBlock() instanceof BlockUIMenuType.BlockUI blockUI)) {
            return false;
        }

        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return blockUI.getUIDisplayName(blockUI.createUIHolder(player, pos, blockState));
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return createAbyssalHeartForgeMenu(containerId, inventory, pos);
            }
        };
        MenuRegistry.openExtendedMenu(player, provider, buffer -> buffer.writeBlockPos(pos));
        return true;
    }

    private static ModularUIContainerMenu createAbyssalHeartForgeMenu(int windowId, Inventory inventory, FriendlyByteBuf data) {
        return createAbyssalHeartForgeMenu(windowId, inventory, data.readBlockPos());
    }

    private static ModularUIContainerMenu createAbyssalHeartForgeMenu(int windowId, Inventory inventory, BlockPos pos) {
        Player player = inventory.player;
        BlockState blockState = player.level().getBlockState(pos);
        if (blockState.getBlock() instanceof BlockUIMenuType.BlockUI blockUI) {
            return new ModularUIContainerMenu(
                    ABYSSAL_HEART_FORGE.get(),
                    windowId,
                    inventory,
                    blockUI.createUIHolder(player, pos, blockState)
            );
        }
        throw new IllegalArgumentException("No Abyssal Heart Forge UI found for block " + blockState);
    }
}
