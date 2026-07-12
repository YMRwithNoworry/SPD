package alku.spd.item;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class BlazingVeinDaggerItemTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void instantSlashUsesSeventyPercentOfNormalAttackAndEnchantmentDamage() {
        assertEquals(4.9F, BlazingVeinDaggerItem.instantSlashDamage(5.0F, 2.0F), 1.0E-6F);
    }

    @Test
    void swiftEdgeSpeedModifierUsesTheMultiplicativeDelta() {
        double expected = Math.pow(1.04D, 5) - 1.0D;

        assertEquals(expected, BlazingVeinDaggerItem.attackSpeedModifierAmount(5), 1.0E-9D);
    }

    @Test
    void commonModuleDoesNotShipALoaderPlatformBridge() {
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("alku.spd.platform.SpdPlatform"));
    }
}
