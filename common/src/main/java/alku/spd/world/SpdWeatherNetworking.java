package alku.spd.world;

import alku.spd.Spd;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class SpdWeatherNetworking {
    public static final ResourceLocation ABYSSAL_GLOOM_SYNC = new ResourceLocation(Spd.MOD_ID, "abyssal_gloom_sync");

    private SpdWeatherNetworking() {
    }

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ABYSSAL_GLOOM_SYNC, (buf, context) -> {
            boolean active = buf.readBoolean();
            int ticks = buf.readInt();
            float windX = buf.readFloat();
            float windZ = buf.readFloat();
            context.queue(() -> AbyssalGloomWeather.setClientState(active, ticks, windX, windZ));
        });
    }

    public static void syncLevel(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            syncPlayer(player);
        }
    }

    public static void syncPlayer(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        AbyssalGloomWeather.State state = AbyssalGloomWeather.getOrCreate(level);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(state.active());
        buf.writeInt(state.ticks());
        buf.writeFloat(state.windX());
        buf.writeFloat(state.windZ());
        NetworkManager.sendToPlayer(player, ABYSSAL_GLOOM_SYNC, buf);
    }
}
