package alku.spd.entity;

import net.minecraft.world.Difficulty;

final class AbyssalTurtleMechanics {
    private static final int MAX_INFECTION = 6000;

    private AbyssalTurtleMechanics() {
    }

    static float biteDamage(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> 0.0F;
            case EASY -> 3.0F;
            case NORMAL -> 4.0F;
            case HARD -> 6.0F;
        };
    }

    static int attackInterval(boolean inWater) {
        return inWater ? 19 : 26;
    }

    static double armor(boolean inWater) {
        return inWater ? 8.0D : 10.0D;
    }

    static float shellDamageMultiplier() {
        return 0.3F;
    }

    static float fireDamageMultiplier() {
        return 0.6F;
    }

    static float purificationDamageMultiplier() {
        return 2.0F;
    }

    static int infectionProgress(int progress, boolean inInfectionBiome) {
        int change = inInfectionBiome ? 20 : -5;
        return Math.max(0, Math.min(MAX_INFECTION, progress + change));
    }
}
