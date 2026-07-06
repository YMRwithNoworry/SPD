package alku.spd.registry;

import alku.spd.Spd;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SpdMenus {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpdMenus.class);

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
        LOGGER.info("[SPD-FORGE-GUI] openAbyssalHeartForge player={} pos={} block={} menuKey={} menuId={}",
                player.getGameProfile().getName(),
                pos,
                blockState.getBlock(),
                BuiltInRegistries.MENU.getKey(ABYSSAL_HEART_FORGE.get()),
                BuiltInRegistries.MENU.getId(ABYSSAL_HEART_FORGE.get()));
        if (!(blockState.getBlock() instanceof BlockUIMenuType.BlockUI blockUI)) {
            LOGGER.warn("[SPD-FORGE-GUI] Block at {} does not implement BlockUI: {}", pos, blockState);
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
                LOGGER.info("[SPD-FORGE-GUI] Server provider createMenu containerId={} player={} pos={}",
                        containerId,
                        menuPlayer.getGameProfile().getName(),
                        pos);
                return createAbyssalHeartForgeMenu(containerId, inventory, pos);
            }
        };
        LOGGER.info("[SPD-FORGE-GUI] Sending extended menu open packet for pos={}", pos);
        MenuRegistry.openExtendedMenu(player, provider, buffer -> buffer.writeBlockPos(pos));
        return true;
    }

    private static ModularUIContainerMenu createAbyssalHeartForgeMenu(int windowId, Inventory inventory, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        LOGGER.info("[SPD-FORGE-GUI] Client createAbyssalHeartForgeMenu from buffer windowId={} player={} pos={} menuKey={} menuId={}",
                windowId,
                inventory.player.getGameProfile().getName(),
                pos,
                BuiltInRegistries.MENU.getKey(ABYSSAL_HEART_FORGE.get()),
                BuiltInRegistries.MENU.getId(ABYSSAL_HEART_FORGE.get()));
        return createAbyssalHeartForgeMenu(windowId, inventory, pos);
    }

    private static ModularUIContainerMenu createAbyssalHeartForgeMenu(int windowId, Inventory inventory, BlockPos pos) {
        Player player = inventory.player;
        BlockState blockState = player.level().getBlockState(pos);
        LOGGER.info("[SPD-FORGE-GUI] createAbyssalHeartForgeMenu windowId={} player={} pos={} block={} side={}",
                windowId,
                player.getGameProfile().getName(),
                pos,
                blockState,
                player.level().isClientSide ? "client" : "server");
        if (blockState.getBlock() instanceof BlockUIMenuType.BlockUI blockUI) {
            return new ModularUIContainerMenu(
                    ABYSSAL_HEART_FORGE.get(),
                    windowId,
                    inventory,
                    blockUI.createUIHolder(player, pos, blockState)
            );
        }
        LOGGER.error("[SPD-FORGE-GUI] No Abyssal Heart Forge UI found for block {} at {}", blockState, pos);
        throw new IllegalArgumentException("No Abyssal Heart Forge UI found for block " + blockState);
    }
}
