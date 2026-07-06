package alku.spd.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MoldZombieEntity extends Zombie implements GeoEntity {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(MoldZombieEntity.class, EntityDataSerializers.INT);
    private static final int VARIANT_UNSET = -1;
    private static final int VARIANT_WALKING = 0;
    private static final int VARIANT_RUNNING = 1;
    private static final double VANILLA_ZOMBIE_SPEED = 0.23D;
    private static final double WALKING_SPEED = VANILLA_ZOMBIE_SPEED * 1.2D;
    private static final double RUNNING_SPEED = VANILLA_ZOMBIE_SPEED * 2.4D;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("daizhe");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("paobu");
    private static final RawAnimation CHASE = RawAnimation.begin().thenLoop("追逐");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("gongji");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("siwang");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int appliedVariant = Integer.MIN_VALUE;

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
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, SpdEntityTargeting::isNonSpdLiving));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, VARIANT_UNSET);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
        this.ensureVariantAssigned();
        this.applyVariantAttributes();
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.ensureVariantAssigned();
            this.applyVariantAttributes();
        }
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
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (this.isDeadOrDying()) {
                state.setAndContinue(DEATH);
                return PlayState.CONTINUE;
            }

            if (this.swinging) {
                state.setAndContinue(ATTACK);
                return PlayState.CONTINUE;
            }

            if (state.isMoving()) {
                state.setAndContinue(this.isRunningVariant() ? CHASE : WALK);
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
}

