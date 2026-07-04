package alku.spd.world;

import net.minecraft.world.level.Level;

@Deprecated
public final class SpdStormState {
    private SpdStormState() {
    }

    public static boolean isStorm(Level level) {
        return AbyssalGloomWeather.isActive(level);
    }

    public static void setActive(boolean active) {
        AbyssalGloomWeather.setClientState(active, 0, 0.0F, 1.0F);
    }
}
