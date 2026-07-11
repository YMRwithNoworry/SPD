package alku.spd.entity;

import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdItems;
import alku.spd.registry.SpdTags;
import alku.spd.world.AbyssalFoxInfectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class AbyssalFoxEntity extends Fox implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.abyssal_fox.idle");
    private static final RawAnimation MOVE = RawAnimation.begin().thenLoop("animation.abyssal_fox.move");
    private static final UUID NIGHT_SPEED_MODIFIER_ID = UUID.fromString("cd40827f-9516-4890-a638-3ba4d4a868a6");
    private static final AttributeModifier NIGHT_SPEED_MODIFIER = new AttributeModifier(
            NIGHT_SPEED_MODIFIER_ID,
            "SPD abyssal fox night speed",
            0.1D,
            AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final int TAIL_SWEEP_COOLDOWN = 20 * 8;
    private static final int PRESSURE_DURATION = 20 * 10;
    private static final int POUNCE_WINDUP = 12;
    private static final int POUNCE_MISS_STUN = 8;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int tailSweepCooldown;
    private int pounceCooldown;
    private int hardStunTicks;
    private int foodTheftCooldown;
    private int fleeTicks;
    @Nullable
    private Player fleeFromPlayer;
    private boolean alertingPack;

    public AbyssalFoxEntity(EntityType<? extends AbyssalFoxEntity> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
        this.setDropChance(EquipmentSlot.MAINHAND, 2.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Fox.createAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public static boolean checkSpawnRules(EntityType<AbyssalFoxEntity> type, ServerLevelAccessor level, MobSpawnType reason,
                                          BlockPos pos, RandomSource random) {
        if (!level.getBiome(pos).is(SpdTags.ABYSSAL_FOX_SPAWNS)) {
            return false;
        }
        if (level.getBiome(pos).is(SpdTags.ABYSSAL_BIOMES)) {
            boolean nearEdge = !level.getBiome(pos.offset(24, 0, 0)).is(SpdTags.ABYSSAL_BIOMES)
                    || !level.getBiome(pos.offset(-24, 0, 0)).is(SpdTags.ABYSSAL_BIOMES)
                    || !level.getBiome(pos.offset(0, 0, 24)).is(SpdTags.ABYSSAL_BIOMES)
                    || !level.getBiome(pos.offset(0, 0, -24)).is(SpdTags.ABYSSAL_BIOMES);
            if (!nearEdge) {
                return false;
            }
        }
        if (reason == MobSpawnType.NATURAL && level.getLevel().isDay() && random.nextFloat() >= 0.625F) {
            return false;
        }
        return level.getBlockState(pos.below()).is(BlockTags.FOXES_SPAWNABLE_ON);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new AvoidPurificationGoal(this));
        this.goalSelector.addGoal(1, new TailSweepGoal(this));
        this.goalSelector.addGoal(2, new PounceAttackGoal(this));
        this.goalSelector.addGoal(3, new FastMeleeAttackGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this).setAlertOthers(AbyssalFoxEntity.class));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 5, true, false,
                target -> target instanceof Player player && !player.isCreative() && !player.isSpectator()
                        && this.distanceToSqr(player) <= 25.0D));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        if (this.tailSweepCooldown > 0) {
            this.tailSweepCooldown--;
        }
        if (this.pounceCooldown > 0) {
            this.pounceCooldown--;
        }
        if (this.hardStunTicks > 0) {
            this.hardStunTicks--;
            this.navigation.stop();
        }
        if (this.foodTheftCooldown > 0) {
            this.foodTheftCooldown--;
        }

        this.updateNightBonuses();
        this.tickAbyssalRegeneration();
        this.tickFoodTheft();
        this.tickPreferredLooting();
        if (this.level().isNight() && this.tickCount % 10 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
                    this.getX(), this.getY() + 0.45D, this.getZ(), 1, 0.25D, 0.2D, 0.25D, 0.0D);
        }

        LivingEntity target = this.getTarget();
        if (target instanceof Player && this.level().isDay() && this.distanceToSqr(target) > 64.0D) {
            this.setTarget(null);
        }
    }

    private void updateNightBonuses() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        boolean night = this.level().isNight();
        boolean applied = speed.getModifier(NIGHT_SPEED_MODIFIER_ID) != null;
        if (night && !applied) {
            speed.addTransientModifier(NIGHT_SPEED_MODIFIER);
        } else if (!night && applied) {
            speed.removeModifier(NIGHT_SPEED_MODIFIER_ID);
        }
    }

    private void tickAbyssalRegeneration() {
        if (this.tickCount % 20 == 0 && this.level().getBiome(this.blockPosition()).is(SpdTags.ABYSSAL_BIOMES)) {
            this.heal(0.5F);
        }
    }

    private void tickFoodTheft() {
        if (this.fleeTicks > 0 && this.fleeFromPlayer != null && this.fleeFromPlayer.isAlive()) {
            this.fleeTicks--;
            Vec3 escape = DefaultRandomPos.getPosAway(this, 12, 7, this.fleeFromPlayer.position());
            if (escape != null) {
                this.navigation.moveTo(escape.x, escape.y, escape.z, 1.65D);
            }
            return;
        }
        this.fleeFromPlayer = null;
        if (this.foodTheftCooldown > 0 || !this.getMainHandItem().isEmpty()) {
            return;
        }

        Player eater = this.level().getNearestPlayer(this, 10.0D);
        if (eater == null || eater.isCreative() || !eater.isUsingItem() || !eater.getUseItem().isEdible()) {
            return;
        }

        this.navigation.moveTo(eater, 1.65D);
        if (this.distanceToSqr(eater) > 3.24D) {
            return;
        }

        ItemStack food = eater.getUseItem();
        ItemStack stolen = food.copyWithCount(1);
        food.shrink(1);
        this.setItemSlot(EquipmentSlot.MAINHAND, stolen);
        this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        this.fleeFromPlayer = eater;
        this.fleeTicks = 20 * 5;
        this.foodTheftCooldown = 20 * 20;
    }

    private void tickPreferredLooting() {
        if (this.tickCount % 10 != 0 || !this.getMainHandItem().isEmpty() || this.fleeTicks > 0) {
            return;
        }

        List<ItemEntity> preferred = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(8.0D),
                item -> item.isAlive() && !item.hasPickUpDelay() && this.isPreferredLoot(item.getItem()));
        preferred.stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .ifPresent(item -> {
                    this.navigation.moveTo(item, 1.4D);
                    if (this.distanceToSqr(item) <= 2.25D) {
                        this.pickUpItem(item);
                        this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                    }
                });
    }

    private boolean isPreferredLoot(ItemStack stack) {
        return stack.is(SpdTags.FORGE_ORES)
                || stack.is(SpdTags.COMMON_ORES)
                || stack.getItem().getFoodProperties() != null && stack.getItem().getFoodProperties().isMeat();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = switch (this.level().getDifficulty()) {
            case PEACEFUL -> 0.0F;
            case EASY -> 2.0F;
            case NORMAL -> 3.0F;
            case HARD -> 4.0F;
        };
        if (this.level().isNight()) {
            damage *= 1.2F;
        }

        boolean hurt = damage > 0.0F && target.hurt(this.damageSources().mobAttack(this), damage);
        if (hurt && target instanceof LivingEntity living) {
            this.applyAbyssalPressure(living, 1);
            this.tryCorrodeHeldItem(living);
            if (living instanceof Fox fox && !(fox instanceof AbyssalFoxEntity)) {
                AbyssalFoxInfectionEvents.markAccelerated(fox);
            }
        }
        return hurt;
    }

    private void tryCorrodeHeldItem(LivingEntity target) {
        if (this.random.nextFloat() >= 0.15F) {
            return;
        }
        ItemStack held = target.getMainHandItem();
        if (held.isDamageableItem()) {
            held.hurtAndBreak(1, target, living -> living.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
    }

    private void applyAbyssalPressure(LivingEntity target, int layers) {
        MobEffectInstance current = target.getEffect(SpdEffects.ABYSSAL_PRESSURE.get());
        int currentLayers = current == null ? 0 : current.getAmplifier() + 1;
        int newLayers = Mth.clamp(currentLayers + layers, 1, 5);
        target.addEffect(new MobEffectInstance(SpdEffects.ABYSSAL_PRESSURE.get(), PRESSURE_DURATION, newLayers - 1), this);
    }

    private void performTailSweep() {
        LivingEntity focus = this.getTarget();
        if (focus == null) {
            return;
        }
        Vec3 sweepDirection = focus.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
        AABB area = this.getBoundingBox().inflate(3.0D, 1.5D, 3.0D);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ASH,
                    this.getX(), this.getY() + 0.4D, this.getZ(), 28, 1.5D, 0.5D, 1.5D, 0.04D);
        }
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area, target ->
                target != this && target.isAlive() && !SpdEntityTargeting.isAbyssalEntity(target))) {
            Vec3 toTarget = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (toTarget.lengthSqr() > 9.0D || toTarget.lengthSqr() < 0.01D || sweepDirection.dot(toTarget.normalize()) < 0.5D) {
                continue;
            }
            this.applyAbyssalPressure(target, 2);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
        }

        Vec3 retreat = this.position().subtract(focus.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
        this.setDeltaMovement(retreat.x * 0.65D, 0.35D, retreat.z * 0.65D);
        this.tailSweepCooldown = TAIL_SWEEP_COOLDOWN;
    }

    private boolean performPounceHit(LivingEntity target) {
        if (!target.hurt(this.damageSources().mobAttack(this), 5.0F)) {
            return false;
        }
        target.knockback(1.0D, this.getX() - target.getX(), this.getZ() - target.getZ());
        this.applyAbyssalPressure(target, 1);
        if (target instanceof Fox fox && !(fox instanceof AbyssalFoxEntity)) {
            AbyssalFoxInfectionEvents.markAccelerated(fox);
        }
        return true;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previous = this.getTarget();
        super.setTarget(target);
        if (target == null || target == previous || this.level().isClientSide || this.alertingPack) {
            return;
        }

        this.alertingPack = true;
        for (AbyssalFoxEntity fox : this.level().getEntitiesOfClass(AbyssalFoxEntity.class,
                this.getBoundingBox().inflate(12.0D), fox -> fox != this && fox.isAlive())) {
            if (fox.getTarget() == null) {
                fox.alertingPack = true;
                fox.setTarget(target);
                fox.alertingPack = false;
            }
        }
        this.alertingPack = false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty() && !this.getMainHandItem().isEmpty()) {
            ItemStack stolen = this.getMainHandItem().copy();
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (!player.getInventory().add(stolen)) {
                player.drop(stolen, false);
            }
            this.setTarget(player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(SpdTags.PURIFICATION_DAMAGE)) {
            amount *= 2.0F;
        } else if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            amount *= 0.5F;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected boolean isImmobile() {
        return this.hardStunTicks > 0 || super.isImmobile();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        if (this.getMainHandItem().isEmpty() && this.random.nextFloat() < 0.3F) {
            this.setItemSlot(EquipmentSlot.MAINHAND, this.random.nextBoolean()
                    ? SpdItems.FUNGAL_RESIDUE.get().getDefaultInstance()
                    : SpdItems.CHROME_DUST.get().getDefaultInstance());
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        }
        return data;
    }

    @Nullable
    @Override
    public Fox getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return SpdEntities.ABYSSAL_FOX.get().create(level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpdTailSweepCooldown", this.tailSweepCooldown);
        tag.putInt("SpdPounceCooldown", this.pounceCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.tailSweepCooldown = tag.getInt("SpdTailSweepCooldown");
        this.pounceCooldown = tag.getInt("SpdPounceCooldown");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            state.setAndContinue(state.isMoving() ? MOVE : IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private static final class FastMeleeAttackGoal extends MeleeAttackGoal {
        private FastMeleeAttackGoal(AbyssalFoxEntity fox) {
            super(fox, 1.35D, true);
        }

        @Override
        protected int getAttackInterval() {
            return 16;
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return 2.25D;
        }
    }

    private static final class TailSweepGoal extends Goal {
        private final AbyssalFoxEntity fox;
        private int actionTicks;

        private TailSweepGoal(AbyssalFoxEntity fox) {
            this.fox = fox;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.fox.getTarget();
            return target != null && target.isAlive()
                    && this.fox.getHealth() < this.fox.getMaxHealth() * 0.5F
                    && this.fox.tailSweepCooldown <= 0
                    && this.fox.distanceToSqr(target) <= 16.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return this.actionTicks > 0;
        }

        @Override
        public void start() {
            this.actionTicks = 8;
            this.fox.navigation.stop();
            this.fox.performTailSweep();
        }

        @Override
        public void tick() {
            this.actionTicks--;
        }
    }

    private static final class PounceAttackGoal extends Goal {
        private final AbyssalFoxEntity fox;
        private int windupTicks;
        private int flightTicks;
        private boolean active;
        private boolean launched;
        private boolean hit;

        private PounceAttackGoal(AbyssalFoxEntity fox) {
            this.fox = fox;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.fox.getTarget();
            double distance = target == null ? 0.0D : this.fox.distanceToSqr(target);
            return target != null && target.isAlive() && this.fox.onGround()
                    && this.fox.pounceCooldown <= 0 && distance > 9.0D && distance < 144.0D
                    && this.fox.random.nextInt(10) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.active;
        }

        @Override
        public void start() {
            this.active = true;
            this.launched = false;
            this.hit = false;
            this.windupTicks = POUNCE_WINDUP;
            this.flightTicks = 0;
            this.fox.setIsCrouching(true);
            this.fox.navigation.stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.fox.getTarget();
            if (target == null || !target.isAlive()) {
                this.active = false;
                return;
            }

            this.fox.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (!this.launched) {
                this.fox.navigation.stop();
                if (--this.windupTicks <= 0) {
                    Vec3 direction = target.position().subtract(this.fox.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
                    this.fox.setDeltaMovement(direction.x * 0.9D, 0.9D, direction.z * 0.9D);
                    this.fox.setIsPouncing(true);
                    this.fox.setIsCrouching(false);
                    this.launched = true;
                }
                return;
            }

            this.flightTicks++;
            if (!this.hit && this.fox.distanceToSqr(target) <= 2.89D) {
                this.hit = this.fox.performPounceHit(target);
                this.active = false;
            } else if (this.flightTicks > 2 && this.fox.onGround() || this.flightTicks >= 40) {
                this.active = false;
            }
        }

        @Override
        public void stop() {
            this.fox.setIsPouncing(false);
            this.fox.setIsCrouching(false);
            this.fox.pounceCooldown = 60;
            if (this.launched && !this.hit) {
                this.fox.hardStunTicks = POUNCE_MISS_STUN;
            }
            this.active = false;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

    private static final class AvoidPurificationGoal extends Goal {
        private final AbyssalFoxEntity fox;
        @Nullable
        private BlockPos danger;
        private int escapeTicks;

        private AvoidPurificationGoal(AbyssalFoxEntity fox) {
            this.fox = fox;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.fox.tickCount % 10 != 0) {
                return false;
            }
            BlockPos center = this.fox.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-6, -3, -6), center.offset(6, 3, 6))) {
                if (this.fox.level().getBlockState(pos).is(SpdTags.PURIFICATION_BLOCKS)) {
                    this.danger = pos.immutable();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.danger != null && this.escapeTicks > 0;
        }

        @Override
        public void start() {
            this.escapeTicks = 40;
            this.fox.setTarget(null);
        }

        @Override
        public void tick() {
            this.escapeTicks--;
            if (this.danger == null) {
                return;
            }
            Vec3 away = DefaultRandomPos.getPosAway(this.fox, 12, 7, Vec3.atCenterOf(this.danger));
            if (away != null) {
                this.fox.navigation.moveTo(away.x, away.y, away.z, 1.5D);
            }
        }

        @Override
        public void stop() {
            this.danger = null;
        }
    }
}
