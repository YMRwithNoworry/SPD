package alku.spd.world;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class AbyssalGloomWeather {
    public static final String NAME = "渊默";
    public static final int NATURAL_CHECK_INTERVAL = 20 * 60;
    public static final int MIN_DURATION = 20 * 90;
    public static final int MAX_DURATION = 20 * 180;

    private static final Map<ResourceKey<Level>, State> SERVER_STATES = new HashMap<>();
    private static boolean clientActive;
    private static int clientTicks;
    private static float clientWindX;
    private static float clientWindZ = 1.0F;

    private AbyssalGloomWeather() {
    }

    public static boolean isActive(Level level) {
        if (level.isClientSide()) {
            return clientActive;
        }
        State state = SERVER_STATES.get(level.dimension());
        return state != null && state.active;
    }

    public static boolean isClientActive() {
        return clientActive;
    }

    public static void setClientState(boolean active, int ticks, float windX, float windZ) {
        clientActive = active;
        clientTicks = Math.max(0, ticks);
        setClientWind(windX, windZ);
    }

    public static int getClientTicks() {
        return clientTicks;
    }

    public static Vec3 getClientWind() {
        return new Vec3(clientWindX, 0.0D, clientWindZ);
    }

    public static void clientTick() {
        if (clientActive) {
            clientTicks++;
        }
    }

    public static State getOrCreate(ServerLevel level) {
        return SERVER_STATES.computeIfAbsent(level.dimension(), key -> State.create(level));
    }

    public static void setActive(ServerLevel level, boolean active) {
        State state = getOrCreate(level);
        if (active) {
            state.start(level, randomDuration(level));
        } else {
            state.stop();
        }
        SpdWeatherNetworking.syncLevel(level);
    }

    static void clear() {
        SERVER_STATES.clear();
        clientActive = false;
        clientTicks = 0;
    }

    private static int randomDuration(ServerLevel level) {
        return Mth.nextInt(level.random, MIN_DURATION, MAX_DURATION);
    }

    private static void setClientWind(float windX, float windZ) {
        float length = Mth.sqrt(windX * windX + windZ * windZ);
        if (length < 0.001F) {
            clientWindX = 0.0F;
            clientWindZ = 1.0F;
        } else {
            clientWindX = windX / length;
            clientWindZ = windZ / length;
        }
    }

    public static final class State {
        private boolean active;
        private int ticks;
        private int remainingTicks;
        private int naturalCheckTicks;
        private float windAngle;
        private float windTurnSpeed;

        private static State create(ServerLevel level) {
            State state = new State();
            state.windAngle = level.random.nextFloat() * Mth.TWO_PI;
            state.windTurnSpeed = 0.003F + level.random.nextFloat() * 0.003F;
            state.naturalCheckTicks = NATURAL_CHECK_INTERVAL;
            return state;
        }

        public boolean active() {
            return active;
        }

        public int ticks() {
            return ticks;
        }

        public float windX() {
            return Mth.cos(windAngle);
        }

        public float windZ() {
            return Mth.sin(windAngle);
        }

        void tick(ServerLevel level) {
            if (active) {
                ticks++;
                remainingTicks--;
                windAngle += windTurnSpeed + Mth.sin(ticks * 0.006F) * 0.0015F;
                if (remainingTicks <= 0) {
                    stop();
                    SpdWeatherNetworking.syncLevel(level);
                } else if (ticks % 40 == 0) {
                    SpdWeatherNetworking.syncLevel(level);
                }
                return;
            }

            naturalCheckTicks--;
            if (naturalCheckTicks <= 0) {
                naturalCheckTicks = NATURAL_CHECK_INTERVAL;
                if (level.dimension() == Level.OVERWORLD && level.random.nextFloat() < 0.08F) {
                    start(level, randomDuration(level));
                    SpdWeatherNetworking.syncLevel(level);
                }
            }
        }

        private void start(ServerLevel level, int duration) {
            active = true;
            ticks = 0;
            remainingTicks = duration;
            windAngle = level.random.nextFloat() * Mth.TWO_PI;
            windTurnSpeed = 0.003F + level.random.nextFloat() * 0.003F;
        }

        private void stop() {
            active = false;
            ticks = 0;
            remainingTicks = 0;
        }
    }
}
