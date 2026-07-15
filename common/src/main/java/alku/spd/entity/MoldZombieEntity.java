package alku.spd.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class MoldZombieEntity extends Zombie implements GeoEntity {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(MoldZombieEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> POUNCER_VARIANT = SynchedEntityData.defineId(MoldZombieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DROWNED_VARIANT = SynchedEntityData.defineId(MoldZombieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HUSK_VARIANT = SynchedEntityData.defineId(MoldZombieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int VARIANT_UNSET = -1;
    private static final int VARIANT_WALKING = 0;
    private static final int VARIANT_RUNNING = 1;
    private static final double VANILLA_ZOMBIE_SPEED = 0.23D;
    private static final double WALKING_SPEED = VANILLA_ZOMBIE_SPEED * 1.2D;
    private static final double RUNNING_SPEED = VANILLA_ZOMBIE_SPEED * 2.4D;
    private static final float POUNCER_CHANCE = 0.2F;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.mold_zombie.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.mold_zombie.walk");
    private static final RawAnimation CHASE = RawAnimation.begin().thenLoop("animation.mold_zombie.chase");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.mold_zombie.attack");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("animation.mold_zombie.death");
    private static final RawAnimation DROWNED_IDLE = RawAnimation.begin().thenLoop("daizhe");
    private static final RawAnimation DROWNED_SWIM = RawAnimation.begin().thenLoop("paobu");
    private static final RawAnimation DROWNED_CHASE = RawAnimation.begin().thenLoop("追逐");
    private static final RawAnimation DROWNED_ATTACK = RawAnimation.begin().thenLoop("gongji");
    private static final RawAnimation HUSK_IDLE = RawAnimation.begin().thenLoop("daizhe");
    private static final RawAnimation HUSK_WALK = RawAnimation.begin().thenLoop("paobu");
    private static final RawAnimation HUSK_CHASE = RawAnimation.begin().thenLoop("追逐");
    private static final RawAnimation HUSK_ATTACK = RawAnimation.begin().thenLoop("gongji");
    private static final RawAnimation HUSK_DEATH = RawAnimation.begin().thenPlayAndHold("siwang");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int appliedVariant = Integer.MIN_VALUE;
    private int pounceCooldown;
    private int submergedTicks;
    private int noKillTicks;

    public MoldZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, WALKING_SPEED);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PounceGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, SpdEntityTargeting::isNonSpdLiving));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, VARIANT_UNSET);
        this.entityData.define(POUNCER_VARIANT, false);
        this.entityData.define(DROWNED_VARIANT, false);
        this.entityData.define(HUSK_VARIANT, false);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
        this.ensureVariantAssigned();
        this.entityData.set(POUNCER_VARIANT, this.random.nextFloat() < POUNCER_CHANCE);
        this.applyVariantAttributes();
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.ensureVariantAssigned();
            this.applyVariantAttributes();
            if (this.pounceCooldown > 0) {
                this.pounceCooldown--;
            }
            if (!this.isDrownedVariant()) {
                this.submergedTicks = MoldDrownedMechanics.updateSubmergedTicks(this.submergedTicks, this.isUnderWater());
                if (MoldDrownedMechanics.shouldTransform(this.submergedTicks)) {
                    this.transformIntoDrowned();
                }
            }
            if (!this.isHuskVariant()) {
                this.noKillTicks = MoldHuskMechanics.advanceNoKillTicks(this.noKillTicks);
                if (MoldHuskMechanics.shouldTransform(this.noKillTicks)) {
                    this.transformIntoHusk();
                }
            }
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isDrownedVariant() && this.isInWater() && this.isEffectiveAi()) {
            this.moveRelative((float) (0.02D * MoldDrownedMechanics.WATER_SPEED_MULTIPLIER), travelVector);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return this.isDrownedVariant() || super.canBreatheUnderwater();
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof Player player) {
            if (this.isDrownedVariant()) {
                player.addEffect(new MobEffectInstance(alku.spd.registry.SpdEffects.GRUDGE_BOUND.get(),
                        MoldDrownedMechanics.GRUDGE_BOUND_TICKS, 0), this);
            } else if (this.isHuskVariant()) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                        MoldHuskMechanics.SLOWNESS_TICKS, 1), this);
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
                        MoldHuskMechanics.BLINDNESS_TICKS, 0), this);
            }
        }
        return hurt;
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    private void ensureVariantAssigned() {
        if (this.entityData.get(VARIANT) == VARIANT_UNSET) {
            this.entityData.set(VARIANT, this.random.nextBoolean() ? VARIANT_WALKING : VARIANT_RUNNING);
        }
    }

    private boolean isWalkingVariant() {
        return this.entityData.get(VARIANT) == VARIANT_WALKING;
    }

    private boolean isRunningVariant() {
        return this.entityData.get(VARIANT) == VARIANT_RUNNING;
    }

    public boolean isPouncerVariant() {
        return this.entityData.get(POUNCER_VARIANT);
    }

    public boolean isDrownedVariant() {
        return this.entityData.get(DROWNED_VARIANT);
    }

    public boolean isHuskVariant() {
        return this.entityData.get(HUSK_VARIANT);
    }

    public void onConfirmedKill(LivingEntity killedEntity) {
        if (!this.level().isClientSide() && !this.isHuskVariant()) {
            this.noKillTicks = MoldHuskMechanics.resetNoKillTicks();
        }
    }

    private void transformIntoDrowned() {
        if (this.isHuskVariant()) {
            return;
        }
        this.entityData.set(DROWNED_VARIANT, true);
        this.entityData.set(POUNCER_VARIANT, false);
        this.submergedTicks = 0;
        this.enableDrownedMovement();
        this.refreshDimensions();
    }

    private void transformIntoHusk() {
        this.entityData.set(HUSK_VARIANT, true);
        this.entityData.set(DROWNED_VARIANT, false);
        this.entityData.set(POUNCER_VARIANT, false);
        this.submergedTicks = 0;
        this.noKillTicks = 0;
        this.enableHuskMovement();
        this.refreshDimensions();
    }

    private void enableDrownedMovement() {
        if (!(this.navigation instanceof AmphibiousPathNavigation)) {
            this.navigation = new AmphibiousPathNavigation(this, this.level());
        }
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    private void enableHuskMovement() {
        if (this.navigation instanceof AmphibiousPathNavigation) {
            this.navigation = new GroundPathNavigation(this, this.level());
        }
    }

    private void applyVariantAttributes() {
        int variant = this.entityData.get(VARIANT);
        if (variant == this.appliedVariant || variant == VARIANT_UNSET || this.getAttribute(Attributes.MOVEMENT_SPEED) == null) {
            return;
        }

        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(variant == VARIANT_RUNNING ? RUNNING_SPEED : WALKING_SPEED);
        this.appliedVariant = variant;
    }

    @Override
    public boolean isSilent() {
        return this.isWalkingVariant() || super.isSilent();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isWalkingVariant() ? null : super.getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return this.isWalkingVariant() ? null : super.getHurtSound(damageSource);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isWalkingVariant() ? null : super.getDeathSound();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (!this.isWalkingVariant()) {
            super.playStepSound(pos, state);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MoldZombieVariant", this.entityData.get(VARIANT));
        tag.putBoolean("MoldZombiePouncerVariant", this.isPouncerVariant());
        tag.putBoolean("MoldZombieDrownedVariant", this.isDrownedVariant());
        tag.putInt("MoldZombieSubmergedTicks", this.submergedTicks);
        tag.putBoolean("MoldZombieHuskVariant", this.isHuskVariant());
        tag.putInt("MoldZombieNoKillTicks", this.noKillTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MoldZombieVariant")) {
            int variant = tag.getInt("MoldZombieVariant");
            this.entityData.set(VARIANT, variant == VARIANT_RUNNING ? VARIANT_RUNNING : VARIANT_WALKING);
            this.appliedVariant = Integer.MIN_VALUE;
            this.applyVariantAttributes();
        }
        if (tag.contains("MoldZombiePouncerVariant")) {
            this.entityData.set(POUNCER_VARIANT, tag.getBoolean("MoldZombiePouncerVariant"));
        }
        this.entityData.set(DROWNED_VARIANT, tag.getBoolean("MoldZombieDrownedVariant"));
        this.submergedTicks = Math.max(0, tag.getInt("MoldZombieSubmergedTicks"));
        this.entityData.set(HUSK_VARIANT, tag.getBoolean("MoldZombieHuskVariant"));
        this.noKillTicks = Math.max(0, tag.getInt("MoldZombieNoKillTicks"));
        if (this.isHuskVariant()) {
            this.enableHuskMovement();
        } else if (this.isDrownedVariant()) {
            this.enableDrownedMovement();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (this.isHuskVariant()) {
                if (this.isDeadOrDying()) {
                    state.setAndContinue(HUSK_DEATH);
                    return PlayState.CONTINUE;
                }
                if (this.swinging) {
                    state.setAndContinue(HUSK_ATTACK);
                    return PlayState.CONTINUE;
                }
                Vec3 huskMovement = this.getDeltaMovement();
                boolean huskMoving = state.isMoving()
                        || huskMovement.x * huskMovement.x + huskMovement.z * huskMovement.z > 1.0E-5D;
                if (this.isRunningVariant() && huskMoving) {
                    state.setAndContinue(HUSK_CHASE);
                } else if (huskMoving) {
                    state.setAndContinue(HUSK_WALK);
                } else {
                    state.setAndContinue(HUSK_IDLE);
                }
                return PlayState.CONTINUE;
            }
            if (this.isDrownedVariant()) {
                if (this.swinging) {
                    state.setAndContinue(DROWNED_ATTACK);
                    return PlayState.CONTINUE;
                }
                Vec3 drownedMovement = this.getDeltaMovement();
                boolean drownedMoving = state.isMoving() || drownedMovement.lengthSqr() > 1.0E-5D;
                if (this.getTarget() != null && drownedMoving) {
                    state.setAndContinue(DROWNED_CHASE);
                } else if (drownedMoving) {
                    state.setAndContinue(DROWNED_SWIM);
                } else {
                    state.setAndContinue(DROWNED_IDLE);
                }
                return PlayState.CONTINUE;
            }
            if (this.isDeadOrDying()) {
                state.setAndContinue(DEATH);
                return PlayState.CONTINUE;
            }

            if (this.swinging) {
                state.setAndContinue(ATTACK);
                return PlayState.CONTINUE;
            }

            Vec3 movement = this.getDeltaMovement();
            boolean moving = state.isMoving()
                    || movement.x * movement.x + movement.z * movement.z > 1.0E-5D;

            if (this.isRunningVariant() && moving) {
                state.setAndContinue(CHASE);
                return PlayState.CONTINUE;
            }

            if (moving) {
                state.setAndContinue(WALK);
                return PlayState.CONTINUE;
            }

            state.setAndContinue(IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private static final class PounceGoal extends Goal {
        private static final double MIN_DISTANCE_SQR = 9.0D;
        private static final double MAX_DISTANCE_SQR = 36.0D;
        private static final int MAX_POUNCE_TICKS = 24;
        private static final int POUNCE_COOLDOWN_TICKS = 80;

        private final MoldZombieEntity zombie;
        private Player target;
        private int pounceTicks;
        private boolean hit;

        private PounceGoal(MoldZombieEntity zombie) {
            this.zombie = zombie;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.zombie.isPouncerVariant() || this.zombie.pounceCooldown > 0 || !this.zombie.onGround()) {
                return false;
            }
            if (!(this.zombie.getTarget() instanceof Player player) || !player.isAlive()) {
                return false;
            }
            double distanceSqr = this.zombie.distanceToSqr(player);
            if (distanceSqr < MIN_DISTANCE_SQR || distanceSqr > MAX_DISTANCE_SQR || !this.zombie.hasLineOfSight(player)) {
                return false;
            }
            this.target = player;
            return true;
        }

        @Override
        public void start() {
            Vec3 direction = this.target.position().subtract(this.zombie.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
            this.zombie.getNavigation().stop();
            this.zombie.setDeltaMovement(direction.x * 1.05D, 0.48D, direction.z * 1.05D);
            this.pounceTicks = MAX_POUNCE_TICKS;
            this.hit = false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive() && this.pounceTicks > 0 && !this.hit;
        }

        @Override
        public void tick() {
            this.pounceTicks--;
            this.zombie.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            if (this.zombie.distanceToSqr(this.target) <= 2.25D && this.zombie.doHurtTarget(this.target)) {
                this.target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0), this.zombie);
                this.hit = true;
                return;
            }
            if (this.pounceTicks < MAX_POUNCE_TICKS - 3 && this.zombie.onGround()) {
                this.pounceTicks = 0;
            }
        }

        @Override
        public void stop() {
            this.zombie.pounceCooldown = POUNCE_COOLDOWN_TICKS;
            this.target = null;
        }
    }
}
