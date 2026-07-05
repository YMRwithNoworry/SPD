package alku.spd.world;

import alku.spd.entity.EpxCloudEntity;
import alku.spd.entity.EpxEntity;
import alku.spd.entity.SpdEntityTargeting;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EpxEvents {
    private static final int KILLS_PER_EPX = 10;
    private static final int MIN_BUFACTOR_DELAY = 20 * 20;
    private static final int MAX_BUFACTOR_DELAY = 20 * 30;
    private static final int MOTHER_SCAN_INTERVAL = 20 * 120;
    private static final int MOTHER_SCAN_COUNT = 10;
    private static final int MOTHER_RELEASE_COST = 50;
    private static final int MOTHER_RELEASE_TARGETS = 25;
    private static final MobEffect[] POSITIVE_EFFECTS = {
            MobEffects.REGENERATION,
            MobEffects.MOVEMENT_SPEED,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.DAMAGE_BOOST,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.HEAL
    };
    private static final MobEffect[] NEGATIVE_EFFECTS = {
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.WEAKNESS,
            MobEffects.DARKNESS,
            MobEffects.BLINDNESS,
            MobEffects.HARM,
            MobEffects.CONFUSION
    };

    private EpxEvents() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(EpxEvents::tickLevel);
    }

    public static void onLivingDeath(LivingEntity victim, DamageSource source) {
        Level level = victim.level();
        if (level.isClientSide() || !(victim instanceof EpxCarrier carrier)) {
            return;
        }

        LivingEntity killer = findLivingKiller(victim, source);
        int epxCount = carrier.spd$getEpxCount();
        if (epxCount > 0) {
            carrier.spd$setEpxCount(0);
            if (SpdEntityTargeting.isMoldEntity(victim)) {
                transferMoldEpx(victim, killer);
            } else if (level instanceof ServerLevel serverLevel) {
                dropNonMoldEpx(serverLevel, victim, epxCount);
            }
        }

        if (killer != null && killer != victim && isMoldEpxEligible(killer) && killer instanceof EpxCarrier killerCarrier) {
            if (killerCarrier.spd$getEpxCount() <= 0) {
                int kills = killerCarrier.spd$getEpxKills() + 1;
                if (kills >= KILLS_PER_EPX) {
                    killerCarrier.spd$setEpxKills(0);
                    addEpx(killer, 1);
                } else {
                    killerCarrier.spd$setEpxKills(kills);
                }
            }
        }
    }

    public static void addEpx(LivingEntity entity, int amount) {
        if (!(entity instanceof EpxCarrier carrier) || amount <= 0) {
            return;
        }

        int current = carrier.spd$getEpxCount();
        int next = SpdEntityTargeting.isMoldEntity(entity) ? Math.min(1, current + amount) : current + amount;
        carrier.spd$setEpxCount(Math.max(0, next));
        scheduleNextBufactor(entity);
    }

    public static boolean isMoldEpxEligible(LivingEntity entity) {
        if (!SpdEntityTargeting.isMoldEntity(entity)) {
            return false;
        }

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) {
            return false;
        }

        String path = id.getPath();
        return !path.contains("organ") && !path.contains("mother");
    }

    public static MobEffect randomNegativeEffect(RandomSource random) {
        return NEGATIVE_EFFECTS[random.nextInt(NEGATIVE_EFFECTS.length)];
    }

    public static void applyNegativeBufactor(LivingEntity target, MobEffect effect, Entity source) {
        if (SpdEntityTargeting.isMoldEntity(target)) {
            return;
        }

        if (effect == MobEffects.HARM) {
            target.hurt(target.damageSources().magic(), 6.0F);
        } else if (effect == MobEffects.WEAKNESS) {
            target.addEffect(new MobEffectInstance(effect, 20 * 5, 1), source);
        } else if (effect == MobEffects.CONFUSION) {
            target.addEffect(new MobEffectInstance(effect, 20 * 10, 0), source);
        } else {
            target.addEffect(new MobEffectInstance(effect, 20 * 5, 0), source);
        }
    }

    private static void tickLevel(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (gameTime % 20 != 0) {
            return;
        }

        if (gameTime % MOTHER_SCAN_INTERVAL == 0L) {
            tickMothers(level);
        }

        for (LivingEntity entity : level.getEntities(EntityTypeTest.forClass(LivingEntity.class), target ->
                target.isAlive() && !isMoldMother(target) && target instanceof EpxCarrier carrier && carrier.spd$getEpxCount() > 0)) {
            tickCarrier(entity, gameTime);
        }
    }

    private static void tickMothers(ServerLevel level) {
        List<? extends LivingEntity> mothers = level.getEntities(EntityTypeTest.forClass(LivingEntity.class), target ->
                target.isAlive() && isMoldMother(target) && target instanceof EpxCarrier);
        if (mothers.isEmpty()) {
            return;
        }

        for (LivingEntity mother : mothers) {
            harvestMotherEpx(level, mother);
            releaseMotherEpx(level, mother);
        }
    }

    private static void harvestMotherEpx(ServerLevel level, LivingEntity mother) {
        List<LivingEntity> candidates = new ArrayList<>(level.getEntities(EntityTypeTest.forClass(LivingEntity.class), target ->
                target != mother && target.isAlive() && SpdEntityTargeting.isMoldEntity(target) && !isMoldMother(target)
                        && target instanceof EpxCarrier carrier && carrier.spd$getEpxCount() > 0));
        if (candidates.isEmpty() || !(mother instanceof EpxCarrier motherCarrier)) {
            return;
        }

        Collections.shuffle(candidates);
        int harvested = 0;
        for (LivingEntity candidate : candidates) {
            if (harvested >= MOTHER_SCAN_COUNT) {
                break;
            }

            EpxCarrier carrier = (EpxCarrier) candidate;
            carrier.spd$setEpxCount(0);
            harvested++;
        }

        if (harvested > 0) {
            motherCarrier.spd$setEpxCount(motherCarrier.spd$getEpxCount() + harvested);
        }
    }

    private static void releaseMotherEpx(ServerLevel level, LivingEntity mother) {
        if (!(mother instanceof EpxCarrier motherCarrier) || motherCarrier.spd$getEpxCount() < MOTHER_RELEASE_COST) {
            return;
        }

        motherCarrier.spd$setEpxCount(motherCarrier.spd$getEpxCount() - MOTHER_RELEASE_COST);
        List<LivingEntity> targets = new ArrayList<>(level.getEntities(EntityTypeTest.forClass(LivingEntity.class), target ->
                target.isAlive() && !SpdEntityTargeting.isMoldEntity(target)));
        if (targets.isEmpty()) {
            return;
        }

        Collections.shuffle(targets);
        int affected = Math.min(MOTHER_RELEASE_TARGETS, targets.size());
        for (int i = 0; i < affected; i++) {
            applyMotherDebuff(targets.get(i), randomNegativeEffect(mother.getRandom()), mother);
        }
    }

    private static void applyMotherDebuff(LivingEntity target, MobEffect effect, Entity source) {
        if (effect == MobEffects.HARM) {
            target.hurt(target.damageSources().magic(), 6.0F);
        } else if (effect == MobEffects.WEAKNESS) {
            target.addEffect(new MobEffectInstance(effect, 20 * 10, 1), source);
        } else {
            target.addEffect(new MobEffectInstance(effect, 20 * 10, 0), source);
        }
    }

    private static void tickCarrier(LivingEntity entity, long gameTime) {
        EpxCarrier carrier = (EpxCarrier) entity;
        if (carrier.spd$getNextBufactorTick() <= 0L) {
            scheduleNextBufactor(entity);
            return;
        }

        if (gameTime < carrier.spd$getNextBufactorTick()) {
            return;
        }

        int rounds = SpdEntityTargeting.isMoldEntity(entity) ? 1 : carrier.spd$getEpxCount();
        for (int i = 0; i < rounds; i++) {
            if (SpdEntityTargeting.isMoldEntity(entity)) {
                applyPositiveBufactors(entity);
            } else {
                applyNegativeBufactors(entity);
            }
        }
        scheduleNextBufactor(entity);
    }

    private static void applyPositiveBufactors(LivingEntity entity) {
        RandomSource random = entity.getRandom();
        List<MobEffect> effects = drawEffects(POSITIVE_EFFECTS, random, 2 + random.nextInt(3));
        for (MobEffect effect : effects) {
            if (effect == MobEffects.HEAL) {
                entity.heal(8.0F);
            } else if (effect == MobEffects.REGENERATION) {
                entity.addEffect(new MobEffectInstance(effect, 20 * 5, 2), entity);
            } else if (effect == MobEffects.MOVEMENT_SPEED || effect == MobEffects.DAMAGE_BOOST) {
                entity.addEffect(new MobEffectInstance(effect, 20 * 5, 1), entity);
            } else if (effect == MobEffects.DAMAGE_RESISTANCE) {
                entity.addEffect(new MobEffectInstance(effect, 20 * 10, 0), entity);
            } else if (effect == MobEffects.FIRE_RESISTANCE) {
                entity.addEffect(new MobEffectInstance(effect, 20 * 5, 0), entity);
            }
        }
    }

    private static void applyNegativeBufactors(LivingEntity entity) {
        RandomSource random = entity.getRandom();
        List<MobEffect> effects = drawEffects(NEGATIVE_EFFECTS, random, 2 + random.nextInt(3));
        for (MobEffect effect : effects) {
            applyNegativeBufactor(entity, effect, entity);
        }
    }

    private static List<MobEffect> drawEffects(MobEffect[] source, RandomSource random, int count) {
        List<MobEffect> pool = new ArrayList<>(List.of(source));
        List<MobEffect> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (pool.isEmpty() || random.nextFloat() < 0.12F) {
                result.add(source[random.nextInt(source.length)]);
                continue;
            }

            result.add(pool.remove(random.nextInt(pool.size())));
        }
        return result;
    }

    private static void scheduleNextBufactor(LivingEntity entity) {
        if (!(entity instanceof EpxCarrier carrier)) {
            return;
        }

        int delay = MIN_BUFACTOR_DELAY + entity.getRandom().nextInt(MAX_BUFACTOR_DELAY - MIN_BUFACTOR_DELAY + 1);
        carrier.spd$setNextBufactorTick(entity.level().getGameTime() + delay);
    }

    private static boolean isMoldMother(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) {
            return false;
        }

        String path = id.getPath();
        return SpdEntityTargeting.isMoldEntity(entity) && (path.contains("mother") || path.contains("matriarch"));
    }

    private static void transferMoldEpx(LivingEntity victim, LivingEntity killer) {
        if (killer == null || killer == victim || victim.getRandom().nextFloat() >= 0.5F) {
            return;
        }

        addEpx(killer, 1);
    }

    private static void dropNonMoldEpx(ServerLevel level, LivingEntity victim, int epxCount) {
        RandomSource random = victim.getRandom();
        for (int i = 0; i < epxCount; i++) {
            if (random.nextFloat() >= 0.4F) {
                continue;
            }

            EpxEntity epx = new EpxEntity(level, victim.getX(), victim.getY() + 0.1D, victim.getZ());
            epx.setDeltaMovement((random.nextDouble() - 0.5D) * 0.18D, 0.12D, (random.nextDouble() - 0.5D) * 0.18D);
            level.addFreshEntity(epx);
        }
    }

    private static LivingEntity findLivingKiller(LivingEntity victim, DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingAttacker && livingAttacker != victim) {
            return livingAttacker;
        }

        Entity direct = source.getDirectEntity();
        if (direct instanceof LivingEntity directLiving && directLiving != victim) {
            return directLiving;
        }

        return null;
    }
}
