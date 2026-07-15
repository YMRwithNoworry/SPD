package alku.spd.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MoldHuskMechanicsTest {
    @Test
    void transformsAfterFiveMinutesWithoutAKill() {
        int ticks = 0;
        for (int i = 0; i < MoldHuskMechanics.NO_KILL_TRANSFORMATION_TICKS - 1; i++) {
            ticks = MoldHuskMechanics.advanceNoKillTicks(ticks);
        }
        assertFalse(MoldHuskMechanics.shouldTransform(ticks));
        ticks = MoldHuskMechanics.advanceNoKillTicks(ticks);
        assertTrue(MoldHuskMechanics.shouldTransform(ticks));
    }

    @Test
    void aConfirmedKillResetsTheNoKillTimer() {
        assertEquals(0, MoldHuskMechanics.resetNoKillTicks());
    }

    @Test
    void huskAttackDurationsMatchTheRequestedEffects() {
        assertEquals(20 * 30, MoldHuskMechanics.SLOWNESS_TICKS);
        assertEquals(20 * 10, MoldHuskMechanics.BLINDNESS_TICKS);
    }
}
