package alku.spd.mixin;

import alku.spd.registry.SpdBiomes;
import alku.spd.registry.SpdBlocks;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SurfaceRuleData.class)
public abstract class SurfaceRuleDataMixin {
    @Inject(method = "overworld", at = @At("RETURN"), cancellable = true)
    private static void spd$addAbyssalBloodDesertSurface(CallbackInfoReturnable<SurfaceRules.RuleSource> cir) {
        SurfaceRules.RuleSource bloodSand = SurfaceRules.state(SpdBlocks.ABYSSAL_BLOOD_SAND.get().defaultBlockState());
        SurfaceRules.RuleSource bloodDesert = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(SpdBiomes.ABYSSAL_BLOOD_DESERT),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, bloodSand),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, bloodSand),
                        SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, bloodSand)));

        cir.setReturnValue(SurfaceRules.sequence(bloodDesert, cir.getReturnValue()));
    }
}
