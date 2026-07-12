package alku.spd.block;

final class AbyssalTurtleEggLogic {
    static final int MIN_EGGS = 1;
    static final int MAX_EGGS = 4;
    static final int MIN_HATCH = 0;
    static final int MAX_HATCH = 2;

    private AbyssalTurtleEggLogic() {
    }

    static int eggs(int value) {
        return Math.max(MIN_EGGS, Math.min(MAX_EGGS, value));
    }

    static int hatch(int value) {
        return Math.max(MIN_HATCH, Math.min(MAX_HATCH, value));
    }
}
