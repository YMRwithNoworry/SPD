package alku.spd.entity;

import alku.spd.registry.SpdEntities;
import alku.spd.world.EpxEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class EpxCloudEntity extends Entity {
    private static final EntityDataAccessor<Integer> EFFECT_ID = SynchedEntityData.defineId(EpxCloudEntity.class, EntityDataSerializers.INT);
    private static final int LIFETIME = 20 * 10;
    private static final double RADIUS = 1.75D;

    public EpxCloudEntity(EntityType<? extends EpxCloudEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public EpxCloudEntity(ServerLevel level, double x, double y, double z) {
        this(SpdEntities.EPX_CLOUD.get(), level);
        setPos(x, y, z);
        setEffect(EpxEvents.randomNegativeEffect(this.random));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(EFFECT_ID, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > LIFETIME) {
            discard();
            return;
        }

        if (level().isClientSide()) {
            spawnClientParticles();
            return;
        }

        if (this.tickCount % 20 == 1) {
            affectNearbyEntities();
        }
    }

    private void affectNearbyEntities() {
        AABB area = getBoundingBox().inflate(RADIUS, 0.6D, RADIUS);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area, target ->
                target.isAlive() && !SpdEntityTargeting.isMoldEntity(target) && target.distanceToSqr(this) <= RADIUS * RADIUS);
        MobEffect effect = MobEffect.byId(this.entityData.get(EFFECT_ID));
        if (effect == null) {
            effect = EpxEvents.randomNegativeEffect(this.random);
            setEffect(effect);
        }

        for (LivingEntity target : targets) {
            EpxEvents.applyNegativeBufactor(target, effect, this);
        }
    }

    private void spawnClientParticles() {
        for (int i = 0; i < 4; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double distance = this.random.nextDouble() * RADIUS;
            level().addParticle(ParticleTypes.WITCH,
                    getX() + Math.cos(angle) * distance,
                    getY() + 0.15D + this.random.nextDouble() * 0.35D,
                    getZ() + Math.sin(angle) * distance,
                    0.0D, 0.02D, 0.0D);
        }
    }

    private void setEffect(MobEffect effect) {
        this.entityData.set(EFFECT_ID, MobEffect.getId(effect));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("Age");
        this.entityData.set(EFFECT_ID, tag.getInt("Effect"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.tickCount);
        tag.putInt("Effect", this.entityData.get(EFFECT_ID));
    }
}
