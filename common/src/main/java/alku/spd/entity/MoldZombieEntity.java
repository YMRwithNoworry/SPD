package alku.spd.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MoldZombieEntity extends Zombie implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("daizhe");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("paobu");
    private static final RawAnimation CHASE = RawAnimation.begin().thenLoop("追逐");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("gongji");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("siwang");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MoldZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.276D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, SpdEntityTargeting::isNonSpdLiving));
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
                state.setAndContinue(this.getTarget() != null && this.getTarget().isAlive() ? CHASE : WALK);
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

