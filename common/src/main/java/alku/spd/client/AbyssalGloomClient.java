package alku.spd.client;

import alku.spd.world.AbyssalGloomWeather;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class AbyssalGloomClient {
    private static final int DUST_PER_TICK = 24;

    private AbyssalGloomClient() {
    }

    public static void register() {
        ClientTickEvent.CLIENT_POST.register(AbyssalGloomClient::tick);
    }

    private static void tick(Minecraft minecraft) {
        AbyssalGloomWeather.clientTick();
        if (!AbyssalGloomWeather.isClientActive() || minecraft.level == null || minecraft.player == null) {
            return;
        }

        RandomSource random = minecraft.level.random;
        Vec3 wind = AbyssalGloomWeather.getClientWind();
        for (int i = 0; i < DUST_PER_TICK; i++) {
            double x = minecraft.player.getX() + Mth.nextDouble(random, -18.0D, 18.0D);
            double y = minecraft.player.getEyeY() + Mth.nextDouble(random, -4.0D, 7.0D);
            double z = minecraft.player.getZ() + Mth.nextDouble(random, -18.0D, 18.0D);
            double vx = wind.x * Mth.nextDouble(random, 0.06D, 0.16D) + Mth.nextDouble(random, -0.015D, 0.015D);
            double vy = Mth.nextDouble(random, -0.012D, 0.018D);
            double vz = wind.z * Mth.nextDouble(random, 0.06D, 0.16D) + Mth.nextDouble(random, -0.015D, 0.015D);
            minecraft.level.addParticle(random.nextBoolean() ? ParticleTypes.ASH : ParticleTypes.CRIMSON_SPORE, x, y, z, vx, vy, vz);
        }
    }
}
