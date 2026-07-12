package alku.spd.entity;

import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AbyssalTurtleEntity extends Turtle implements GeoEntity {
    private static final double LAND_SPEED = 0.15D;
    private static final double WATER_SPEED = 0.32D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new AdaptiveMeleeAttackGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
    }

    @Override
    public void aiStep() {
        this.updateEnvironmentAttributes();
        super.aiStep();
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
        if (source.is(SpdTags.PURIFICATION_DAMAGE)) {
            amount *= AbyssalTurtleMechanics.purificationDamageMultiplier();
        } else if (source.is(DamageTypeTags.IS_FIRE)) {
            amount *= AbyssalTurtleMechanics.fireDamageMultiplier();
        }
        return super.hurt(source, amount);
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
        controllers.add(new AnimationController<>(this, "base", 5, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
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
    }
}
