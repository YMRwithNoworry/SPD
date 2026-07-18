package alku.spd.entity;

import alku.spd.registry.SpdBiomes;
import alku.spd.registry.SpdItems;
import alku.spd.registry.SpdTags;
import alku.spd.world.ChromeDragonContamination;
import alku.spd.world.SpdCorrosion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.GlassBlock;
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Elite chrome-infected dragon. Its combat effects deliberately use the existing
 * abyssal-pressure system so that normal pressure conversion and resistance rules
 * remain consistent with the rest of the mod.
 */
public final class GriefErodedChromeDragonEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("qi xi");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("xing zou");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fei xing");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("gong ji");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("si wang");

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> FLYING =
            net.minecraft.network.syncher.SynchedEntityData.defineId(GriefErodedChromeDragonEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> REGENERATING =
            net.minecraft.network.syncher.SynchedEntityData.defineId(GriefErodedChromeDragonEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    private static final UUID ARMOR_PIERCE_ID = UUID.fromString("2b513866-8fa9-49bb-9f19-6f9ce38a4957");
    private static final UUID DOMAIN_ATTACK_ID = UUID.fromString("4c3e7238-c54d-4e45-8f67-cbdc6d0ae341");
    private static final UUID DOMAIN_ARMOR_ID = UUID.fromString("673e02f1-3f09-4fcb-a3f5-1e0b10ad5f1a");
    private static final AttributeModifier DOMAIN_ATTACK = new AttributeModifier(
            DOMAIN_ATTACK_ID, "SPD chrome dragon grief domain damage", 0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier DOMAIN_ARMOR = new AttributeModifier(
            DOMAIN_ARMOR_ID, "SPD chrome dragon grief domain armor", 0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private static final int MELEE_INTERVAL = 24;
    private static final int BREATH_CHARGE_TICKS = 16;
    private static final int BREATH_COOLDOWN = 20 * 10;
    private static final int ROAR_COOLDOWN = 20 * 18;
    private static final int DIVE_COOLDOWN = 20 * 12;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<CorrosionZone> corrosionZones = new java.util.ArrayList<>();
    private final Set<UUID> domainTargets = new HashSet<>();
    private int attackCooldown;
    private int breathCooldown;
    private int breathChargeTicks;
    private int roarCooldown;
    private int diveCooldown;
    private int flightTicks;
    private int regenerationTicks;
    private boolean regenerationUsed;
    private int hardStunTicks;

    public GriefErodedChromeDragonEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.xpReward = 65;
        this.setMaxUpStep(1.5F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FLYING_SPEED, 0.48D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static boolean checkSpawnRules(EntityType<GriefErodedChromeDragonEntity> type,
                                           ServerLevelAccessor level, MobSpawnType reason, BlockPos pos,
                                           RandomSource random) {
        if (!level.getBiome(pos).is(SpdBiomes.CHROME_SEABED_CAVES)
                || pos.getY() > level.getSeaLevel() - 8
                || level.getRawBrightness(pos, 0) > 7
                || !level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), type)) {
            return false;
        }
        AABB nearby = new AABB(pos).inflate(32.0D, 16.0D, 32.0D);
        return level.getEntitiesOfClass(GriefErodedChromeDragonEntity.class, nearby, dragon -> dragon.isAlive()).isEmpty();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FLYING, false);
        this.entityData.define(REGENERATING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DragonMeleeGoal(this));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 24, true, false,
                SpdEntityTargeting::isNonSpdLiving));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        this.attackCooldown = Math.max(0, this.attackCooldown - 1);
        this.breathCooldown = Math.max(0, this.breathCooldown - 1);
        this.roarCooldown = Math.max(0, this.roarCooldown - 1);
        this.diveCooldown = Math.max(0, this.diveCooldown - 1);
        this.hardStunTicks = Math.max(0, this.hardStunTicks - 1);

        this.tickRegeneration();
        this.tickCorrosionZones();
        if (this.tickCount % 20 == 0) {
            this.tickGriefDomain();
        }

        if (this.isRegenerating()) {
            this.navigation.stop();
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        LivingEntity target = this.getTarget();
        if (this.isFlying()) {
            this.tickFlight(target);
        } else if (this.breathChargeTicks > 0) {
            this.breathChargeTicks--;
            this.navigation.stop();
            if (this.breathChargeTicks == 0 && target != null && target.isAlive()) {
                this.performChromeBreath(target);
            }
        } else if (target != null && target.isAlive()) {
            double distanceSqr = this.distanceToSqr(target);
            if (this.breathCooldown <= 0 && distanceSqr >= 36.0D) {
                this.breathChargeTicks = BREATH_CHARGE_TICKS;
            } else if (this.roarCooldown <= 0 && distanceSqr <= 144.0D) {
                this.performGriefRoar();
            } else if (this.diveCooldown <= 0 && distanceSqr >= 64.0D) {
                this.startFlight();
            }
        }

        if (this.getHealth() <= this.getMaxHealth() * 0.30F && !this.regenerationUsed) {
            this.startRegeneration();
        }
    }

    private void tickRegeneration() {
        if (this.regenerationTicks <= 0) {
            if (this.isRegenerating()) {
                this.entityData.set(REGENERATING, false);
            }
            return;
        }
        this.regenerationTicks--;
        if (this.tickCount % 4 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                    12, this.getBbWidth() * 0.35D, this.getBbHeight() * 0.35D,
                    this.getBbWidth() * 0.35D, 0.02D);
        }
        if (this.regenerationTicks == 0) {
            this.entityData.set(REGENERATING, false);
        }
    }

    private void startRegeneration() {
        this.regenerationUsed = true;
        this.regenerationTicks = 40;
        this.entityData.set(REGENERATING, true);
        this.heal(40.0F);
        this.removeAllEffects();
    }

    private void tickFlight(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || this.flightTicks++ > 100) {
            this.stopFlight(true);
            return;
        }

        Vec3 origin = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        Vec3 direction = target.getEyePosition().subtract(origin).normalize();
        this.setYRot((float) (Mth.atan2(direction.z, direction.x) * (180.0D / Math.PI)) - 90.0F);
        this.setXRot((float) -(Mth.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z))
                * (180.0D / Math.PI)));
        Vec3 velocity = this.getDeltaMovement().scale(0.72D).add(direction.scale(0.18D));
        this.setDeltaMovement(velocity);
        this.move(net.minecraft.world.entity.MoverType.SELF, velocity);

        if (this.distanceToSqr(target) <= 12.25D) {
            this.performDiveImpact();
        }
    }

    private void startFlight() {
        this.entityData.set(FLYING, true);
        this.flightTicks = 0;
        this.setNoGravity(true);
        this.navigation.stop();
        this.setDeltaMovement(0.0D, 0.45D, 0.0D);
    }

    private void stopFlight(boolean missed) {
        this.entityData.set(FLYING, false);
        this.setNoGravity(false);
        this.flightTicks = 0;
        if (missed) {
            this.hardStunTicks = 20;
            this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        }
    }

    private void performDiveImpact() {
        LivingEntity target = this.getTarget();
        if (target == null) {
            this.stopFlight(true);
            return;
        }

        AABB area = this.getBoundingBox().inflate(1.5D, 0.75D, 1.5D);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, area,
                victim -> victim != this && victim.isAlive() && SpdEntityTargeting.isNonSpdLiving(victim))) {
            this.hurtWithPiercing(victim, 15.0F, 0);
            SpdCorrosion.addAbyssalPressure(victim, 1, 100, this);
            victim.knockback(0.8D, this.getX() - victim.getX(), this.getZ() - victim.getZ());
        }
        this.corrosionZones.add(new CorrosionZone(this.blockPosition(), this.tickCount + 100));
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ASH, this.getX(), this.getY(), this.getZ(), 40,
                    1.5D, 0.4D, 1.5D, 0.08D);
        }
        this.diveCooldown = DIVE_COOLDOWN;
        this.stopFlight(false);
        this.hardStunTicks = 8;
    }

    private void performChromeBreath(LivingEntity focus) {
        this.breathCooldown = BREATH_COOLDOWN;
        Vec3 origin = this.position().add(0.0D, this.getBbHeight() * 0.65D, 0.0D);
        Vec3 direction = focus.getEyePosition().subtract(origin).normalize();
        AABB area = new AABB(origin, origin).inflate(12.0D);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, area,
                victim -> victim != this && victim.isAlive() && SpdEntityTargeting.isNonSpdLiving(victim))) {
            Vec3 toVictim = victim.getEyePosition().subtract(origin);
            double distance = toVictim.length();
            if (distance > 12.0D || direction.dot(toVictim.normalize()) < 0.5D) {
                continue;
            }
            this.hurtWithPiercing(victim, 8.0F, 0);
            SpdCorrosion.addAbyssalPressure(victim, 2, 80, this);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), this);
        }

        for (int step = 1; step <= 12; step++) {
            this.corrosionZones.add(new CorrosionZone(BlockPos.containing(origin.add(direction.scale(step))), this.tickCount + 60));
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, origin.x, origin.y, origin.z, 30,
                    0.3D, 0.3D, 0.3D, 0.06D);
        }
    }

    private void performGriefRoar() {
        this.roarCooldown = ROAR_COOLDOWN;
        AABB area = this.getBoundingBox().inflate(8.0D);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, area,
                victim -> victim != this && victim.isAlive() && SpdEntityTargeting.isNonSpdLiving(victim))) {
            victim.hurt(this.damageSources().magic(), 5.0F);
            SpdCorrosion.addAbyssalPressure(victim, 3, 100, this);
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), this);
            victim.knockback(1.1D, this.getX() - victim.getX(), this.getZ() - victim.getZ());
        }

        BlockPos center = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -1, -8), center.offset(8, 2, 8))) {
            if (this.level().getBlockState(pos).getBlock() instanceof GlassBlock) {
                this.level().destroyBlock(pos, true, this);
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 2 + this.random.nextInt(2); i++) {
                AbyssalErodedSilverfishEntity minion = alku.spd.registry.SpdEntities.ABYSSAL_ERODED_SILVERFISH.get().create(serverLevel);
                if (minion == null) {
                    continue;
                }
                minion.moveTo(this.getX() + this.random.nextGaussian() * 2.0D, this.getY(),
                        this.getZ() + this.random.nextGaussian() * 2.0D, this.getYRot(), 0.0F);
                serverLevel.addFreshEntity(minion);
            }
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(),
                    36, 2.0D, 1.0D, 2.0D, 0.04D);
        }
    }

    private void tickCorrosionZones() {
        if (this.corrosionZones.isEmpty() || this.tickCount % 20 != 0) {
            return;
        }
        Iterator<CorrosionZone> iterator = this.corrosionZones.iterator();
        while (iterator.hasNext()) {
            CorrosionZone zone = iterator.next();
            if (zone.expireTick <= this.tickCount) {
                iterator.remove();
                continue;
            }
            AABB area = new AABB(zone.center).inflate(1.5D, 0.75D, 1.5D);
            for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, area,
                    victim -> victim != this && victim.isAlive() && SpdEntityTargeting.isNonSpdLiving(victim))) {
                SpdCorrosion.addAbyssalPressure(victim, 1, 60, this);
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0), this);
            }
        }
    }

    private void tickGriefDomain() {
        AABB area = this.getBoundingBox().inflate(10.0D);
        Set<UUID> currentTargets = new HashSet<>();
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, area,
                victim -> victim.isAlive() && SpdEntityTargeting.isAbyssalEntity(victim))) {
            applyDomainModifiers(victim);
            currentTargets.add(victim.getUUID());
        }
        for (UUID previous : this.domainTargets) {
            if (!currentTargets.contains(previous) && this.getTrackedEntity(previous) instanceof LivingEntity victim) {
                removeDomainModifiers(victim);
            }
        }
        this.domainTargets.clear();
        this.domainTargets.addAll(currentTargets);

        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, area,
                victim -> victim != this && victim.isAlive() && SpdEntityTargeting.isNonSpdLiving(victim))) {
            SpdCorrosion.addAbyssalPressure(victim, 1, 40, this);
        }
    }

    private void applyDomainModifiers(LivingEntity victim) {
        AttributeInstance attack = victim.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null && attack.getModifier(DOMAIN_ATTACK_ID) == null) {
            attack.addTransientModifier(DOMAIN_ATTACK);
        }
        AttributeInstance armor = victim.getAttribute(Attributes.ARMOR);
        if (armor != null && armor.getModifier(DOMAIN_ARMOR_ID) == null) {
            armor.addTransientModifier(DOMAIN_ARMOR);
        }
    }

    private void removeDomainModifiers(LivingEntity victim) {
        AttributeInstance attack = victim.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.removeModifier(DOMAIN_ATTACK_ID);
        }
        AttributeInstance armor = victim.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(DOMAIN_ARMOR_ID);
        }
    }

    private boolean hurtWithPiercing(LivingEntity victim, float damage, int additionalArmorPierce) {
        AttributeInstance armor = victim.getAttribute(Attributes.ARMOR);
        boolean addedPierce = armor != null && armor.getModifier(ARMOR_PIERCE_ID) == null;
        if (addedPierce) {
            armor.addTransientModifier(new AttributeModifier(ARMOR_PIERCE_ID, "SPD chrome dragon armor pierce",
                    -4.0D - additionalArmorPierce, AttributeModifier.Operation.ADDITION));
        }
        try {
            return victim.hurt(this.damageSources().mobAttack(this), damage);
        } finally {
            if (addedPierce && armor != null) {
                armor.removeModifier(ARMOR_PIERCE_ID);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity victim) || this.attackCooldown > 0 || this.isRegenerating()) {
            return false;
        }
        this.attackCooldown = MELEE_INTERVAL;
        boolean hurt = this.hurtWithPiercing(victim, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE), 0);
        if (hurt) {
            SpdCorrosion.addAbyssalPressure(victim, 3, 15 * 20, this);
            this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        if (this.isRegenerating()) {
            if (source.is(SpdTags.PURIFICATION_DAMAGE)) {
                this.regenerationTicks = 0;
                this.entityData.set(REGENERATING, false);
            } else {
                return false;
            }
        }
        if (source.is(SpdTags.PURIFICATION_DAMAGE)) {
            amount *= 1.5F;
        }
        if (source.getEntity() instanceof Player player && isMetalWeapon(player.getMainHandItem())) {
            amount *= 0.8F;
        }
        return super.hurt(source, amount);
    }

    private static boolean isMetalWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.IRON_SWORD || item == Items.IRON_AXE || item == Items.IRON_PICKAXE
                || item == Items.IRON_SHOVEL || item == Items.IRON_HOE
                || item == Items.GOLDEN_SWORD || item == Items.GOLDEN_AXE || item == Items.GOLDEN_PICKAXE
                || item == Items.GOLDEN_SHOVEL || item == Items.GOLDEN_HOE
                || item == Items.NETHERITE_SWORD || item == Items.NETHERITE_AXE || item == Items.NETHERITE_PICKAXE
                || item == Items.NETHERITE_SHOVEL || item == Items.NETHERITE_HOE;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        Difficulty worldDifficulty = level.getDifficulty();
        double health = worldDifficulty == Difficulty.EASY ? 140.0D : worldDifficulty == Difficulty.HARD ? 240.0D : 180.0D;
        double attack = worldDifficulty == Difficulty.EASY ? 10.0D : worldDifficulty == Difficulty.HARD ? 16.0D : 12.0D;
        AttributeInstance healthAttribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(health);
            this.setHealth((float) health);
        }
        AttributeInstance attackAttribute = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.setBaseValue(attack);
        }
        return data;
    }

    @Override
    public int getExperienceReward() {
        return 50 + this.random.nextInt(31);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(damageSource, looting, recentlyHit);
        this.spawnAtLocation(new ItemStack(SpdItems.DARK_CHROME_DRAGON_SCALE.get(), 3 + this.random.nextInt(4)));
        this.spawnAtLocation(new ItemStack(SpdItems.GRUDGE_CORE_CRYSTAL.get()));
        if (this.random.nextFloat() < 0.60F) {
            this.spawnAtLocation(new ItemStack(SpdItems.CHROME_DRAGON_FANG.get(), 1 + this.random.nextInt(2)));
        }
        if (this.random.nextFloat() < 0.30F) {
            this.spawnAtLocation(new ItemStack(SpdItems.GRIEF_ERODED_DRAGON_WING_MEMBRANE.get()));
        }
    }

    @Override
    public void die(DamageSource source) {
        boolean wasAlive = !this.isDeadOrDying();
        super.die(source);
        if (wasAlive && !this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            this.clearDomainModifiers();
            ChromeDragonContamination.add(serverLevel, this.blockPosition());
        }
    }

    private void clearDomainModifiers() {
        for (UUID targetId : this.domainTargets) {
            if (this.getTrackedEntity(targetId) instanceof LivingEntity victim) {
                removeDomainModifiers(victim);
            }
        }
        this.domainTargets.clear();
    }

    @Nullable
    private Entity getTrackedEntity(UUID id) {
        return this.level() instanceof ServerLevel serverLevel ? serverLevel.getEntity(id) : null;
    }

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    public boolean isRegenerating() {
        return this.entityData.get(REGENERATING);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpdDragonBreathCooldown", this.breathCooldown);
        tag.putInt("SpdDragonRoarCooldown", this.roarCooldown);
        tag.putInt("SpdDragonDiveCooldown", this.diveCooldown);
        tag.putBoolean("SpdDragonRegenerationUsed", this.regenerationUsed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.breathCooldown = Math.max(0, tag.getInt("SpdDragonBreathCooldown"));
        this.roarCooldown = Math.max(0, tag.getInt("SpdDragonRoarCooldown"));
        this.diveCooldown = Math.max(0, tag.getInt("SpdDragonDiveCooldown"));
        this.regenerationUsed = tag.getBoolean("SpdDragonRegenerationUsed");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (this.isDeadOrDying()) {
                state.setAndContinue(DEATH);
            } else if (this.swinging || this.breathChargeTicks > 0) {
                state.setAndContinue(ATTACK);
            } else if (this.isFlying()) {
                state.setAndContinue(FLY);
            } else {
                Vec3 movement = this.getDeltaMovement();
                boolean moving = state.isMoving() || movement.horizontalDistanceSqr() > 1.0E-5D;
                state.setAndContinue(moving ? WALK : IDLE);
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected boolean isImmobile() {
        return this.hardStunTicks > 0 || this.isRegenerating() || super.isImmobile();
    }

    private static final class CorrosionZone {
        private final BlockPos center;
        private final int expireTick;

        private CorrosionZone(BlockPos center, int expireTick) {
            this.center = center;
            this.expireTick = expireTick;
        }
    }

    private static final class DragonMeleeGoal extends MeleeAttackGoal {
        private final GriefErodedChromeDragonEntity dragon;

        private DragonMeleeGoal(GriefErodedChromeDragonEntity dragon) {
            super(dragon, 1.0D, true);
            this.dragon = dragon;
        }

        @Override
        public boolean canUse() {
            return !this.dragon.isFlying() && !this.dragon.isRegenerating() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.dragon.isFlying() && !this.dragon.isRegenerating() && super.canContinueToUse();
        }
    }
}
