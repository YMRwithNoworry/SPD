package alku.spd.item;

import alku.spd.registry.SpdItems;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class PulsingHatredItemResourceTest {
    private static final String TEXTURE_SHA256 = "46f26cada9a12d6cb80c2edf3d78c00541881231c6f5e62214344ebead1a52ba";

    @Test
    void registersThePulsingHatredItem() throws Exception {
        assertNotNull(SpdItems.class.getDeclaredField("PULSING_HATRED"));
    }

    @Test
    void packagesTheSuppliedTextureAndGeneratedItemModel() throws Exception {
        JsonObject model = resourceJson("/assets/spd/models/item/pulsing_hatred.json");
        assertEquals("minecraft:item/generated", model.get("parent").getAsString());
        assertEquals("spd:item/pulsing_hatred", model.getAsJsonObject("textures").get("layer0").getAsString());

        try (InputStream stream = getClass().getResourceAsStream("/assets/spd/textures/item/pulsing_hatred.png")) {
            assertNotNull(stream);
            byte[] textureBytes = stream.readAllBytes();
            assertEquals(TEXTURE_SHA256, sha256(textureBytes));

            BufferedImage texture = ImageIO.read(new ByteArrayInputStream(textureBytes));
            assertNotNull(texture);
            assertEquals(16, texture.getWidth());
            assertEquals(16, texture.getHeight());
        }
    }

    @Test
    void hasChineseAndEnglishNames() throws Exception {
        assertEquals("仍在脉动的仇恨", resourceJson("/assets/spd/lang/zh_cn.json")
                .get("item.spd.pulsing_hatred").getAsString());
        assertEquals("Still-Pulsing Hatred", resourceJson("/assets/spd/lang/en_us.json")
                .get("item.spd.pulsing_hatred").getAsString());
    }

    private JsonObject resourceJson(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
