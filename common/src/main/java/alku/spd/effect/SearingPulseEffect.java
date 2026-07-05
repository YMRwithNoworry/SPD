package alku.spd.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class SearingPulseEffect extends MobEffect {
    public SearingPulseEffect() {
        super(MobEffectCategory.HARMFUL, 0xD93D25);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.getDeltaMovement().horizontalDistanceSqr() > 0.0025D) {
            entity.hurt(entity.damageSources().onFire(), 1.0F);
        }
    }
}
