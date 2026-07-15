package alku.spd.entity;

public final class MoldHuskMechanics {
    public static final int NO_KILL_TRANSFORMATION_TICKS = 20 * 60 * 5;
    public static final int SLOWNESS_TICKS = 20 * 30;
    public static final int BLINDNESS_TICKS = 20 * 10;

    private MoldHuskMechanics() {
    }

    public static int advanceNoKillTicks(int currentTicks) {
        return Math.min(NO_KILL_TRANSFORMATION_TICKS, currentTicks + 1);
    }

    public static int resetNoKillTicks() {
        return 0;
    }

    public static boolean shouldTransform(int noKillTicks) {
        return noKillTicks >= NO_KILL_TRANSFORMATION_TICKS;
    }
}
