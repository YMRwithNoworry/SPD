package alku.spd.entity;

import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdTags;
import alku.spd.world.AbyssalWolfInfectionEvents;
import alku.spd.world.SpdCorrosion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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
import java.util.List;
import java.util.UUID;

public class AbyssalWolfEntity extends Wolf implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.abyssal_wolf.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.abyssal_wolf.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.abyssal_wolf.attack");
    private static final UUID RAGE_SPEED_ID = UUID.fromString("614ecb4b-e6ea-4b88-922f-9251febcc94a");
    private static final UUID RAGE_DAMAGE_ID = UUID.fromString("0b687c89-b620-44c9-9c08-83832a56d35f");
    private static final UUID PACK_DAMAGE_ID = UUID.fromString("d373ebfa-4455-491c-ad9d-f1b8ba2c421d");
    private static final AttributeModifier RAGE_SPEED = new AttributeModifier(RAGE_SPEED_ID,
            "SPD abyssal wolf rage speed", 0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier RAGE_DAMAGE = new AttributeModifier(RAGE_DAMAGE_ID,
            "SPD abyssal wolf rage damage", 0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int howlCooldown;
    private int pounceCooldown;
    private int hardStunTicks;
    private int pacifiedTicks;
    private int attackAnimationTicks;
    private boolean enraged;
    private boolean alertingPack;

    public AbyssalWolfEntity(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(true);
        this.xpReward = 2;
    }

    @Override
    public int getExperienceReward() {
        return 2 + this.random.nextInt(3);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    public static boolean checkSpawnRules(EntityType<AbyssalWolfEntity> type, ServerLevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBiome(pos).is(SpdTags.ABYSSAL_WOLF_SPAWNS)
                && level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), type)
                && level.getRawBrightness(pos, 0) <= 10;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new AvoidPurificationGoal(this));
        this.goalSelector.addGoal(1, new PounceGoal(this));
        this.goalSelector.addGoal(2, new AbyssalMeleeGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this).setAlertOthers(AbyssalWolfEntity.class));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 5, true, false,
                target -> this.pacifiedTicks <= 0 && target instanceof Player player
                        && !player.isCreative() && !player.isSpectator() && this.distanceToSqr(player) <= 64.0D));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.attackAnimationTicks > 0) this.attackAnimationTicks--;
        if (this.level().isClientSide) return;
        if (this.howlCooldown > 0) this.howlCooldown--;
        if (this.pounceCooldown > 0) this.pounceCooldown--;
        if (this.pacifiedTicks > 0) this.pacifiedTicks--;
        if (this.hardStunTicks > 0) {
            this.hardStunTicks--;
            this.navigation.stop();
        }

        this.updateRageAndPackBonuses();
        if (SpdCorrosion.isInAbyssalEnvironment(this) && this.tickCount % 20 == 0) this.heal(0.5F);
        if (this.getHealth() < this.getMaxHealth() * 0.6F && this.howlCooldown <= 0 && this.getTarget() != null) {
            this.howl(this.getTarget());
        }
    }

    private void updateRageAndPackBonuses() {
        if (!this.enraged && this.getHealth() < this.getMaxHealth() * 0.3F) this.enraged = true;
        updateModifier(Attributes.MOVEMENT_SPEED, RAGE_SPEED, this.enraged && this.getTarget() != null);
        updateModifier(Attributes.ATTACK_DAMAGE, RAGE_DAMAGE, this.enraged && this.getTarget() != null);

        int allies = Math.min(4, this.level().getEntitiesOfClass(AbyssalWolfEntity.class,
                this.getBoundingBox().inflate(5.0D), wolf -> wolf != this && wolf.isAlive()).size());
        AttributeModifier pack = new AttributeModifier(PACK_DAMAGE_ID, "SPD abyssal wolf pack damage",
                allies * 0.08D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(PACK_DAMAGE_ID);
            if (allies > 0) damage.addTransientModifier(pack);
        }
    }

    private void updateModifier(net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                AttributeModifier modifier, boolean active) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance == null) return;
        boolean present = instance.getModifier(modifier.getId()) != null;
        if (active && !present) instance.addTransientModifier(modifier);
        else if (!active && present) instance.removeModifier(modifier.getId());
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!(entity instanceof LivingEntity target)) return super.doHurtTarget(entity);
        float damage = switch (this.level().getDifficulty()) {
            case PEACEFUL -> 0.0F;
            case EASY -> 4.0F;
            case NORMAL -> 5.0F;
            case HARD -> 7.0F;
        };
        damage *= (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) / 5.0D);
        if (!target.hurt(this.damageSources().mobAttack(this), damage)) return false;
        this.swing(InteractionHand.MAIN_HAND);
        this.attackAnimationTicks = 14;
        SpdCorrosion.addAbyssalPressure(target, this.enraged ? 3 : 2, 20 * 12, this);
        target.addEffect(new MobEffectInstance(SpdEffects.RENDING.get(), 20 * 3, 0), this);
        Vec3 pull = this.position().subtract(target.position()).multiply(0.12D, 0.0D, 0.12D);
        target.push(pull.x, 0.05D, pull.z);
        if (target.isUsingItem()) target.stopUsingItem();
        if (target instanceof Wolf wolf && !(wolf instanceof AbyssalWolfEntity)) {
            AbyssalWolfInfectionEvents.markAccelerated(wolf);
        }
        return true;
    }

    private void howl(LivingEntity target) {
        this.howlCooldown = 20 * 15;
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0));
        this.playSound(SoundEvents.WOLF_HOWL, 1.2F, 0.65F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ASH, this.getX(), this.getY() + 1.0D, this.getZ(),
                    20, 0.6D, 0.5D, 0.6D, 0.02D);
        }
        List<AbyssalWolfEntity> wolves = this.level().getEntitiesOfClass(AbyssalWolfEntity.class,
                this.getBoundingBox().inflate(16.0D), wolf -> wolf != this && wolf.isAlive());
        wolves.stream().limit(5).forEach(wolf -> wolf.setTarget(target));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(SpdTags.PURIFICATION_DAMAGE)) amount *= 2.0F;
        else if (source.is(DamageTypeTags.IS_FIRE)) amount *= 0.6F;
        if (this.enraged) amount *= 1.15F;
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.howlCooldown <= 0 && source.getEntity() instanceof LivingEntity target) {
            this.howl(target);
        }
        return hurt;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEdible() && stack.getItem().getFoodProperties() != null
                && stack.getItem().getFoodProperties().isMeat()) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            this.pacifiedTicks = 20 * 30;
            this.setTarget(null);
            this.heal(2.0F);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public Wolf getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    protected boolean isImmobile() {
        return this.hardStunTicks > 0 || super.isImmobile();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("SpdAbyssalWolfEnraged", this.enraged);
        tag.putInt("SpdAbyssalWolfHowlCooldown", this.howlCooldown);
        tag.putInt("SpdAbyssalWolfPounceCooldown", this.pounceCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.enraged = tag.getBoolean("SpdAbyssalWolfEnraged");
        this.howlCooldown = tag.getInt("SpdAbyssalWolfHowlCooldown");
        this.pounceCooldown = tag.getInt("SpdAbyssalWolfPounceCooldown");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, state -> {
            if (this.attackAnimationTicks > 0 || this.swinging) state.setAndContinue(ATTACK);
            else {
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

    private static final class AbyssalMeleeGoal extends MeleeAttackGoal {
        private final AbyssalWolfEntity wolf;
        private AbyssalMeleeGoal(AbyssalWolfEntity wolf) {
            super(wolf, 1.25D, true);
            this.wolf = wolf;
        }
        @Override protected int getAttackInterval() { return this.wolf.enraged ? 14 : 20; }
        @Override protected double getAttackReachSqr(LivingEntity target) { return 3.24D; }
    }

    private static final class PounceGoal extends Goal {
        private final AbyssalWolfEntity wolf;
        private int ticks;
        private boolean hit;
        private PounceGoal(AbyssalWolfEntity wolf) {
            this.wolf = wolf;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
        }
        @Override public boolean canUse() {
            LivingEntity target = this.wolf.getTarget();
            return target != null && target.isAlive() && this.wolf.onGround() && this.wolf.pounceCooldown <= 0
                    && this.wolf.distanceToSqr(target) > 4.0D && this.wolf.distanceToSqr(target) < 64.0D
                    && this.wolf.random.nextInt(8) == 0;
        }
        @Override public void start() {
            LivingEntity target = this.wolf.getTarget();
            if (target == null) return;
            Vec3 direction = target.position().subtract(this.wolf.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
            this.wolf.setDeltaMovement(direction.x * 0.8D, 0.55D, direction.z * 0.8D);
            this.ticks = 20;
            this.hit = false;
        }
        @Override public boolean canContinueToUse() { return this.ticks > 0 && !this.hit; }
        @Override public void tick() {
            this.ticks--;
            LivingEntity target = this.wolf.getTarget();
            if (target != null && this.wolf.distanceToSqr(target) <= 3.24D) {
                float base = (float) this.wolf.getAttributeValue(Attributes.ATTACK_DAMAGE);
                if (target.hurt(this.wolf.damageSources().mobAttack(this.wolf), base * 1.3F)) {
                    this.wolf.swing(InteractionHand.MAIN_HAND);
                    SpdCorrosion.addAbyssalPressure(target, 1, 20 * 12, this.wolf);
                    target.knockback(1.0D, this.wolf.getX() - target.getX(), this.wolf.getZ() - target.getZ());
                    this.wolf.attackAnimationTicks = 14;
                }
                this.hit = true;
            }
        }
        @Override public void stop() {
            this.wolf.pounceCooldown = 60;
            if (!this.hit) this.wolf.hardStunTicks = 6;
        }
    }

    private static final class AvoidPurificationGoal extends Goal {
        private final AbyssalWolfEntity wolf;
        private BlockPos danger;
        private AvoidPurificationGoal(AbyssalWolfEntity wolf) { this.wolf = wolf; this.setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            if (this.wolf.tickCount % 10 != 0) return false;
            BlockPos center = this.wolf.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-6, -3, -6), center.offset(6, 3, 6))) {
                if (this.wolf.level().getBlockState(pos).is(SpdTags.PURIFICATION_BLOCKS)) {
                    this.danger = pos.immutable();
                    return true;
                }
            }
            return false;
        }
        @Override public boolean canContinueToUse() { return this.danger != null && this.wolf.distanceToSqr(Vec3.atCenterOf(this.danger)) < 100.0D; }
        @Override public void tick() {
            Vec3 away = this.wolf.position().subtract(Vec3.atCenterOf(this.danger)).normalize();
            this.wolf.navigation.moveTo(this.wolf.getX() + away.x * 10.0D, this.wolf.getY(), this.wolf.getZ() + away.z * 10.0D, 1.4D);
        }
        @Override public void stop() { this.danger = null; }
    }
}
