package alku.spd.effect;

import alku.spd.registry.SpdEffects;
import net.minecraft.world.entity.LivingEntity;

public final class SubjugationHooks {
    private SubjugationHooks() {
    }

    public static boolean isSubjugated(LivingEntity entity) {
        return entity != null && entity.hasEffect(SpdEffects.SUBJUGATION.get());
    }
}
