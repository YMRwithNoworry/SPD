package alku.spd.world;

import alku.spd.registry.SpdBiomes;
import alku.spd.registry.SpdEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class SpdCorrosion {
    private SpdCorrosion() {
    }

    public static boolean isErosionBoost(MobEffect effect) {
        return effect == SpdEffects.ABYSSAL_BOOST.get();
    }

    public static boolean canReceiveErosionBoost(LivingEntity entity, MobEffect effect) {
        return !isErosionBoost(effect) || !entity.hasEffect(SpdEffects.EROSION_SUPPRESSION.get());
    }

    public static void reduceAbyssalBoostLayer(LivingEntity entity) {
        reduceEffectLayer(entity, SpdEffects.ABYSSAL_BOOST.get());
    }

    public static void reduceAbyssalPressureLayer(LivingEntity entity) {
        reduceEffectLayer(entity, SpdEffects.ABYSSAL_PRESSURE.get());
    }

    public static boolean isInAbyssalBiome(Player player) {
        return player.level().getBiome(player.blockPosition()).is(SpdBiomes.ABYSSAL_BLOOD_DESERT);
    }

    public static float getAbyssalPressureAccumulationMultiplier(Player player) {
        return isInAbyssalBiome(player) ? 0.7F : 1.0F;
    }

    private static void reduceEffectLayer(LivingEntity entity, MobEffect effect) {
        MobEffectInstance instance = entity.getEffect(effect);
        if (instance == null) {
            return;
        }

        entity.removeEffectNoUpdate(effect);
        if (instance.getAmplifier() > 0) {
            entity.addEffect(new MobEffectInstance(
                    effect,
                    instance.getDuration(),
                    instance.getAmplifier() - 1,
                    instance.isAmbient(),
                    instance.isVisible(),
                    instance.showIcon()));
        }
    }
}
