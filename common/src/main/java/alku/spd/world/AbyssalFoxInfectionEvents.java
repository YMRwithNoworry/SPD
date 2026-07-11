package alku.spd.world;

import alku.spd.entity.AbyssalFoxEntity;
import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdTags;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.Set;

public final class AbyssalFoxInfectionEvents {
    private static final String PROGRESS_PREFIX = "spd_abyssal_fox_infection_";
    private static final String ACCELERATED_TAG = "spd_abyssal_fox_accelerated";
    private static final int CONVERSION_TICKS = 20 * 60 * 5;

    private AbyssalFoxInfectionEvents() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(AbyssalFoxInfectionEvents::tickLevel);
    }

    public static void markAccelerated(Fox fox) {
        fox.addTag(ACCELERATED_TAG);
    }

    private static void tickLevel(ServerLevel level) {
        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        for (Fox fox : level.getEntities(EntityTypeTest.forClass(Fox.class), fox ->
                fox.isAlive() && !(fox instanceof AbyssalFoxEntity))) {
            boolean accelerated = fox.getTags().contains(ACCELERATED_TAG);
            boolean inAbyssalBiome = level.getBiome(fox.blockPosition()).is(SpdTags.ABYSSAL_BIOMES);
            if (!accelerated && !inAbyssalBiome) {
                continue;
            }

            int progress = readProgress(fox.getTags()) + (accelerated ? 80 : 20);
            writeProgress(fox, progress);
            if (progress >= CONVERSION_TICKS) {
                fox.convertTo(SpdEntities.ABYSSAL_FOX.get(), true);
            }
        }
    }

    private static int readProgress(Set<String> tags) {
        for (String tag : tags) {
            if (!tag.startsWith(PROGRESS_PREFIX)) {
                continue;
            }
            try {
                return Integer.parseInt(tag.substring(PROGRESS_PREFIX.length()));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static void writeProgress(Fox fox, int progress) {
        fox.getTags().stream()
                .filter(tag -> tag.startsWith(PROGRESS_PREFIX))
                .toList()
                .forEach(fox::removeTag);
        fox.addTag(PROGRESS_PREFIX + progress);
    }
}
