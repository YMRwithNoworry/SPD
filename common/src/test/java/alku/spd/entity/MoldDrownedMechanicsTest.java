package alku.spd.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MoldDrownedMechanicsTest {
    @Test
    void transformsAfterThirtyContinuousSecondsUnderwater() {
        int ticks = 0;
        for (int i = 0; i < MoldDrownedMechanics.TRANSFORMATION_TICKS - 1; i++) {
            ticks = MoldDrownedMechanics.updateSubmergedTicks(ticks, true);
        }
        assertFalse(MoldDrownedMechanics.shouldTransform(ticks));

        ticks = MoldDrownedMechanics.updateSubmergedTicks(ticks, true);
        assertTrue(MoldDrownedMechanics.shouldTransform(ticks));
    }

    @Test
    void leavingWaterResetsTransformationProgress() {
        assertEquals(0, MoldDrownedMechanics.updateSubmergedTicks(300, false));
    }

    @Test
    void grudgeBoundAlwaysPullsPlayersDownward() {
        assertEquals(MoldDrownedMechanics.SINK_SPEED, MoldDrownedMechanics.sinkVelocity(0.3D));
        assertEquals(-0.2D, MoldDrownedMechanics.sinkVelocity(-0.2D));
    }
}
