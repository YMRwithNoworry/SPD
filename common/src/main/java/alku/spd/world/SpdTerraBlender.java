package alku.spd.world;

import alku.spd.Spd;
import alku.spd.registry.SpdBiomes;
import alku.spd.registry.SpdBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

public final class SpdTerraBlender {
    private static final int BLOOD_DESERT_REGION_WEIGHT = 3;
    private static boolean registered;

    private SpdTerraBlender() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        Regions.register(new AbyssalBloodDesertRegion(
                new ResourceLocation(Spd.MOD_ID, "abyssal_blood_desert"),
                BLOOD_DESERT_REGION_WEIGHT));

        SurfaceRules.RuleSource bloodDesertSurfaceRule = bloodDesertSurfaceRule();
        SurfaceRuleManager.addSurfaceRules(
                SurfaceRuleManager.RuleCategory.OVERWORLD,
                Spd.MOD_ID,
                bloodDesertSurfaceRule);
        SurfaceRuleManager.addToDefaultSurfaceRulesAtStage(
                SurfaceRuleManager.RuleCategory.OVERWORLD,
                SurfaceRuleManager.RuleStage.AFTER_BEDROCK,
                1000,
                bloodDesertSurfaceRule);
    }

    private static SurfaceRules.RuleSource bloodDesertSurfaceRule() {
        SurfaceRules.RuleSource bloodSand = SurfaceRules.state(SpdBlocks.ABYSSAL_BLOOD_SAND.get().defaultBlockState());
        return SurfaceRules.ifTrue(
                SurfaceRules.isBiome(SpdBiomes.ABYSSAL_BLOOD_DESERT),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, bloodSand),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, bloodSand),
                        SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, bloodSand)));
    }
}
