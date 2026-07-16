package alku.spd.item;

final class GoldNameWave {
    static final long COLOR_CYCLE_MILLIS = 3000L;
    private static final int DARK_GOLD = 0xD18A00;
    private static final int LIGHT_GOLD = 0xFFE78A;

    private GoldNameWave() {
    }

    static int colorAt(long timeMillis) {
        double phase = Math.floorMod(timeMillis, COLOR_CYCLE_MILLIS) / (double) COLOR_CYCLE_MILLIS;
        double blend = (Math.sin(phase * Math.PI * 2.0D) + 1.0D) * 0.5D;
        return interpolateRgb(DARK_GOLD, LIGHT_GOLD, blend);
    }

    private static int interpolateRgb(int start, int end, double amount) {
        int red = interpolateChannel(start >> 16, end >> 16, amount);
        int green = interpolateChannel(start >> 8, end >> 8, amount);
        int blue = interpolateChannel(start, end, amount);
        return red << 16 | green << 8 | blue;
    }

    private static int interpolateChannel(int start, int end, double amount) {
        return (int) Math.round((start & 0xFF) + ((end & 0xFF) - (start & 0xFF)) * amount);
    }
}
