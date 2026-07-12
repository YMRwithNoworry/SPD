package alku.spd.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SearingPulseEffectTest {
    @Test
    void movingDamageIsHalfAHeartPointPerSecond() {
        assertEquals(0.5F, SearingPulseEffect.MOVING_DAMAGE, 0.0F);
    }
}
