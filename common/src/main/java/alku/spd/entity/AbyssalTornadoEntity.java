package alku.spd.entity;

import alku.spd.registry.SpdEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public class AbyssalTornadoEntity extends Entity {
    private static final EntityDataAccessor<Float> WIND_X = SynchedEntityData.defineId(AbyssalTornadoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WIND_Z = SynchedEntityData.defineId(AbyssalTornadoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(AbyssalTornadoEntity.class, EntityDataSerializers.INT);
    private static final int DEFAULT_LIFETIME = 20 * 35;
    private static final double MOVE_SPEED = 0.045D;
    private static final double PULL_RADIUS = 5.75D;
    private static final double LIFT_STRENGTH = 0.18D;
    private static final DustParticleOptions BLOOD_DUST = new DustParticleOptions(new Vector3f(0.55F, 0.07F, 0.08F), 1.35F);

    public AbyssalTornadoEntity(EntityType<? extends AbyssalTornadoEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public AbyssalTornadoEntity(ServerLevel level, double x, double y, double z, Vec3 wind) {
        this(SpdEntities.ABYSSAL_TORNADO.get(), level);
        setPos(x, y, z);
        setWind(wind);
    }

    public AbyssalTornadoEntity(ServerLevel level, double x, double y, double z, Vec3 wind, int lifetimeTicks) {
        this(level, x, y, z, wind);
        setLifetime(lifetimeTicks);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(WIND_X, 0.0F);
        this.entityData.define(WIND_Z, 1.0F);
        this.entityData.define(LIFETIME, DEFAULT_LIFETIME);
    }

    public Vec3 getWind() {
        return new Vec3(this.entityData.get(WIND_X), 0.0D, this.entityData.get(WIND_Z));
    }

    public float getVisualHeight(float partialTick) {
        float age = this.tickCount + partialTick;
        float fadeIn = Mth.clamp(age / 30.0F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((getLifetime() - age) / 40.0F, 0.0F, 1.0F);
        return 9.0F * Math.min(fadeIn, fadeOut);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > getLifetime()) {
            discard();
            return;
        }

        if (this.level().isClientSide) {
            spawnStormParticles();
        } else {
            drift();
            pullNearbyEntities();
        }
    }

    private void spawnStormParticles() {
        float height = getVisualHeight(0.0F);
        if (height <= 0.05F) {
            return;
        }

        int count = 4 + this.level().random.nextInt(4);
        for (int i = 0; i < count; i++) {
            float t = 0.04F + this.level().random.nextFloat() * 0.92F;
            double radius = 0.7D + Math.pow(t, 1.45D) * 5.8D;
            double angle = this.level().random.nextDouble() * Mth.TWO_PI + this.tickCount * (0.09D + (1.0D - t) * 0.08D);
            double x = getX() + Math.cos(angle) * radius;
            double y = getY() + height * t + (this.level().random.nextDouble() - 0.5D) * 0.7D;
            double z = getZ() + Math.sin(angle) * radius;
            double tangentX = -Math.sin(angle) * (0.05D + (1.0D - t) * 0.08D);
            double tangentZ = Math.cos(angle) * (0.05D + (1.0D - t) * 0.08D);
            double inwardX = (getX() - x) * 0.006D;
            double inwardZ = (getZ() - z) * 0.006D;
            if (this.level().random.nextFloat() < 0.72F) {
                this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, tangentX + inwardX, 0.02D + t * 0.025D, tangentZ + inwardZ);
            } else if (this.level().random.nextFloat() < 0.55F) {
                this.level().addParticle(ParticleTypes.SMOKE, x, y, z, tangentX, 0.025D, tangentZ);
            } else {
                this.level().addParticle(BLOOD_DUST, x, y, z, tangentX * 0.7D, 0.01D, tangentZ * 0.7D);
            }
        }

        if (this.tickCount % 2 == 0) {
            double angle = this.level().random.nextDouble() * Mth.TWO_PI;
            double radius = 3.5D + this.level().random.nextDouble() * 3.6D;
            this.level().addParticle(ParticleTypes.CLOUD,
                    getX() + Math.cos(angle) * radius,
                    getY() + height * (0.76D + this.level().random.nextDouble() * 0.22D),
                    getZ() + Math.sin(angle) * radius,
                    -Math.sin(angle) * 0.04D,
                    0.01D,
                    Math.cos(angle) * 0.04D);
        }
    }

    private void drift() {
        Vec3 wind = getWind();
        double wobble = Math.sin(this.tickCount * 0.025D + getId() * 0.17D) * 0.018D;
        double dx = wind.x * MOVE_SPEED - wind.z * wobble;
        double dz = wind.z * MOVE_SPEED + wind.x * wobble;
        setDeltaMovement(dx, 0.0D, dz);
        move(MoverType.SELF, getDeltaMovement());
    }

    private void pullNearbyEntities() {
        AABB area = getBoundingBox().inflate(PULL_RADIUS, 8.0D, PULL_RADIUS);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area, target ->
                target.isAlive() && !target.isSpectator());
        for (LivingEntity target : entities) {
            double dx = getX() - target.getX();
            double dz = getZ() - target.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            if (horizontalDistance > PULL_RADIUS || horizontalDistance < 0.001D) {
                continue;
            }

            double normalizedX = dx / horizontalDistance;
            double normalizedZ = dz / horizontalDistance;
            double swirlX = -normalizedZ * 0.18D;
            double swirlZ = normalizedX * 0.18D;
            double pull = (1.0D - horizontalDistance / PULL_RADIUS) * 0.22D;
            double heightFactor = Mth.clamp((target.getY() - getY()) / 9.0D, 0.0D, 1.0D);
            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(
                    motion.x * 0.55D + normalizedX * pull + swirlX,
                    Math.min(0.82D, motion.y * 0.65D + LIFT_STRENGTH + heightFactor * 0.12D),
                    motion.z * 0.55D + normalizedZ * pull + swirlZ
            );
            target.fallDistance = 0.0F;
            target.hurtMarked = true;
        }
    }

    private void setWind(Vec3 wind) {
        Vec3 normalized = wind.horizontalDistanceSqr() < 0.0001D ? new Vec3(0.0D, 0.0D, 1.0D) : wind.normalize();
        this.entityData.set(WIND_X, (float) normalized.x);
        this.entityData.set(WIND_Z, (float) normalized.z);
    }

    private int getLifetime() {
        return this.entityData.get(LIFETIME);
    }

    private void setLifetime(int lifetimeTicks) {
        this.entityData.set(LIFETIME, Math.max(20, lifetimeTicks));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("Age");
        setWind(new Vec3(tag.getFloat("WindX"), 0.0D, tag.getFloat("WindZ")));
        if (tag.contains("Lifetime")) {
            setLifetime(tag.getInt("Lifetime"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.tickCount);
        tag.putInt("Lifetime", getLifetime());
        tag.putFloat("WindX", this.entityData.get(WIND_X));
        tag.putFloat("WindZ", this.entityData.get(WIND_Z));
    }
}
