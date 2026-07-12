package alku.spd.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AbyssalWolfAnimationTest {
    @Test
    void limbSwingKeepsWalkAnimationActiveWhenGeckoLibMotionFlagIsFalse() {
        assertTrue(AbyssalWolfAnimation.isMoving(false, false, 0.01F, 0.0D, 0.0D));
    }

    @Test
    void walkAnimationStateKeepsWalkAnimationActiveDuringClientInterpolation() {
        assertTrue(AbyssalWolfAnimation.isMoving(false, true, 0.0F, 0.0D, 0.0D));
    }

    @Test
    void noMotionKeepsIdleAnimationActive() {
        assertFalse(AbyssalWolfAnimation.isMoving(false, false, 0.0F, 0.0D, 0.0D));
    }
}
