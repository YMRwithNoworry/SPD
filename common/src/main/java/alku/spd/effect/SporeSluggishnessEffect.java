package alku.spd.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class SporeSluggishnessEffect extends MobEffect {
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("7154f64c-fb17-4b38-a4db-0c75598ee828");

    public SporeSluggishnessEffect() {
        super(MobEffectCategory.HARMFUL, 0x35151B);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID.toString(),
                -0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
