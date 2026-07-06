package alku.spd.network;

import alku.spd.Spd;
import alku.spd.client.AbyssalHeartForgeScreenOpener;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
            context.queue(() -> EnvExecutor.runInEnv(Env.CLIENT,
                    () -> () -> AbyssalHeartForgeScreenOpener.open(containerId, pos)));
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
}
