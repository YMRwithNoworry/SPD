package alku.spd.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public final class SpdDifficulty {
    private static final String DATA_NAME = "spd_difficulty";
    private static final String DIFFICULTY_KEY = "Difficulty";

    private SpdDifficulty() {
    }

    public static Difficulty get(MinecraftServer server) {
        return data(server).difficulty;
    }

    public static void set(MinecraftServer server, Difficulty difficulty) {
        Data data = data(server);
        if (data.difficulty != difficulty) {
            data.difficulty = difficulty;
            data.setDirty();
        }
    }

    public static String availableNames() {
        return String.join(", ", Arrays.stream(Difficulty.values()).map(Difficulty::serializedName).toList());
    }

    private static Data data(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(Data::load, Data::new, DATA_NAME);
    }

    public enum Difficulty {
        NOPRE("Nopre", 0.0D, 0.0D, 0.0D, 0.0D, 0.0F, -1, -1,
                new int[]{0}, new int[]{100}, 0.0F),
        CONSTITUTE("Constitute", 0.4D, 0.2D, 0.05D, 0.2D, 0.05F, -1, -1,
                new int[]{0, 1}, new int[]{90, 10}, 0.0F),
        EPCRISES("Epcrises", 1.0D, 0.6D, 0.0D, 0.5D, 0.1F, -1, -1,
                new int[]{0, 1}, new int[]{70, 30}, 0.0F),
        EPHVOL("Ephvol", 2.0D, 1.0D, 0.0D, 0.5D, 0.3F, 1, -1,
                new int[]{0, 1, 2}, new int[]{50, 40, 10}, 0.0F),
        EPDESPAIR("Epdespair", 5.0D, 3.0D, 0.0D, 1.0D, 0.5F, 2, 2,
                new int[]{1, 2}, new int[]{60, 40}, 20.0F),
        EPHIDIN("Ephidin", 10.0D, 5.0D, 0.0D, 2.0D, 0.8F, 3, 4,
                new int[]{2, 3}, new int[]{60, 40}, 40.0F);

        private final String serializedName;
        private final double healthBonus;
        private final double attackBonus;
        private final double armorBonus;
        private final double speedBonus;
        private final float damageImmunityChance;
        private final int moldResistanceAmplifier;
        private final int moldRegenerationAmplifier;
        private final int[] moldMutationAmplifiers;
        private final int[] moldMutationWeights;
        private final float wormVariantAttackDamage;

        Difficulty(String serializedName, double healthBonus, double attackBonus, double armorBonus, double speedBonus,
                   float damageImmunityChance, int moldResistanceAmplifier, int moldRegenerationAmplifier,
                   int[] moldMutationAmplifiers, int[] moldMutationWeights, float wormVariantAttackDamage) {
            this.serializedName = serializedName;
            this.healthBonus = healthBonus;
            this.attackBonus = attackBonus;
            this.armorBonus = armorBonus;
            this.speedBonus = speedBonus;
            this.damageImmunityChance = damageImmunityChance;
            this.moldResistanceAmplifier = moldResistanceAmplifier;
            this.moldRegenerationAmplifier = moldRegenerationAmplifier;
            this.moldMutationAmplifiers = moldMutationAmplifiers;
            this.moldMutationWeights = moldMutationWeights;
            this.wormVariantAttackDamage = wormVariantAttackDamage;
        }

        public String serializedName() {
            return serializedName;
        }

        public double healthBonus() {
            return healthBonus;
        }

        public double attackBonus() {
            return attackBonus;
        }

        public double armorBonus() {
            return armorBonus;
        }

        public double speedBonus() {
            return speedBonus;
        }

        public float damageImmunityChance() {
            return damageImmunityChance;
        }

        public int moldResistanceAmplifier() {
            return moldResistanceAmplifier;
        }

        public int moldRegenerationAmplifier() {
            return moldRegenerationAmplifier;
        }

        public float wormVariantAttackDamage() {
            return wormVariantAttackDamage;
        }

        public int randomMoldMutationAmplifier(net.minecraft.util.RandomSource random) {
            int totalWeight = 0;
            for (int weight : moldMutationWeights) {
                totalWeight += weight;
            }

            int roll = random.nextInt(totalWeight);
            for (int i = 0; i < moldMutationWeights.length; i++) {
                roll -= moldMutationWeights[i];
                if (roll < 0) {
                    return moldMutationAmplifiers[i];
                }
            }
            return moldMutationAmplifiers[0];
        }

        public static Optional<Difficulty> byName(String name) {
            String normalized = name.toLowerCase(Locale.ROOT);
            return Arrays.stream(values())
                    .filter(difficulty -> difficulty.serializedName.toLowerCase(Locale.ROOT).equals(normalized)
                            || difficulty.name().toLowerCase(Locale.ROOT).equals(normalized))
                    .findFirst();
        }
    }

    private static final class Data extends SavedData {
        private Difficulty difficulty = Difficulty.NOPRE;

        private static Data load(CompoundTag tag) {
            Data data = new Data();
            Difficulty.byName(tag.getString(DIFFICULTY_KEY)).ifPresent(difficulty -> data.difficulty = difficulty);
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putString(DIFFICULTY_KEY, difficulty.serializedName());
            return tag;
        }
    }
}
