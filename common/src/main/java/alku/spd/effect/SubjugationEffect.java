package alku.spd.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SubjugationEffect extends MobEffect {
    public SubjugationEffect() {
        super(MobEffectCategory.HARMFUL, 0x5B5B60);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "8cf97ca4-90fd-40ea-a720-5b52ca4bc89c",
                -0.8D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
