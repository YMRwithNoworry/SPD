package alku.spd.world;

import alku.spd.entity.SpdEntityTargeting;
import alku.spd.registry.SpdBiomes;
import alku.spd.registry.SpdBlocks;
import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SpdCorrosion {
    public static final int MAX_ABYSSAL_PRESSURE_LAYERS = 10;
    public static final int DEFAULT_PRESSURE_DURATION = 20 * 10;

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
        MobEffectInstance instance = entity.getEffect(SpdEffects.ABYSSAL_PRESSURE.get());
        if (instance == null) {
            return;
        }

        if (instance.getAmplifier() <= 0) {
            entity.removeEffect(SpdEffects.ABYSSAL_PRESSURE.get());
            return;
        }

        entity.removeEffectNoUpdate(SpdEffects.ABYSSAL_PRESSURE.get());
        entity.addEffect(new MobEffectInstance(
                SpdEffects.ABYSSAL_PRESSURE.get(),
                DEFAULT_PRESSURE_DURATION,
                instance.getAmplifier() - 1,
                instance.isAmbient(),
                instance.isVisible(),
                instance.showIcon()));
    }

    public static int getAbyssalPressureLayers(LivingEntity entity) {
        if (SpdEntityTargeting.isSpdEntity(entity)) {
            return 0;
        }
        MobEffectInstance instance = entity.getEffect(SpdEffects.ABYSSAL_PRESSURE.get());
        return instance == null ? 0 : Math.min(MAX_ABYSSAL_PRESSURE_LAYERS, instance.getAmplifier() + 1);
    }

    public static void addAbyssalPressure(LivingEntity entity, int layers) {
        addAbyssalPressure(entity, layers, DEFAULT_PRESSURE_DURATION, null);
    }

    public static void addAbyssalPressure(LivingEntity entity, int layers, int duration, LivingEntity source) {
        if (layers <= 0 || entity.level().isClientSide() || SpdEntityTargeting.isSpdEntity(entity)) {
            return;
        }

        MobEffectInstance current = entity.getEffect(SpdEffects.ABYSSAL_PRESSURE.get());
        int currentLayers = current == null ? 0 : current.getAmplifier() + 1;
        if (entity instanceof Player && currentLayers <= 3 && isNearPurificationBlock(entity)) {
            return;
        }

        int resistantPieces = 0;
        for (ItemStack armor : entity.getArmorSlots()) {
            if (armor.is(SpdTags.ABYSSAL_PRESSURE_RESISTANT_ARMOR)) {
                resistantPieces++;
            }
        }
        if (resistantPieces > 0 && entity.getRandom().nextFloat() < resistantPieces * 0.15F) {
            return;
        }

        int newLayers = Math.min(MAX_ABYSSAL_PRESSURE_LAYERS, currentLayers + layers);
        entity.removeEffectNoUpdate(SpdEffects.ABYSSAL_PRESSURE.get());
        entity.addEffect(new MobEffectInstance(
                SpdEffects.ABYSSAL_PRESSURE.get(),
                Math.max(1, duration),
                newLayers - 1,
                false,
                true,
                true), source);
    }

    public static boolean isInAbyssalEnvironment(LivingEntity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        return level.getBiome(pos).is(SpdTags.ABYSSAL_BIOMES)
                || level.getBlockState(pos.below()).is(SpdBlocks.ABYSSAL_BLOOD_SAND.get());
    }

    public static boolean isNearPurificationBlock(LivingEntity entity) {
        BlockPos center = entity.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-5, -3, -5), center.offset(5, 3, 5))) {
            if (entity.level().getBlockState(pos).is(SpdTags.PURIFICATION_BLOCKS)) {
                return true;
            }
        }
        return false;
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
