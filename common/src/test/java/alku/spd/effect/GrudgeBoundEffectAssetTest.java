package alku.spd.effect;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class GrudgeBoundEffectAssetTest {
    private static final String ICON_PATH = "/assets/spd/textures/mob_effect/grudge_bound.png";

    @Test
    void packagesSuppliedIcon() throws Exception {
        byte[] icon;
        try (InputStream stream = getClass().getResourceAsStream(ICON_PATH)) {
            assertNotNull(stream, ICON_PATH);
            icon = stream.readAllBytes();
        }

        assertEquals("84df18266ded363a16a4b90b2da2ca2e6aac13f3689fa7262ef7b120f6d33fc8", sha256(icon));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(icon));
        assertNotNull(image, ICON_PATH);
        assertEquals(16, image.getWidth());
        assertEquals(16, image.getHeight());
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
