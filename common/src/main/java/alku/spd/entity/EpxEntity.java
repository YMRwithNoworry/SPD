package alku.spd.entity;

import alku.spd.registry.SpdEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EpxEntity extends Entity {
    private static final int MAX_AGE = 20 * 60;

    public EpxEntity(EntityType<? extends EpxEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
    }

    public EpxEntity(ServerLevel level, double x, double y, double z) {
        this(SpdEntities.EPX.get(), level);
        setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > MAX_AGE) {
            discard();
            return;
        }

        if (level().isClientSide()) {
            return;
        }

        setDeltaMovement(getDeltaMovement().multiply(0.86D, 0.98D, 0.86D));
        move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
        if (onGround()) {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x, 0.0D, motion.z);
        }

        AABB area = getBoundingBox().inflate(0.08D);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area, target ->
                target.isAlive() && !SpdEntityTargeting.isMoldEntity(target));
        if (!targets.isEmpty()) {
            explode();
        }
    }

    private void explode() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.explode(this, getX(), getY(), getZ(), 2.0F, Level.ExplosionInteraction.MOB);
        for (int i = 0; i < 5; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double distance = 0.7D + this.random.nextDouble() * 1.4D;
            EpxCloudEntity cloud = new EpxCloudEntity(serverLevel,
                    getX() + Math.cos(angle) * distance,
                    getY() + 0.1D,
                    getZ() + Math.sin(angle) * distance);
            serverLevel.addFreshEntity(cloud);
        }
        discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.tickCount);
    }
}
