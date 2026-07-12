package alku.spd.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BlazingVeinDaggerStateTest {
    @Test
    void timeoutStartsAFreshSwiftEdgeChain() {
        BlazingVeinDaggerState.HitResult result = BlazingVeinDaggerState.onHit(3, 100L, 117L);

        assertEquals(1, result.layers());
        assertFalse(result.instantSlash());
    }

    @Test
    void missingLastHitStartsAFreshSwiftEdgeChainWithoutOverflow() {
        BlazingVeinDaggerState.HitResult result = BlazingVeinDaggerState.onHit(3, Long.MIN_VALUE, 100L);

        assertEquals(1, result.layers());
        assertFalse(result.instantSlash());
    }

    @Test
    void fifthStoredLayerMakesTheNextHitAnInstantSlash() {
        BlazingVeinDaggerState.HitResult result = BlazingVeinDaggerState.onHit(5, 100L, 116L);

        assertEquals(0, result.layers());
        assertTrue(result.instantSlash());
    }

    @Test
    void fiveLayersUseMultiplicativeAttackSpeed() {
        assertEquals(Math.pow(1.04D, 5), BlazingVeinDaggerState.attackSpeedMultiplier(5), 1.0E-9D);
    }

    @Test
    void malformedStoredLayersCannotExceedTheInstantSlashThreshold() {
        BlazingVeinDaggerState.HitResult result = BlazingVeinDaggerState.onHit(99, 40L, 41L);

        assertEquals(0, result.layers());
        assertTrue(result.instantSlash());
    }
}
