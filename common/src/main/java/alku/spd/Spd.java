package alku.spd;

import alku.spd.registry.SpdBlocks;
import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdCreativeTabs;
import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdItems;
import alku.spd.registry.SpdRecipeSerializers;
import alku.spd.registry.SpdSounds;
import alku.spd.world.EpxEvents;
import alku.spd.world.AbyssalFoxInfectionEvents;
import alku.spd.world.AbyssalPressureEvents;
import alku.spd.world.AbyssalWolfInfectionEvents;
import alku.spd.world.AbyssalTurtleInfectionEvents;
import alku.spd.world.ChromeDragonContamination;
import alku.spd.world.MoldCurseEvents;
import alku.spd.world.SpdDifficultyScaling;
import alku.spd.world.SpdDifficultyEvents;
import alku.spd.world.SpdBigEyesEvents;
import alku.spd.world.SpdGriefErodedDirtEvents;
import alku.spd.world.SpdWeatherEvents;

public final class Spd {
    public static final String MOD_ID = "spd";

    public static void init() {
        SpdEffects.register();
        SpdSounds.register();
        SpdBlocks.register();
        SpdBlockEntities.register();
        SpdEntities.register();
        SpdItems.register();
        SpdRecipeSerializers.register();
        SpdCreativeTabs.register();
        SpdDifficultyEvents.register();
        SpdDifficultyScaling.register();
        SpdBigEyesEvents.register();
        SpdGriefErodedDirtEvents.register();
        SpdWeatherEvents.register();
        MoldCurseEvents.register();
        AbyssalFoxInfectionEvents.register();
        AbyssalPressureEvents.register();
        AbyssalWolfInfectionEvents.register();
        AbyssalTurtleInfectionEvents.register();
        ChromeDragonContamination.register();
        EpxEvents.register();
    }
}
