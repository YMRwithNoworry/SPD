package alku.spd.item;

public final class BlazingVeinDaggerState {
    public static final int MAX_LAYERS = 5;
    public static final long COMBO_TIMEOUT_TICKS = 16L;

    private BlazingVeinDaggerState() {
    }

    public static HitResult onHit(int layers, long lastHitTick, long currentTick) {
        int activeLayers = hasTimedOut(layers, lastHitTick, currentTick) ? 0 : clamp(layers);
        if (activeLayers >= MAX_LAYERS) {
            return new HitResult(0, true);
        }
        return new HitResult(activeLayers + 1, false);
    }

    public static boolean hasTimedOut(int layers, long lastHitTick, long currentTick) {
        return layers > 0 && (lastHitTick < 0L || currentTick < lastHitTick
                || currentTick - lastHitTick > COMBO_TIMEOUT_TICKS);
    }

    public static double attackSpeedMultiplier(int layers) {
        return Math.pow(1.04D, clamp(layers));
    }

    public static int clamp(int layers) {
        return Math.max(0, Math.min(MAX_LAYERS, layers));
    }

    public record HitResult(int layers, boolean instantSlash) {
    }
}
