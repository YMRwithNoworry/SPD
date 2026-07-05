package alku.spd.entity;

import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NamelessSlashEntity extends Projectile {
    private static final DustParticleOptions SLASH_DUST = new DustParticleOptions(new Vector3f(0.95F, 0.08F, 0.03F), 1.45F);
    private static final int LIFE_TICKS = 13;
    private static final float DAMAGE = 7.0F;
    private static final double SPEED = 0.62D;

    private final Set<Integer> hitEntities = new HashSet<>();

    public NamelessSlashEntity(EntityType<? extends NamelessSlashEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public NamelessSlashEntity(Level level, LivingEntity owner, Vec3 direction) {
        this(SpdEntities.NAMELESS_SLASH.get(), level);
        this.setOwner(owner);
        Vec3 normalized = direction.normalize();
        this.setDeltaMovement(normalized.scale(SPEED));
        this.setYRot((float) (Math.atan2(normalized.z, normalized.x) * (180.0D / Math.PI)) - 90.0F);
        this.setXRot((float) (-(Math.atan2(normalized.y, normalized.horizontalDistance()) * (180.0D / Math.PI))));
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 0.0001D) {
            this.discard();
            return;
        }

        Vec3 from = this.position();
        Vec3 to = from.add(movement);
        spawnTrail(from, to);

        if (!this.level().isClientSide) {
            hitEntities(from, to);
        }

        this.move(MoverType.SELF, movement);
        if (this.tickCount >= LIFE_TICKS) {
            this.discard();
        }
    }

    private void hitEntities(Vec3 from, Vec3 to) {
        Entity owner = this.getOwner();
        AABB area = new AABB(from, to).inflate(0.85D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, target ->
                target.isAlive() && target != owner && !this.hitEntities.contains(target.getId())
                        && target.getBoundingBox().inflate(0.35D).intersects(area));
        for (LivingEntity target : targets) {
            this.hitEntities.add(target.getId());
            float damage = DAMAGE;
            if (SpdEntityTargeting.isAbyssalEntity(target)) {
                damage *= 1.4F;
                target.addEffect(new MobEffectInstance(SpdEffects.EROSION_SUPPRESSION.get(), 20 * 5, 0), owner);
            }
            target.addEffect(new MobEffectInstance(SpdEffects.SEARING_PULSE.get(), 20 * 4, 0), owner);
            if (owner instanceof LivingEntity livingOwner) {
                target.hurt(this.level().damageSources().indirectMagic(this, livingOwner), damage);
            } else {
                target.hurt(this.level().damageSources().magic(), damage);
            }
        }
    }

    private void spawnTrail(Vec3 from, Vec3 to) {
        Vec3 step = to.subtract(from).scale(0.2D);
        for (int i = 0; i < 5; i++) {
            Vec3 point = from.add(step.scale(i));
            this.level().addParticle(
                    SLASH_DUST,
                    point.x + (this.random.nextDouble() - 0.5D) * 0.35D,
                    point.y + (this.random.nextDouble() - 0.5D) * 0.25D,
                    point.z + (this.random.nextDouble() - 0.5D) * 0.35D,
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !(target instanceof Player player && player == this.getOwner());
    }
}
