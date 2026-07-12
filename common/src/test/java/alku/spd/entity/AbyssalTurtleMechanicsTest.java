package alku.spd.entity;

import net.minecraft.world.Difficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AbyssalTurtleMechanicsTest {
    @Test
    void biteDamageMatchesWorldDifficulty() {
        assertEquals(0.0F, AbyssalTurtleMechanics.biteDamage(Difficulty.PEACEFUL), 0.0F);
        assertEquals(3.0F, AbyssalTurtleMechanics.biteDamage(Difficulty.EASY), 0.0F);
        assertEquals(4.0F, AbyssalTurtleMechanics.biteDamage(Difficulty.NORMAL), 0.0F);
        assertEquals(6.0F, AbyssalTurtleMechanics.biteDamage(Difficulty.HARD), 0.0F);
    }

    @Test
    void attackIntervalIsFasterInWater() {
        assertEquals(26, AbyssalTurtleMechanics.attackInterval(false));
        assertEquals(19, AbyssalTurtleMechanics.attackInterval(true));
    }

    @Test
    void armorIsHigherOnLand() {
        assertEquals(10.0D, AbyssalTurtleMechanics.armor(false), 0.0D);
        assertEquals(8.0D, AbyssalTurtleMechanics.armor(true), 0.0D);
    }

    @Test
    void damageMultipliersMatchDefensiveRules() {
        assertEquals(0.3F, AbyssalTurtleMechanics.shellDamageMultiplier(), 0.0F);
        assertEquals(0.6F, AbyssalTurtleMechanics.fireDamageMultiplier(), 0.0F);
        assertEquals(2.0F, AbyssalTurtleMechanics.purificationDamageMultiplier(), 0.0F);
    }

    @Test
    void infectionProgressChangesOncePerSecondAndClamps() {
        assertEquals(120, AbyssalTurtleMechanics.infectionProgress(100, true));
        assertEquals(95, AbyssalTurtleMechanics.infectionProgress(100, false));
        assertEquals(6000, AbyssalTurtleMechanics.infectionProgress(5990, true));
        assertEquals(0, AbyssalTurtleMechanics.infectionProgress(3, false));
    }
}
