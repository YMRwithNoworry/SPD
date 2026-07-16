package alku.spd.block;

import alku.spd.registry.SpdBlocks;
import alku.spd.registry.SpdItems;
import com.google.gson.JsonArray;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PulseCoreBlockResourceTest {
    private static final String TEXTURE_SHA256 = "c846420f5e953d0cf1e2ef8e9fe0488c685eb261b48a6c9d13f2a2bf3b0e43c0";

    @Test
    void registersTheBlockAndItsPlaceableItem() throws Exception {
        assertNotNull(SpdBlocks.class.getDeclaredField("PULSE_CORE"));
        assertNotNull(SpdItems.class.getDeclaredField("PULSE_CORE"));
    }

    @Test
    void packagesCubeResourcesAndUsesTheSuppliedTexture() throws Exception {
        JsonObject blockState = resourceJson("/assets/spd/blockstates/pulse_core.json");
        assertEquals("spd:block/pulse_core", blockState.getAsJsonObject("variants")
                .getAsJsonObject("").get("model").getAsString());

        JsonObject blockModel = resourceJson("/assets/spd/models/block/pulse_core.json");
        assertEquals("minecraft:block/cube_all", blockModel.get("parent").getAsString());
        assertEquals("spd:block/pulse_core", blockModel.getAsJsonObject("textures").get("all").getAsString());

        JsonObject itemModel = resourceJson("/assets/spd/models/item/pulse_core.json");
        assertEquals("spd:block/pulse_core", itemModel.get("parent").getAsString());

        try (InputStream stream = getClass().getResourceAsStream("/assets/spd/textures/block/pulse_core.png")) {
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
    void dropsItselfAndCanBeMinedWithAPickaxe() throws Exception {
        JsonObject lootTable = resourceJson("/data/spd/loot_tables/blocks/pulse_core.json");
        JsonObject entry = lootTable.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject();
        assertEquals("spd:pulse_core", entry.get("name").getAsString());

        JsonArray values = resourceJson("/data/minecraft/tags/blocks/mineable/pickaxe.json").getAsJsonArray("values");
        assertTrue(contains(values, "spd:pulse_core"));
    }

    @Test
    void hasChineseAndEnglishBlockNames() throws Exception {
        assertEquals("脉冲核心", resourceJson("/assets/spd/lang/zh_cn.json")
                .get("block.spd.pulse_core").getAsString());
        assertEquals("Pulse Core", resourceJson("/assets/spd/lang/en_us.json")
                .get("block.spd.pulse_core").getAsString());
    }

    private static boolean contains(JsonArray values, String expected) {
        for (int index = 0; index < values.size(); index++) {
            if (expected.equals(values.get(index).getAsString())) {
                return true;
            }
        }
        return false;
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
