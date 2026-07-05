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
        NOPRE("Nopre");

        private final String serializedName;

        Difficulty(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
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
