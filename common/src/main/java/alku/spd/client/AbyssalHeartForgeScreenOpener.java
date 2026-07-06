package alku.spd.client;

import alku.spd.block.AbyssalHeartForgeBlock;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.factory.LDMenuTypes;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class AbyssalHeartForgeScreenOpener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbyssalHeartForgeScreenOpener.class);

    private AbyssalHeartForgeScreenOpener() {
    }

    public static void open(int containerId, BlockPos pos) {
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

        BlockUIMenuType.BlockUIHolder holder = block.createUIHolder(minecraft.player, pos, blockState);
        ModularUIContainerMenu menu = new ModularUIContainerMenu(
                LDMenuTypes.BLOCK_UI.get(),
                containerId,
                minecraft.player.getInventory(),
                holder
        );
        ModularUIContainerScreen screen = new ModularUIContainerScreen(menu, minecraft.player.getInventory(), block.getUIDisplayName(holder));
        minecraft.player.containerMenu = menu;
        minecraft.setScreen(screen);
        LOGGER.info("[SPD-FORGE-GUI] Opened SPD direct forge GUI screen id={} at {}", containerId, pos);
    }
}
