package alku.spd.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class SpdBigEyes {
    private static final String DATA_NAME = "spd_big_eyes";
    private static final String ACTIVE_KEY = "Active";
    private static volatile boolean clientActive;

    private SpdBigEyes() {
    }

    public static boolean isActive(MinecraftServer server) {
        return data(server).active;
    }

    public static void setActive(MinecraftServer server, boolean active) {
        Data data = data(server);
        if (data.active != active) {
            data.active = active;
            data.setDirty();
        }
    }

    public static boolean isClientActive() {
        return clientActive;
    }

    static void setClientActive(boolean active) {
        clientActive = active;
    }

    private static Data data(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(Data::load, Data::new, DATA_NAME);
    }

    private static final class Data extends SavedData {
        private boolean active;

        private static Data load(CompoundTag tag) {
            Data data = new Data();
            data.active = tag.getBoolean(ACTIVE_KEY);
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putBoolean(ACTIVE_KEY, active);
            return tag;
        }
    }
}
