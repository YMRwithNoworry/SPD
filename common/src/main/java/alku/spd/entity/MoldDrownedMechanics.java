package alku.spd.entity;

public final class MoldDrownedMechanics {
    public static final int TRANSFORMATION_TICKS = 20 * 30;
    public static final int GRUDGE_BOUND_TICKS = 20 * 20;
    public static final double WATER_SPEED_MULTIPLIER = 2.0D;
    public static final double SINK_SPEED = -0.08D;

    private MoldDrownedMechanics() {
    }

    public static int updateSubmergedTicks(int currentTicks, boolean submerged) {
        return submerged ? Math.min(TRANSFORMATION_TICKS, currentTicks + 1) : 0;
    }

    public static boolean shouldTransform(int submergedTicks) {
        return submergedTicks >= TRANSFORMATION_TICKS;
    }

    public static double sinkVelocity(double currentVelocity) {
        return Math.min(currentVelocity, SINK_SPEED);
    }
}
