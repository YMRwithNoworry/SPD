package alku.spd.network;

import alku.spd.Spd;
import alku.spd.client.MoltenChromeNozzleScreenOpener;
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

public final class MoltenChromeNozzleNetworking {
    public static final ResourceLocation OPEN_GUI = new ResourceLocation(Spd.MOD_ID, "open_molten_chrome_nozzle");
    private static final Logger LOGGER = LoggerFactory.getLogger(MoltenChromeNozzleNetworking.class);

    private MoltenChromeNozzleNetworking() {
    }

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, OPEN_GUI, (buf, context) -> {
            int containerId = buf.readVarInt();
            BlockPos pos = buf.readBlockPos();
            context.queue(() -> EnvExecutor.runInEnv(Env.CLIENT,
                    () -> () -> MoltenChromeNozzleScreenOpener.open(containerId, pos)));
        });
    }

    public static boolean open(ServerPlayer player, BlockPos pos) {
        if (Platform.isForge()) {
            LOGGER.info("[SPD-NOZZLE-GUI] Opening LDLib2 server menu before direct forge GUI packet for {} at {}", player.getGameProfile().getName(), pos);
            if (!BlockUIMenuType.openUI(player, pos)) {
                return false;
            }

            int containerId = player.containerMenu.containerId;
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(containerId);
            buf.writeBlockPos(pos);
            NetworkManager.sendToPlayer(player, OPEN_GUI, buf);
            return true;
        }

        return BlockUIMenuType.openUI(player, pos);
    }
}
