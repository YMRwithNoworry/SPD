package alku.spd.network;

import alku.spd.Spd;
import alku.spd.block.AbyssalHeartForgeBlock;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.factory.LDMenuTypes;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AbyssalHeartForgeNetworking {
    public static final ResourceLocation OPEN_GUI = new ResourceLocation(Spd.MOD_ID, "open_abyssal_heart_forge");
    private static final Logger LOGGER = LoggerFactory.getLogger(AbyssalHeartForgeNetworking.class);

    private AbyssalHeartForgeNetworking() {
    }

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, OPEN_GUI, (buf, context) -> {
            int containerId = buf.readVarInt();
            BlockPos pos = buf.readBlockPos();
            context.queue(() -> openClientScreen(containerId, pos));
        });
    }

    public static boolean open(ServerPlayer player, BlockPos pos) {
        if (Platform.isForge()) {
            LOGGER.info("[SPD-FORGE-GUI] Opening LDLib2 server menu before SPD direct forge GUI packet for {} at {}", player.getGameProfile().getName(), pos);
            if (!BlockUIMenuType.openUI(player, pos)) {
                return false;
            }

            int containerId = player.containerMenu.containerId;
            LOGGER.info("[SPD-FORGE-GUI] Sending SPD direct forge GUI packet to {} at {} using containerId={}", player.getGameProfile().getName(), pos, containerId);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(containerId);
            buf.writeBlockPos(pos);
            NetworkManager.sendToPlayer(player, OPEN_GUI, buf);
            return true;
        }

        LOGGER.info("[SPD-FORGE-GUI] Falling back to LDLib2 BlockUIMenuType.openUI for {} at {}", player.getGameProfile().getName(), pos);
        return BlockUIMenuType.openUI(player, pos);
    }

    private static void openClientScreen(int containerId, BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            LOGGER.warn("[SPD-FORGE-GUI] Cannot open direct forge GUI because the client player or level is missing");
            return;
        }

        BlockState blockState = minecraft.level.getBlockState(pos);
        LOGGER.info("[SPD-FORGE-GUI] Received SPD direct forge GUI packet containerId={} pos={} block={}", containerId, pos, blockState);
        if (!(blockState.getBlock() instanceof AbyssalHeartForgeBlock block)) {
            LOGGER.warn("[SPD-FORGE-GUI] Cannot open direct forge GUI because {} is not an Abyssal Heart Forge", blockState);
            return;
        }

        ModularUIContainerMenu menu = new ModularUIContainerMenu(
                LDMenuTypes.BLOCK_UI.get(),
                containerId,
                minecraft.player.getInventory(),
                block.createUIHolder(minecraft.player, pos, blockState)
        );
        ModularUIContainerScreen screen = new ModularUIContainerScreen(menu, minecraft.player.getInventory(), block.getUIDisplayName(block.createUIHolder(minecraft.player, pos, blockState)));
        minecraft.player.containerMenu = menu;
        minecraft.setScreen(screen);
        LOGGER.info("[SPD-FORGE-GUI] Opened SPD direct forge GUI screen id={} at {}", containerId, pos);
    }
}
