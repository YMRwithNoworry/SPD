package alku.spd;

import alku.spd.registry.SpdBlocks;
import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdCreativeTabs;
import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdItems;
import alku.spd.registry.SpdMenus;
import alku.spd.world.MoldCurseEvents;
import alku.spd.world.SpdDifficultyScaling;
import alku.spd.world.SpdDifficultyEvents;
import alku.spd.world.SpdWeatherEvents;
import alku.spd.world.SpdWeatherNetworking;

public final class Spd {
    public static final String MOD_ID = "spd";

    public static void init() {
        SpdEffects.register();
        SpdBlocks.register();
        SpdBlockEntities.register();
        SpdEntities.register();
        SpdItems.register();
        SpdMenus.register();
        SpdCreativeTabs.register();
        SpdWeatherNetworking.register();
        SpdDifficultyEvents.register();
        SpdDifficultyScaling.register();
        SpdWeatherEvents.register();
        MoldCurseEvents.register();
    }
}
