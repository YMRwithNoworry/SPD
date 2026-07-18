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

final class ContradictionDyeItemResourceTest {
    private static final String TEXTURE_SHA256 = "8145772933d1c00d5f76216281e31412ad25bdf7642cc38d74544123f167a44c";

    @Test
    void registersTheContradictionDyeItem() throws Exception {
        assertNotNull(SpdItems.class.getDeclaredField("CONTRADICTION_DYE"));
    }

    @Test
    void packagesTheSuppliedTextureAndGeneratedItemModel() throws Exception {
        JsonObject model = resourceJson("/assets/spd/models/item/contradiction_dye.json");
        assertEquals("minecraft:item/generated", model.get("parent").getAsString());
        assertEquals("spd:item/contradiction_dye", model.getAsJsonObject("textures").get("layer0").getAsString());

        try (InputStream stream = getClass().getResourceAsStream("/assets/spd/textures/item/contradiction_dye.png")) {
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
        assertEquals("矛盾染料", resourceJson("/assets/spd/lang/zh_cn.json")
                .get("item.spd.contradiction_dye").getAsString());
        assertEquals("Contradiction Dye", resourceJson("/assets/spd/lang/en_us.json")
                .get("item.spd.contradiction_dye").getAsString());
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
