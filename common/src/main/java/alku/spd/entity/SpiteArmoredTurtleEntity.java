package alku.spd.entity;

import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdBlocks;
import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdItems;
import alku.spd.registry.SpdTags;
import alku.spd.world.SpdCorrosion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class SpiteArmoredTurtleEntity extends Turtle implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final int SHELL_DEFENSIVE_TICKS = 40;
    private static final int TORPEDO_COOLDOWN_TICKS = 60;
    private static final int POLLUTION_INTERVAL_TICKS = 4;
    private static final int NODULE_MIN_COUNT = 2;
    private static final int NODULE_MAX_COUNT = 4;

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> SHELL_DEFENSIVE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SpiteArmoredTurtleEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private BlockPos spiteHomePos = BlockPos.ZERO;
    private Vec3 lastPollutionPosition;
    private int shellTicks;
    private int torpedoCooldown;
    private int pollutionTicks;
    private int shellRetaliationCooldown;
    private boolean noduleLaid;

    public SpiteArmoredTurtleEntity(EntityType<? extends SpiteArmoredTurtleEntity> entityType, Level level) {
        super(entityType, level);
        this.lastPollutionPosition = this.position();
        this.xpReward = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Turtle.createAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static boolean checkSpawnRules(EntityType<SpiteArmoredTurtleEntity> type, ServerLevelAccessor level,
                                           MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (reason == MobSpawnType.SPAWN_EGG) {
            return true;
        }
        if (!level.getBiome(pos).is(BiomeTags.IS_OCEAN) && !level.getBiome(pos).is(BiomeTags.IS_BEACH)) {
            return false;
        }
        if (!level.getFluidState(pos).is(FluidTags.WATER)
                && !level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)
                && !isSandSurface(level.getBlockState(pos.below()))) {
            return false;
        }
        return level.getRawBrightness(pos, 0) <= 10 || reason != MobSpawnType.NATURAL;
    }

    private static boolean isSandSurface(BlockState state) {
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)
                || state.is(SpdBlocks.ABYSSAL_BLOOD_SAND.get());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHELL_DEFENSIVE, false);
    }

    @Override
    public void setHomePos(BlockPos pos) {
        super.setHomePos(pos);
        this.spiteHomePos = pos.immutable();
    }

    public boolean isShellDefensive() {
        return this.entityData.get(SHELL_DEFENSIVE);
    }

    private void setShellDefensive(boolean value) {
        this.entityData.set(SHELL_DEFENSIVE, value);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeAllGoals(goal -> goal.getClass().getName().endsWith("Turtle$TurtleLayEggGoal"));
        this.goalSelector.addGoal(0, new AvoidPurificationGoal(this));
        this.goalSelector.addGoal(0, new TorpedoAttackGoal(this));
        this.goalSelector.addGoal(1, new ShellDefenseGoal(this));
        this.goalSelector.addGoal(2, new SpiteMeleeAttackGoal(this));
        this.goalSelector.addGoal(1, new SpiteNoduleGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 24, true, false,
                SpdEntityTargeting::isNonSpdLiving));
    }

    @Override
    public void tick() {
        Vec3 previousPosition = this.lastPollutionPosition;
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        if (this.shellTicks > 0 && --this.shellTicks == 0) {
            this.setShellDefensive(false);
        }
        if (this.torpedoCooldown > 0) {
            this.torpedoCooldown--;
        }
        if (this.pollutionTicks > 0) {
            this.pollutionTicks--;
        }
        if (this.shellRetaliationCooldown > 0) {
            this.shellRetaliationCooldown--;
        }
        if (this.isShellDefensive()) {
            this.navigation.stop();
        }
        if (SpdCorrosion.isInAbyssalEnvironment(this) && this.tickCount % 20 == 0) {
            this.heal(0.5F);
        }
        this.tickPollutionTrail(previousPosition);
        this.lastPollutionPosition = this.position();
    }

    private void tickPollutionTrail(Vec3 previousPosition) {
        Vec3 current = this.position();
        boolean movedHorizontally = current.x != previousPosition.x || current.z != previousPosition.z;
        if (!this.isInWater() || !movedHorizontally || this.tickCount % POLLUTION_INTERVAL_TICKS != 0) {
            return;
        }

        AABB pollutedArea = new AABB(previousPosition, previousPosition).inflate(1.5D, 0.8D, 1.5D);
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, pollutedArea,
                target -> target != this && target.isAlive() && SpdEntityTargeting.isNonSpdLiving(target))) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.POISON, 40, 0), this);
            if (this.pollutionTicks <= 0) {
                target.hurt(this.damageSources().magic(), 1.0F);
            }
        }
        this.pollutionTicks = POLLUTION_INTERVAL_TICKS;
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
                    previousPosition.x, previousPosition.y + 0.25D, previousPosition.z,
                    4, 0.35D, 0.15D, 0.35D, 0.01D);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.isShellDefensive() || !(target instanceof LivingEntity livingTarget)) {
            return false;
        }
        boolean hurt = livingTarget.hurt(this.damageSources().mobAttack(this),
                (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (!hurt) {
            return false;
        }
        this.swing(InteractionHand.MAIN_HAND);
        livingTarget.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                SpdEffects.RENDING.get(), 60, 0), this);
        return true;
    }

    private boolean hitTorpedoTarget(LivingEntity target) {
        boolean hurt = target.hurt(this.damageSources().mobAttack(this),
                (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (!hurt) {
            return false;
        }
        this.swing(InteractionHand.MAIN_HAND);
        target.knockback(1.0D, this.getX() - target.getX(), this.getZ() - target.getZ());
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                SpdEffects.RENDING.get(), 60, 0), this);
        return true;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source.is(SpdTags.PURIFICATION_DAMAGE)) {
            amount *= 2.0F;
        } else if (source.is(DamageTypeTags.IS_FIRE)) {
            amount *= 0.5F;
        }

        boolean directLivingAttacker = source.getEntity() instanceof LivingEntity;
        if (!this.level().isClientSide && !this.isInWater() && directLivingAttacker) {
            this.setShellDefensive(true);
            this.shellTicks = SHELL_DEFENSIVE_TICKS;
            amount *= 0.3F;
        } else if (this.isShellDefensive()) {
            amount *= 0.3F;
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && !this.isInWater()
                && directLivingAttacker && this.shellRetaliationCooldown <= 0) {
            this.retaliateWithShell();
        }
        return hurt;
    }

    private void retaliateWithShell() {
        this.shellRetaliationCooldown = 20;
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(4.0D),
                target -> target != this && target.isAlive() && SpdEntityTargeting.isNonSpdLiving(target))) {
            if (target.hurt(this.damageSources().mobAttack(this), 2.0F)) {
                target.knockback(1.0D, target.getX() - this.getX(), target.getZ() - this.getZ());
            }
        }
    }

    @Override
    protected boolean isImmobile() {
        return this.isShellDefensive() || super.isImmobile();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return SpdEntities.SPITE_ARMORED_TURTLE.get().create(level);
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(new ItemStack(SpdItems.HEAVY_SPITE_SCUTE.get(), 1 + (this.random.nextBoolean() ? 1 : 0)));
        if (this.random.nextFloat() < 0.5F) {
            this.spawnAtLocation(new ItemStack(SpdItems.RESIDUAL_MALICE.get(), 1 + (this.random.nextBoolean() ? 1 : 0)));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpiteShellTicks", this.shellTicks);
        tag.putInt("SpiteTorpedoCooldown", this.torpedoCooldown);
        tag.putInt("SpitePollutionTicks", this.pollutionTicks);
        tag.putInt("SpiteShellRetaliationCooldown", this.shellRetaliationCooldown);
        tag.putBoolean("SpiteNoduleLaid", this.noduleLaid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.shellTicks = tag.getInt("SpiteShellTicks");
        this.torpedoCooldown = tag.getInt("SpiteTorpedoCooldown");
        this.pollutionTicks = tag.getInt("SpitePollutionTicks");
        this.shellRetaliationCooldown = tag.getInt("SpiteShellRetaliationCooldown");
        this.noduleLaid = tag.getBoolean("SpiteNoduleLaid");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, state -> {
            if (this.swinging) {
                state.setAndContinue(ATTACK);
            } else {
                Vec3 motion = this.getDeltaMovement();
                boolean moving = state.isMoving() || motion.x * motion.x + motion.z * motion.z > 1.0E-5D;
                state.setAndContinue(moving ? WALK : IDLE);
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private static final class SpiteMeleeAttackGoal extends MeleeAttackGoal {
        private final SpiteArmoredTurtleEntity turtle;

        private SpiteMeleeAttackGoal(SpiteArmoredTurtleEntity turtle) {
            super(turtle, 1.0D, true);
            this.turtle = turtle;
        }

        @Override
        protected int getAttackInterval() {
            return 20;
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return 2.25D;
        }
    }

    private static final class TorpedoAttackGoal extends Goal {
        private final SpiteArmoredTurtleEntity turtle;
        private int windupTicks;
        private int flightTicks;
        private boolean launched;
        private boolean hit;

        private TorpedoAttackGoal(SpiteArmoredTurtleEntity turtle) {
            this.turtle = turtle;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.turtle.getTarget();
            return target != null && target.isAlive() && SpdEntityTargeting.isNonSpdLiving(target)
                    && this.turtle.isInWater() && this.turtle.torpedoCooldown <= 0
                    && this.turtle.distanceToSqr(target) > 9.0D
                    && this.turtle.distanceToSqr(target) <= 144.0D
                    && this.turtle.random.nextInt(8) == 0;
        }

        @Override
        public void start() {
            this.windupTicks = 12;
            this.flightTicks = 20;
            this.launched = false;
            this.hit = false;
            this.turtle.navigation.stop();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.hit && this.flightTicks > 0 && this.turtle.getTarget() != null
                    && this.turtle.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.turtle.getTarget();
            if (target == null) {
                return;
            }
            this.turtle.lookAt(target, 30.0F, 30.0F);
            this.turtle.navigation.stop();
            if (this.windupTicks > 0) {
                this.windupTicks--;
                return;
            }
            if (!this.launched) {
                Vec3 direction = target.position().subtract(this.turtle.position())
                        .multiply(1.0D, 0.0D, 1.0D).normalize();
                this.turtle.setDeltaMovement(direction.x * 0.9D, 0.12D, direction.z * 0.9D);
                this.turtle.hasImpulse = true;
                this.launched = true;
            }
            this.flightTicks--;
            if (this.turtle.distanceToSqr(target) <= 2.25D) {
                this.hit = this.turtle.hitTorpedoTarget(target);
            }
        }

        @Override
        public void stop() {
            this.turtle.torpedoCooldown = TORPEDO_COOLDOWN_TICKS;
            this.turtle.navigation.stop();
        }
    }

    private static final class ShellDefenseGoal extends Goal {
        private final SpiteArmoredTurtleEntity turtle;

        private ShellDefenseGoal(SpiteArmoredTurtleEntity turtle) {
            this.turtle = turtle;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.turtle.isShellDefensive();
        }

        @Override
        public boolean canContinueToUse() {
            return this.turtle.isShellDefensive();
        }

        @Override
        public void tick() {
            this.turtle.navigation.stop();
        }
    }

    private static final class AvoidPurificationGoal extends Goal {
        private final SpiteArmoredTurtleEntity turtle;
        private BlockPos danger;

        private AvoidPurificationGoal(SpiteArmoredTurtleEntity turtle) {
            this.turtle = turtle;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.turtle.tickCount % 10 != 0) {
                return false;
            }
            BlockPos center = this.turtle.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-6, -3, -6), center.offset(6, 3, 6))) {
                if (this.turtle.level().getBlockState(pos).is(SpdTags.PURIFICATION_BLOCKS)) {
                    this.danger = pos.immutable();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.danger != null && this.turtle.distanceToSqr(Vec3.atCenterOf(this.danger)) < 100.0D;
        }

        @Override
        public void tick() {
            Vec3 away = this.turtle.position().subtract(Vec3.atCenterOf(this.danger)).normalize();
            this.turtle.navigation.moveTo(this.turtle.getX() + away.x * 10.0D, this.turtle.getY(),
                    this.turtle.getZ() + away.z * 10.0D, 1.2D);
        }

        @Override
        public void stop() {
            this.danger = null;
        }
    }

    private static final class SpiteNoduleGoal extends Goal {
        private final SpiteArmoredTurtleEntity turtle;
        private int ticks;

        private SpiteNoduleGoal(SpiteArmoredTurtleEntity turtle) {
            this.turtle = turtle;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !this.turtle.noduleLaid && this.turtle.hasEgg() && !this.turtle.isInWater()
                    && !this.turtle.spiteHomePos.equals(BlockPos.ZERO)
                    && this.turtle.spiteHomePos.closerToCenterThan(this.turtle.position(), 9.0D);
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks > 0 && !this.turtle.isInWater();
        }

        @Override
        public void start() {
            this.ticks = 20;
            this.turtle.navigation.stop();
        }

        @Override
        public void tick() {
            this.turtle.navigation.stop();
            if (--this.ticks <= 0) {
                this.placeNodules();
            }
        }

        private void placeNodules() {
            if (this.turtle.noduleLaid || !(this.turtle.level() instanceof ServerLevel level)) {
                return;
            }
            int count = NODULE_MIN_COUNT + this.turtle.random.nextInt(NODULE_MAX_COUNT - NODULE_MIN_COUNT + 1);
            int placed = 0;
            BlockPos center = this.turtle.blockPosition();
            for (int attempt = 0; attempt < 16 && placed < count; attempt++) {
                BlockPos surface = center.offset(this.turtle.random.nextInt(5) - 2, 0,
                        this.turtle.random.nextInt(5) - 2);
                BlockPos nodulePos = surface.above();
                if (!isSandSurface(level.getBlockState(surface)) || !level.isEmptyBlock(nodulePos)) {
                    continue;
                }
                level.setBlock(nodulePos, SpdBlocks.SPITE_NODULE.get().defaultBlockState(), 3);
                placed++;
            }
            if (placed > 0) {
                this.turtle.noduleLaid = true;
                this.turtle.setInLoveTime(600);
            }
        }
    }
}
