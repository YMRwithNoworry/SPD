package alku.spd.world;

import alku.spd.Spd;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class SpdBigEyesNetworking {
    public static final ResourceLocation SYNC = new ResourceLocation(Spd.MOD_ID, "big_eyes_sync");

    private SpdBigEyesNetworking() {
    }

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SYNC, (buf, context) -> {
            boolean active = buf.readBoolean();
            context.queue(() -> SpdBigEyes.setClientActive(active));
        });
    }

    public static void syncAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncPlayer(player);
        }
    }

    public static void syncPlayer(ServerPlayer player) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(SpdBigEyes.isActive(player.getServer()));
        NetworkManager.sendToPlayer(player, SYNC, buf);
    }
}
