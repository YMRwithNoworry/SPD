package alku.spd.world;

import alku.spd.entity.AbyssalWolfEntity;
import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdTags;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.Set;

public final class AbyssalWolfInfectionEvents {
    private static final String PROGRESS_PREFIX = "spd_abyssal_wolf_infection_";
    private static final String ACCELERATED_TAG = "spd_abyssal_wolf_accelerated";
    private static final int CONVERSION_TICKS = 20 * 60 * 5;

    private AbyssalWolfInfectionEvents() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(AbyssalWolfInfectionEvents::tickLevel);
    }

    public static void markAccelerated(Wolf wolf) {
        wolf.addTag(ACCELERATED_TAG);
    }

    private static void tickLevel(ServerLevel level) {
        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        for (Wolf wolf : level.getEntities(EntityTypeTest.forClass(Wolf.class),
                wolf -> wolf.isAlive() && !(wolf instanceof AbyssalWolfEntity) && !wolf.isTame())) {
            boolean accelerated = wolf.getTags().contains(ACCELERATED_TAG);
            boolean inAbyssalBiome = level.getBiome(wolf.blockPosition()).is(SpdTags.ABYSSAL_BIOMES);
            if (!accelerated && !inAbyssalBiome) {
                continue;
            }

            int progress = readProgress(wolf.getTags()) + (accelerated ? 80 : 20);
            writeProgress(wolf, progress);
            if (progress >= CONVERSION_TICKS) {
                wolf.convertTo(SpdEntities.ABYSSAL_WOLF.get(), true);
            }
        }
    }

    private static int readProgress(Set<String> tags) {
        for (String tag : tags) {
            if (!tag.startsWith(PROGRESS_PREFIX)) continue;
            try {
                return Integer.parseInt(tag.substring(PROGRESS_PREFIX.length()));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static void writeProgress(Wolf wolf, int progress) {
        wolf.getTags().stream().filter(tag -> tag.startsWith(PROGRESS_PREFIX)).toList().forEach(wolf::removeTag);
        wolf.addTag(PROGRESS_PREFIX + progress);
    }
}
