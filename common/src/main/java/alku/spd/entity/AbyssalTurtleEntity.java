package alku.spd.entity;

import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdBlocks;
import alku.spd.registry.SpdTags;
import alku.spd.world.SpdCorrosion;
import alku.spd.mixin.TurtleAccessorMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.EnumSet;
import java.util.UUID;

public class AbyssalTurtleEntity extends Turtle implements GeoEntity {
    private static final double LAND_SPEED = 0.15D;
    private static final double WATER_SPEED = 0.32D;
    private static final int FOG_COOLDOWN_TICKS = 100;
    private static final int FOG_DURATION_TICKS = 40;
    private static final int SHELL_DURATION_TICKS = 120;
    private static final int SHELL_COOLDOWN_TICKS = 400;
    private static final int CHARGE_COOLDOWN_TICKS = 160;
    private static final int CHARGE_RETREAT_TICKS = 10;
    private static final UUID ARMOR_PIERCE_MODIFIER_ID = UUID.fromString("73494fc3-f9eb-4fe3-8d8e-10f35ca8f51e");
    private static final EntityDataAccessor<Boolean> SHELL_GUARD = SynchedEntityData.defineId(
            AbyssalTurtleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(
            AbyssalTurtleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int fogTicks;
    private int fogCooldown;
    private int shellTicks;
    private int shellCooldown;
    private int chargeCooldown;
    private int chargeRetreatTicks;
    private boolean chargeResolved;
    private int attackAnimationTicks;

    public AbyssalTurtleEntity(EntityType<? extends AbyssalTurtleEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Turtle.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, LAND_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static boolean checkSpawnRules(EntityType<AbyssalTurtleEntity> type, ServerLevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBiome(pos).is(SpdTags.ABYSSAL_TURTLE_SPAWNS)
                && (level.getFluidState(pos).is(FluidTags.WATER) || TurtleEggBlock.onSand(level, pos));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHELL_GUARD, false);
        this.entityData.define(CHARGING, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeAllGoals(goal -> goal.getClass().getName().contains("TurtleLayEggGoal"));
        this.goalSelector.addGoal(1, new LayAbyssalEggGoal(this));
        this.goalSelector.addGoal(2, new AdaptiveMeleeAttackGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
    }

    @Override
    public void aiStep() {
        this.updateEnvironmentAttributes();
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.attackAnimationTicks > 0) this.attackAnimationTicks--;
        if (this.level().isClientSide) return;

        if (this.fogCooldown > 0) this.fogCooldown--;
        if (this.shellCooldown > 0) this.shellCooldown--;
        if (this.chargeCooldown > 0) this.chargeCooldown--;
        this.tickSporeFog();
        this.tickShellGuard();
        this.tickWaterCharge();
    }

    private void updateEnvironmentAttributes() {
        boolean inWater = this.isInWater();
        AttributeInstance movement = this.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance armor = this.getAttribute(Attributes.ARMOR);
        if (movement != null) {
            movement.setBaseValue(inWater ? WATER_SPEED : LAND_SPEED);
        }
        if (armor != null) {
            armor.setBaseValue(AbyssalTurtleMechanics.armor(inWater));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean purification = source.is(SpdTags.PURIFICATION_DAMAGE);
        if (purification && this.isShellGuarding()) {
            this.stopShellGuard();
        }
        if (source.is(SpdTags.PURIFICATION_DAMAGE)) {
            amount *= AbyssalTurtleMechanics.purificationDamageMultiplier();
        } else if (source.is(DamageTypeTags.IS_FIRE)) {
            amount *= AbyssalTurtleMechanics.fireDamageMultiplier();
        }
        if (this.isShellGuarding()) {
            amount *= AbyssalTurtleMechanics.shellDamageMultiplier();
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.startSporeFog();
            if (source.getEntity() instanceof LivingEntity attacker) {
                this.applySporePulse(attacker);
                this.setTarget(attacker);
            }
            if (!purification && this.getHealth() <= this.getMaxHealth() * 0.4F) {
                this.startShellGuard();
            }
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!(entity instanceof LivingEntity target) || this.isShellGuarding()) return false;
        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        AttributeModifier pierce = new AttributeModifier(ARMOR_PIERCE_MODIFIER_ID,
                "SPD abyssal turtle calcium erosion", -2.0D, AttributeModifier.Operation.ADDITION);
        boolean addedPierce = this.random.nextFloat() < 0.2F && armor != null
                && armor.getModifier(ARMOR_PIERCE_MODIFIER_ID) == null;
        if (addedPierce) armor.addTransientModifier(pierce);
        try {
            float damage = AbyssalTurtleMechanics.biteDamage(this.level().getDifficulty());
            if (!target.hurt(this.damageSources().mobAttack(this), damage)) return false;
        } finally {
            if (addedPierce && armor != null) armor.removeModifier(ARMOR_PIERCE_MODIFIER_ID);
        }
        this.attackAnimationTicks = 12;
        SpdCorrosion.addAbyssalPressure(target, 2, 20 * 12, this);
        this.startSporeFog();
        return true;
    }

    private void tickSporeFog() {
        if (this.fogTicks <= 0) return;
        this.fogTicks--;
        if (this.fogTicks % 20 != 0) return;
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(2.0D), SpdEntityTargeting::isNonSpdLiving);
        for (LivingEntity target : targets) this.applySporePulse(target);
    }

    private void startSporeFog() {
        if (this.fogCooldown > 0) return;
        this.fogCooldown = FOG_COOLDOWN_TICKS;
        this.fogTicks = FOG_DURATION_TICKS;
    }

    private void applySporePulse(LivingEntity target) {
        if (!SpdEntityTargeting.isNonSpdLiving(target)) return;
        SpdCorrosion.addAbyssalPressure(target, 1, 20 * 10, this);
        target.addEffect(new MobEffectInstance(SpdEffects.SPORE_SLUGGISHNESS.get(), 40, 0), this);
    }

    private void tickShellGuard() {
        if (!this.isShellGuarding()) return;
        this.navigation.stop();
        if (this.shellTicks > 0) this.shellTicks--;
        if (this.shellTicks > 0 && this.shellTicks % 20 == 0) {
            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(2.0D), SpdEntityTargeting::isNonSpdLiving)) {
                this.applySporePulse(target);
            }
        }
        if (this.shellTicks <= 0) this.stopShellGuard();
    }

    private void startShellGuard() {
        if (this.shellCooldown > 0 || this.isShellGuarding()) return;
        this.shellCooldown = SHELL_COOLDOWN_TICKS;
        this.shellTicks = SHELL_DURATION_TICKS;
        this.entityData.set(SHELL_GUARD, true);
        this.navigation.stop();
    }

    private void stopShellGuard() {
        this.shellTicks = 0;
        this.entityData.set(SHELL_GUARD, false);
    }

    private void tickWaterCharge() {
        LivingEntity target = this.getTarget();
        if (!this.isCharging() && target != null && target.isAlive() && this.isInWater()
                && this.chargeCooldown <= 0 && !this.isShellGuarding()
                && this.distanceToSqr(target) > 16.0D && this.random.nextInt(20) == 0) {
            this.entityData.set(CHARGING, true);
            this.chargeCooldown = CHARGE_COOLDOWN_TICKS;
            this.chargeResolved = false;
            this.chargeRetreatTicks = 0;
        }
        if (!this.isCharging()) return;
        if (target == null || !target.isAlive() || !this.isInWater()) {
            this.stopCharge();
            return;
        }
        if (this.chargeRetreatTicks > 0) {
            Vec3 retreat = this.position().subtract(target.position()).normalize();
            this.getMoveControl().setWantedPosition(this.getX() + retreat.x * 3.0D,
                    this.getY(), this.getZ() + retreat.z * 3.0D, 1.4D);
            this.chargeRetreatTicks--;
            if (this.chargeRetreatTicks <= 0) this.stopCharge();
            return;
        }
        this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.9D);
        if (!this.chargeResolved && this.distanceToSqr(target) <= 2.56D) {
            this.chargeResolved = true;
            if (target.hurt(this.damageSources().mobAttack(this), 6.0F)) {
                SpdCorrosion.addAbyssalPressure(target, 2, 20 * 12, this);
                target.knockback(2.0F, this.getX() - target.getX(), this.getZ() - target.getZ());
            }
            this.chargeRetreatTicks = CHARGE_RETREAT_TICKS;
        }
    }

