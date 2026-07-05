package alku.spd.world;

import alku.spd.entity.SpdEntityTargeting;
import alku.spd.registry.SpdEffects;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class MoldCurseEvents {
    private static final Map<UUID, Integer> NEXT_PENALTY_TICKS = new HashMap<>();
    private static final int LEVEL_ONE_MIN_INTERVAL = 20 * 10;
    private static final int LEVEL_ONE_MAX_INTERVAL = 20 * 20;
    private static final int LEVEL_TWO_MIN_INTERVAL = 20 * 5;
    private static final int LEVEL_TWO_MAX_INTERVAL = 20 * 10;

    private MoldCurseEvents() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(MoldCurseEvents::tickLevel);
    }

    private static void tickLevel(ServerLevel level) {
        if (level.getGameTime() % 20 != 0) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> iterator = NEXT_PENALTY_TICKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            if (level.getEntity(entry.getKey()) == null) {
                iterator.remove();
            }
        }

        for (LivingEntity entity : level.getEntities(EntityTypeTest.forClass(LivingEntity.class), target ->
                target.hasEffect(SpdEffects.MOLD_CURSE.get()) && canCarryMoldCurse(target))) {
            MobEffectInstance curse = entity.getEffect(SpdEffects.MOLD_CURSE.get());
            if (curse == null) {
                continue;
            }

            int levelIndex = Math.min(1, curse.getAmplifier());
            UUID id = entity.getUUID();
            int nextTick = NEXT_PENALTY_TICKS.computeIfAbsent(id, ignored -> scheduleNext(level, levelIndex));
            if (entity.tickCount < nextTick) {
                continue;
            }

            applyPenalty(level, entity, levelIndex);
            if (levelIndex >= 1) {
                spreadMoldCurse(level, entity);
            }
            NEXT_PENALTY_TICKS.put(id, entity.tickCount + randomInterval(level, levelIndex));
        }
    }

    public static boolean canCarryMoldCurse(LivingEntity entity) {
        return !SpdEntityTargeting.isMoldEntity(entity);
    }

    private static int scheduleNext(ServerLevel level, int levelIndex) {
        return randomInterval(level, levelIndex);
    }

    private static int randomInterval(ServerLevel level, int levelIndex) {
        if (levelIndex >= 1) {
            return Mth.nextInt(level.random, LEVEL_TWO_MIN_INTERVAL, LEVEL_TWO_MAX_INTERVAL);
        }
        return Mth.nextInt(level.random, LEVEL_ONE_MIN_INTERVAL, LEVEL_ONE_MAX_INTERVAL);
    }

    private static void applyPenalty(ServerLevel level, LivingEntity entity, int levelIndex) {
        int option = levelIndex >= 1 ? level.random.nextInt(8) : level.random.nextInt(5);
        switch (option) {
            case 0 -> entity.setSecondsOnFire(levelIndex >= 1 ? 10 : 6);
            case 1 -> strikeLightning(level, entity, levelIndex >= 1);
            case 2 -> entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, levelIndex >= 1 ? 20 * 20 : 20 * 10, levelIndex >= 1 ? 2 : 1));
            case 3 -> entity.addEffect(new MobEffectInstance(MobEffects.POISON, levelIndex >= 1 ? 20 * 20 : 20 * 10, 0));
            case 4 -> trueDamage(level, entity, levelIndex >= 1 ? Mth.nextFloat(level.random, 10.0F, 15.0F) : Mth.nextFloat(level.random, 5.0F, 10.0F));
            case 5 -> entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 20, 0));
            case 6 -> entity.addEffect(new MobEffectInstance(SpdEffects.MOLD_CURSE.get(), 20 * 10, 1));
            case 7 -> trueDamage(level, entity, entity.getMaxHealth() * Mth.nextFloat(level.random, 0.1F, 0.2F));
            default -> {
            }
        }
    }

    private static void strikeLightning(ServerLevel level, LivingEntity entity, boolean levelTwo) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(entity.getX(), entity.getY(), entity.getZ());
            level.addFreshEntity(lightning);
        }
        if (levelTwo) {
            entity.hurt(level.damageSources().lightningBolt(), 2.0F);
        }
    }

    private static void trueDamage(ServerLevel level, LivingEntity entity, float amount) {
        entity.hurt(level.damageSources().magic(), amount);
    }

    private static void spreadMoldCurse(ServerLevel level, LivingEntity source) {
        AABB area = source.getBoundingBox().inflate(4.0D);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, target ->
                target != source && target.isAlive() && canCarryMoldCurse(target) && !target.hasEffect(SpdEffects.MOLD_CURSE.get()))) {
            target.addEffect(new MobEffectInstance(SpdEffects.MOLD_CURSE.get(), 20 * 30, 0), source);
        }
    }
}
