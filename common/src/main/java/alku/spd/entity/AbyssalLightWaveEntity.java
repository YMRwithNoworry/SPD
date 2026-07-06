package alku.spd.entity;

import alku.spd.registry.SpdEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;

public class AbyssalLightWaveEntity extends AbstractHurtingProjectile {
    private static final DustParticleOptions ABYSSAL_WAVE_DUST = new DustParticleOptions(new Vector3f(0.78F, 0.04F, 0.02F), 1.35F);
    private static final float DAMAGE = 20.0F;

    public AbyssalLightWaveEntity(EntityType<? extends AbyssalLightWaveEntity> entityType, Level level) {
        super(entityType, level);
    }

    public AbyssalLightWaveEntity(Level level, LivingEntity owner, double xPower, double yPower, double zPower) {
        super(SpdEntities.ABYSSAL_LIGHT_WAVE.get(), owner, xPower, yPower, zPower, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(
                        ABYSSAL_WAVE_DUST,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.35D,
                        this.getY() + (this.random.nextDouble() - 0.5D) * 0.35D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.35D,
                        -this.xPower * 0.1D,
                        -this.yPower * 0.1D,
                        -this.zPower * 0.1D);
            }
        }

        if (this.tickCount > 80) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity livingOwner) {
            target.hurt(this.level().damageSources().mobProjectile(this, livingOwner), DAMAGE);
        } else {
            target.hurt(this.level().damageSources().magic(), DAMAGE);
        }
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() != HitResult.Type.ENTITY) {
            this.discard();
        }
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ABYSSAL_WAVE_DUST;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }
}
