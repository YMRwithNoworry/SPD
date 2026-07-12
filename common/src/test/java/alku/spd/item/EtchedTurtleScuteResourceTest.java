package alku.spd.item;

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

final class EtchedTurtleScuteResourceTest {
    private static final String TEXTURE_SHA256 = "2ff500d7f343254f2f667ecf03dea17c9a9e5f7d8a211052b3f19548a5781a99";

    @Test
    void usesTheSuppliedEtchedTurtleScuteTexture() throws Exception {
        JsonObject model = resourceJson("/assets/spd/models/item/etched_turtle_scute.json");
        assertEquals("minecraft:item/generated", model.get("parent").getAsString());
        assertEquals("spd:item/etched_turtle_scute",
                model.getAsJsonObject("textures").get("layer0").getAsString());

        try (InputStream stream = getClass().getResourceAsStream("/assets/spd/textures/item/etched_turtle_scute.png")) {
            assertNotNull(stream);
            byte[] textureBytes = stream.readAllBytes();
            assertEquals(TEXTURE_SHA256, sha256(textureBytes));

            BufferedImage texture = ImageIO.read(new ByteArrayInputStream(textureBytes));
            assertNotNull(texture);
            assertEquals(16, texture.getWidth());
            assertEquals(16, texture.getHeight());
        }
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
