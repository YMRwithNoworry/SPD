package alku.spd.world;

import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdEntities;
import alku.spd.Spd;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.Zombie;
import alku.spd.entity.AbyssalFoxEntity;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.entity.AbyssalWolfEntity;
import net.minecraft.world.entity.animal.Wolf;

import java.util.UUID;

public final class AbyssalPressureEvents {
    private static final int ENVIRONMENT_INTERVAL = 20;
    private static final int DECAY_INTERVAL = 20 * 3;
    private static final ResourceKey<DamageType> CORROSION_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(Spd.MOD_ID, "abyssal_corrosion"));
    private static final UUID ATTACK_DAMAGE_PENALTY_ID = UUID.fromString("6ec1c851-e970-4b59-9858-ae067771180b");
    private static final AttributeModifier ATTACK_DAMAGE_PENALTY = new AttributeModifier(
            ATTACK_DAMAGE_PENALTY_ID,
            "SPD abyssal pressure attack penalty",
            -0.1D,
            AttributeModifier.Operation.MULTIPLY_TOTAL);

    private AbyssalPressureEvents() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(AbyssalPressureEvents::tickLevel);
    }

    private static void tickLevel(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (gameTime % ENVIRONMENT_INTERVAL != 0L) {
            return;
        }

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
                continue;
            }

            boolean exposed = SpdCorrosion.isInAbyssalEnvironment(living);
            if (exposed) {
                SpdCorrosion.addAbyssalPressure(living, 1);
            } else if (gameTime % DECAY_INTERVAL == 0L) {
                SpdCorrosion.reduceAbyssalPressureLayer(living);
            }

            applyTierEffects(level, living);
            applyRending(level, living);
            tryConvert(living);
        }
    }

    private static void applyTierEffects(ServerLevel level, LivingEntity living) {
        int layers = SpdCorrosion.getAbyssalPressureLayers(living);
        updateAttackPenalty(living, layers >= 4);
        if (layers < 4) {
            return;
        }

        if (layers >= 7) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, false, false));
        } else {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0, true, false, false));
        }

        boolean damageTick = layers >= 7 || level.getGameTime() % 40L == 0L;
        if (damageTick) {
            float damage = layers >= 7 ? (layers == 10 ? 2.25F : 1.5F) : 1.0F;
            DamageSource source = new DamageSource(level.registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(CORROSION_DAMAGE));
            living.hurt(source, damage);
        }

        level.sendParticles(ParticleTypes.ASH,
                living.getX(), living.getY() + living.getBbHeight() * 0.55D, living.getZ(),
                layers >= 7 ? 8 : 3,
                living.getBbWidth() * 0.45D, living.getBbHeight() * 0.35D,
                living.getBbWidth() * 0.45D, 0.01D);
    }

    private static void updateAttackPenalty(LivingEntity living, boolean active) {
        AttributeInstance attackDamage = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }
        boolean applied = attackDamage.getModifier(ATTACK_DAMAGE_PENALTY_ID) != null;
        if (active && !applied) {
            attackDamage.addTransientModifier(ATTACK_DAMAGE_PENALTY);
        } else if (!active && applied) {
            attackDamage.removeModifier(ATTACK_DAMAGE_PENALTY_ID);
        }
    }

    private static void tryConvert(LivingEntity living) {
        if (SpdCorrosion.getAbyssalPressureLayers(living) < SpdCorrosion.MAX_ABYSSAL_PRESSURE_LAYERS) {
            return;
        }
        if (living instanceof Fox fox && !(fox instanceof AbyssalFoxEntity)) {
            fox.convertTo(SpdEntities.ABYSSAL_FOX.get(), true);
        } else if (living instanceof Zombie zombie && !(zombie instanceof MoldZombieEntity)) {
            zombie.convertTo(SpdEntities.MOLD_ZOMBIE.get(), true);
        } else if (living instanceof Wolf wolf && !(wolf instanceof AbyssalWolfEntity) && !wolf.isTame()) {
            wolf.convertTo(SpdEntities.ABYSSAL_WOLF.get(), true);
        }
    }

    private static void applyRending(ServerLevel level, LivingEntity living) {
        if (!living.hasEffect(SpdEffects.RENDING.get()) || level.getGameTime() % 20L != 0L) {
            return;
        }
        double horizontalSpeed = living.getDeltaMovement().horizontalDistanceSqr();
        float damage = horizontalSpeed > 1.0E-4D ? 1.0F : 0.5F;
        DamageSource source = new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(CORROSION_DAMAGE));
        living.hurt(source, damage);
    }
}
