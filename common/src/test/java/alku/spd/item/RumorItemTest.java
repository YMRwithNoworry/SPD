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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class RumorItemTest {
    private static final String TEXTURE_SHA256 = "8f82d15389ba5d43325219bd68de8b8d6f7b97443d4e246423da7f28526ee26a";

    @Test
    void registersTheRumorItem() throws Exception {
        assertNotNull(SpdItems.class.getDeclaredField("RUMOR"));
    }

    @Test
    void goldWaveChangesSmoothlyAndRepeats() {
        int start = GoldNameWave.colorAt(0L);
        int quarterCycle = GoldNameWave.colorAt(GoldNameWave.COLOR_CYCLE_MILLIS / 4L);

        assertNotEquals(start, quarterCycle);
        assertEquals(start, GoldNameWave.colorAt(GoldNameWave.COLOR_CYCLE_MILLIS));
        assertEquals(GoldNameWave.colorAt(1L), GoldNameWave.colorAt(GoldNameWave.COLOR_CYCLE_MILLIS + 1L));
    }

    @Test
    void packagesTheSuppliedTextureAndGeneratedItemModel() throws Exception {
        JsonObject model = resourceJson("/assets/spd/models/item/rumor.json");
        assertEquals("minecraft:item/generated", model.get("parent").getAsString());
        assertEquals("spd:item/rumor", model.getAsJsonObject("textures").get("layer0").getAsString());

        try (InputStream stream = getClass().getResourceAsStream("/assets/spd/textures/item/rumor.png")) {
            assertNotNull(stream);
            byte[] textureBytes = stream.readAllBytes();
            assertEquals(TEXTURE_SHA256, sha256(textureBytes));

            BufferedImage texture = ImageIO.read(new ByteArrayInputStream(textureBytes));
            assertNotNull(texture);
            assertEquals(256, texture.getWidth());
            assertEquals(256, texture.getHeight());
        }
    }

    @Test
    void hasChineseAndEnglishNames() throws Exception {
        assertEquals("传闻", resourceJson("/assets/spd/lang/zh_cn.json").get("item.spd.rumor").getAsString());
        assertEquals("Rumor", resourceJson("/assets/spd/lang/en_us.json").get("item.spd.rumor").getAsString());
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
