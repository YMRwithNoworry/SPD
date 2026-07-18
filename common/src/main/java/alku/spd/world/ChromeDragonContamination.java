package alku.spd.world;

import alku.spd.entity.SpdEntityTargeting;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Keeps a one-minute chrome contamination field at each dragon corpse. */
public final class ChromeDragonContamination {
    private static final int DURATION_TICKS = 20 * 60;
    private static final Map<ServerLevel, List<Zone>> ZONES = new WeakHashMap<>();

    private ChromeDragonContamination() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(ChromeDragonContamination::tickLevel);
    }

    public static void add(ServerLevel level, BlockPos center) {
        ZONES.computeIfAbsent(level, ignored -> new ArrayList<>())
                .add(new Zone(center.immutable(), level.getGameTime() + DURATION_TICKS));
    }

    private static void tickLevel(ServerLevel level) {
        List<Zone> zones = ZONES.get(level);
        if (zones == null || zones.isEmpty()) {
            return;
        }
        long gameTime = level.getGameTime();
        Iterator<Zone> iterator = zones.iterator();
        while (iterator.hasNext()) {
            Zone zone = iterator.next();
            if (gameTime >= zone.expireTick) {
                iterator.remove();
                continue;
            }
            if (gameTime % 20L != 0L) {
                continue;
            }
            AABB area = new AABB(zone.center).inflate(6.0D, 3.0D, 6.0D);
            for (LivingEntity living : level.getEntities(EntityTypeTest.forClass(LivingEntity.class), area, LivingEntity::isAlive)) {
                if (SpdEntityTargeting.isSpdEntity(living)) {
                    continue;
                }
                SpdCorrosion.addAbyssalPressure(living, 2, 80, null);
            }
        }
        if (zones.isEmpty()) {
            ZONES.remove(level);
        }
    }

    private record Zone(BlockPos center, long expireTick) {
    }
}
