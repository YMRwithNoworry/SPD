package alku.spd.entity;

import alku.spd.registry.SpdEffects;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class AbyssalLizardEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(AbyssalLizardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("qi xi");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fei xing");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("xing zou");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("gong ji");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("si wang");
    private static final int ROAR_DURATION = 64;
    private static final int ROAR_COOLDOWN = 240;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int roarTicks;
    private int roarCooldown = 80;

    public AbyssalLizardEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_SPEED, 0.35D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FLYING_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RoarGoal(this));
        this.goalSelector.addGoal(2, new ChargedLightWaveGoal(this));
        this.goalSelector.addGoal(3, new SlowMeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.75D, 80));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 0.9D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, SpdEntityTargeting::isNonSpdLiving));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROARING, false);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (!this.level().isClientSide) {
            if (this.roarCooldown > 0) {
                this.roarCooldown--;
            }
            if (this.roarTicks > 0) {
                this.roarTicks--;
                this.entityData.set(ROARING, true);
                if (this.roarTicks == ROAR_DURATION - 18 || this.roarTicks % 20 == 0) {
                    this.applyRoar();
                }
            } else {
                this.entityData.set(ROARING, false);
            }
        }
    }

    public boolean isRoaring() {
        return this.entityData.get(ROARING);
    }

    private boolean canRoar() {
        return this.roarCooldown <= 0 && this.getTarget() != null && this.getTarget().isAlive();
    }

    private void startRoar() {
        this.roarTicks = ROAR_DURATION;
        this.roarCooldown = ROAR_COOLDOWN;
        this.navigation.stop();
    }

    private void applyRoar() {
        AABB range = this.getBoundingBox().inflate(18.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, range, target ->
                target != this && SpdEntityTargeting.isNonSpdLiving(target));
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(SpdEffects.SUBJUGATION.get(), 180, 0), this);
            if (target.isUsingItem()) {
                target.stopUsingItem();
            }
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

            if (this.isRoaring()) {
                state.setAndContinue(IDLE);
                return PlayState.CONTINUE;
            }

            if (!this.onGround() || Math.abs(this.getDeltaMovement().y) > 0.03D) {
                state.setAndContinue(FLY);
                return PlayState.CONTINUE;
            }

            if (state.isMoving()) {
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

    private static final class SlowMeleeAttackGoal extends MeleeAttackGoal {
        private SlowMeleeAttackGoal(AbyssalLizardEntity mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        protected int getAttackInterval() {
            return 50;
        }
    }

    private static final class ChargedLightWaveGoal extends Goal {
        private final AbyssalLizardEntity mob;
        private int chargeTicks;
        private int cooldownTicks;

        private ChargedLightWaveGoal(AbyssalLizardEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive() && this.mob.distanceToSqr(target) > 16.0D;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive() && !this.mob.isRoaring();
        }

        @Override
        public void stop() {
            this.chargeTicks = 0;
            this.mob.navigation.stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return;
            }

            double distance = this.mob.distanceToSqr(target);
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (distance > 100.0D) {
                this.mob.getNavigation().moveTo(target, 1.0D);
            } else {
                this.mob.getNavigation().stop();
            }

            if (this.cooldownTicks > 0) {
                this.cooldownTicks--;
                return;
            }

            if (!this.mob.hasLineOfSight(target)) {
                this.chargeTicks = 0;
                return;
            }

            this.chargeTicks++;
            if (this.chargeTicks >= 24) {
                Vec3 aim = target.getEyePosition().subtract(this.mob.getEyePosition()).normalize();
                AbyssalLightWaveEntity lightWave = new AbyssalLightWaveEntity(this.mob.level(), this.mob, aim.x * 0.22D, aim.y * 0.22D, aim.z * 0.22D);
                lightWave.setPos(this.mob.getX(), this.mob.getEyeY() - 0.15D, this.mob.getZ());
                this.mob.level().addFreshEntity(lightWave);
                this.chargeTicks = 0;
                this.cooldownTicks = 70;
            }
        }
    }

    private static final class RoarGoal extends Goal {
        private final AbyssalLizardEntity mob;

        private RoarGoal(AbyssalLizardEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.mob.canRoar();
        }

        @Override
        public boolean canContinueToUse() {
            return this.mob.isRoaring();
        }

        @Override
        public void start() {
            this.mob.startRoar();
        }

        @Override
        public void tick() {
            this.mob.getNavigation().stop();
            LivingEntity target = this.mob.getTarget();
            if (target != null) {
                this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }
    }
}
