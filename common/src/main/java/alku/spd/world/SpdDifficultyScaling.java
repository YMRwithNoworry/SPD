package alku.spd.world;

import alku.spd.entity.SpdEntityTargeting;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.UUID;

public final class SpdDifficultyScaling {
    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("1d03f7a5-68a3-4cfd-9ce6-68b828eb1874");
    private static final UUID ATTACK_MODIFIER_ID = UUID.fromString("bc3bcf4a-ccf1-4cc6-9c8f-64adf5f5a0f1");
    private static final UUID ARMOR_MODIFIER_ID = UUID.fromString("ec151b3e-3035-4a71-9f5c-3ae05f1cfa21");
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("7d37dd86-b1f5-44f6-9cd2-1f46daf92f33");
    private static final UUID FLYING_SPEED_MODIFIER_ID = UUID.fromString("be70c50d-fab5-4c96-a55d-3f845b25d5c2");
    private static final String HEALTH_MODIFIER_NAME = "SPD difficulty health";
    private static final String ATTACK_MODIFIER_NAME = "SPD difficulty attack";
    private static final String ARMOR_MODIFIER_NAME = "SPD difficulty armor";
    private static final String SPEED_MODIFIER_NAME = "SPD difficulty speed";
    private static final String FLYING_SPEED_MODIFIER_NAME = "SPD difficulty flying speed";
    private static final int MOLD_BUFF_DURATION = 20 * 12;

    private SpdDifficultyScaling() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(SpdDifficultyScaling::tickLevel);
    }

    private static void tickLevel(ServerLevel level) {
        if (level.getGameTime() % 20 != 0) {
            return;
        }

        SpdDifficulty.Difficulty difficulty = SpdDifficulty.get(level.getServer());
        for (LivingEntity entity : level.getEntities(EntityTypeTest.forClass(LivingEntity.class), LivingEntity::isAlive)) {
            if (isDifficultyScaledMonster(entity)) {
                applyMonsterScaling(entity, difficulty);
            }

            if (SpdEntityTargeting.isMoldEntity(entity)) {
                applyMoldDifficultyBuffs(entity, difficulty);
            }
        }
    }

    private static boolean isDifficultyScaledMonster(LivingEntity entity) {
        return entity.getType().getCategory() == MobCategory.MONSTER;
    }

    private static void applyMonsterScaling(LivingEntity entity, SpdDifficulty.Difficulty difficulty) {
        double oldMaxHealth = entity.getMaxHealth();
        setModifier(entity, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, HEALTH_MODIFIER_NAME, difficulty.healthBonus());
        setModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_MODIFIER_ID, ATTACK_MODIFIER_NAME, difficulty.attackBonus());
        setModifier(entity, Attributes.ARMOR, ARMOR_MODIFIER_ID, ARMOR_MODIFIER_NAME, difficulty.armorBonus());
        setModifier(entity, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID, SPEED_MODIFIER_NAME, difficulty.speedBonus());
        setModifier(entity, Attributes.FLYING_SPEED, FLYING_SPEED_MODIFIER_ID, FLYING_SPEED_MODIFIER_NAME, difficulty.speedBonus());

        float newMaxHealth = entity.getMaxHealth();
        if (newMaxHealth > oldMaxHealth && entity.getHealth() == oldMaxHealth) {
            entity.setHealth(newMaxHealth);
        } else if (entity.getHealth() > newMaxHealth) {
            entity.setHealth(newMaxHealth);
        }
    }

    private static void setModifier(LivingEntity entity, Attribute attribute, UUID id, String name, double amount) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier oldModifier = instance.getModifier(id);
        if (oldModifier != null) {
            instance.removeModifier(id);
        }

        if (amount != 0.0D) {
            instance.addPermanentModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    private static void applyMoldDifficultyBuffs(LivingEntity entity, SpdDifficulty.Difficulty difficulty) {
        if (difficulty.moldResistanceAmplifier() >= 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, MOLD_BUFF_DURATION,
                    difficulty.moldResistanceAmplifier(), true, false, true));
        }

        if (difficulty.moldRegenerationAmplifier() >= 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, MOLD_BUFF_DURATION,
                    difficulty.moldRegenerationAmplifier(), true, false, true));
        }
    }
}