    private void stopCharge() {
        this.chargeRetreatTicks = 0;
        this.chargeResolved = false;
        this.entityData.set(CHARGING, false);
    }

    public boolean isShellGuarding() {
        return this.entityData.get(SHELL_GUARD);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    public int getAttackAnimationTicks() {
        return this.attackAnimationTicks;
    }

    @Override
    public int getExperienceReward() {
        return 3 + this.random.nextInt(3);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return SpdEntities.ABYSSAL_TURTLE.get().create(level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "base", 5, state -> {
            if (this.attackAnimationTicks > 0) return state.setAndContinue(ATTACK_ANIMATION);
            if (this.isShellGuarding()) return state.setAndContinue(IDLE_ANIMATION);
            if (this.isCharging() || state.isMoving()) return state.setAndContinue(WALK_ANIMATION);
            return state.setAndContinue(IDLE_ANIMATION);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected boolean isImmobile() {
        return this.isShellGuarding() || super.isImmobile();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SporeFogTicks", this.fogTicks);
        tag.putInt("SporeFogCooldown", this.fogCooldown);
        tag.putInt("ShellGuardTicks", this.shellTicks);
        tag.putInt("ShellGuardCooldown", this.shellCooldown);
        tag.putInt("ChargeCooldown", this.chargeCooldown);
        tag.putBoolean("ShellGuard", this.isShellGuarding());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.fogTicks = tag.getInt("SporeFogTicks");
        this.fogCooldown = tag.getInt("SporeFogCooldown");
        this.shellTicks = tag.getInt("ShellGuardTicks");
        this.shellCooldown = tag.getInt("ShellGuardCooldown");
        this.chargeCooldown = tag.getInt("ChargeCooldown");
        this.entityData.set(SHELL_GUARD, tag.getBoolean("ShellGuard") && this.shellTicks > 0);
    }

    private static final class AdaptiveMeleeAttackGoal extends MeleeAttackGoal {
        private final AbyssalTurtleEntity turtle;

        private AdaptiveMeleeAttackGoal(AbyssalTurtleEntity turtle) {
            super(turtle, 1.0D, true);
            this.turtle = turtle;
        }

        @Override
        protected int getAttackInterval() {
            return AbyssalTurtleMechanics.attackInterval(this.turtle.isInWater());
        }

        @Override
        public boolean canUse() {
            return !this.turtle.isShellGuarding() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.turtle.isShellGuarding() && super.canContinueToUse();
        }
    }

    private static final class LayAbyssalEggGoal extends Goal {
        private final AbyssalTurtleEntity turtle;
        @Nullable
        private BlockPos nest;

        private LayAbyssalEggGoal(AbyssalTurtleEntity turtle) {
            this.turtle = turtle;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.turtle.hasEgg() || this.turtle.isShellGuarding()) return false;
            BlockPos home = ((TurtleAccessorMixin) (Object) this.turtle).spd$getHomePos();
            this.nest = findNest(home);
            return this.nest != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.nest != null && this.turtle.hasEgg() && !this.turtle.isShellGuarding();
        }

        @Override
        public void start() {
            if (this.nest != null) {
                this.turtle.getNavigation().moveTo(this.nest.getX() + 0.5D, this.nest.getY(), this.nest.getZ() + 0.5D, 1.0D);
            }
        }

        @Override
        public void tick() {
            if (this.nest == null || this.turtle.distanceToSqr(this.nest.getX() + 0.5D, this.nest.getY(), this.nest.getZ() + 0.5D) > 3.0D) return;
            BlockState eggs = SpdBlocks.ABYSSAL_TURTLE_EGG.get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.TurtleEggBlock.EGGS, 1 + this.turtle.getRandom().nextInt(2));
            this.turtle.level().setBlock(this.nest, eggs, 3);
            ((TurtleAccessorMixin) (Object) this.turtle).spd$setHasEgg(false);
            this.nest = null;
        }

        @Nullable
        private BlockPos findNest(BlockPos home) {
            Level level = this.turtle.level();
            for (BlockPos candidate : BlockPos.betweenClosed(home.offset(-8, -2, -8), home.offset(8, 2, 8))) {
                if (level.isEmptyBlock(candidate) && TurtleEggBlock.onSand(level, candidate)) return candidate.immutable();
            }
            return null;
        }
    }
}
