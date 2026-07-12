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

final class BloodAshIngotBlockResourceTest {
    private static final String TEXTURE_SHA256 = "8318dc02228b3579fe067860e45868e53c00d8f72b5f8a7ae2e467a46d0dae81";

    @Test
    void registersTheBlockAndItsPlaceableItem() throws Exception {
        assertNotNull(SpdBlocks.class.getDeclaredField("BLOOD_ASH_INGOT_BLOCK"));
        assertNotNull(SpdItems.class.getDeclaredField("BLOOD_ASH_INGOT_BLOCK"));
    }

    @Test
    void compactsNineBloodAshIngots() throws Exception {
        JsonObject recipe = resourceJson("/data/spd/recipes/blood_ash_ingot_block.json");
        JsonArray pattern = recipe.getAsJsonArray("pattern");

        assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertEquals("###", pattern.get(0).getAsString());
        assertEquals("###", pattern.get(1).getAsString());
        assertEquals("###", pattern.get(2).getAsString());
        assertEquals("spd:blood_ash_ingot", recipe.getAsJsonObject("key").getAsJsonObject("#").get("item").getAsString());
        assertEquals("spd:blood_ash_ingot_block", recipe.getAsJsonObject("result").get("item").getAsString());
        assertEquals(1, recipe.getAsJsonObject("result").get("count").getAsInt());
    }

    @Test
    void packagesBlockResourcesAndUsesTheSuppliedTexture() throws Exception {
        JsonObject blockState = resourceJson("/assets/spd/blockstates/blood_ash_ingot_block.json");
        assertEquals("spd:block/blood_ash_ingot_block", blockState.getAsJsonObject("variants")
                .getAsJsonObject("").get("model").getAsString());

        JsonObject blockModel = resourceJson("/assets/spd/models/block/blood_ash_ingot_block.json");
        assertEquals("minecraft:block/cube_all", blockModel.get("parent").getAsString());
        assertEquals("spd:block/blood_ash_ingot_block", blockModel.getAsJsonObject("textures").get("all").getAsString());

        JsonObject itemModel = resourceJson("/assets/spd/models/item/blood_ash_ingot_block.json");
        assertEquals("spd:block/blood_ash_ingot_block", itemModel.get("parent").getAsString());

        try (InputStream stream = getClass().getResourceAsStream("/assets/spd/textures/block/blood_ash_ingot_block.png")) {
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
        JsonObject lootTable = resourceJson("/data/spd/loot_tables/blocks/blood_ash_ingot_block.json");
        JsonObject entry = lootTable.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject();
        assertEquals("spd:blood_ash_ingot_block", entry.get("name").getAsString());

        JsonArray values = resourceJson("/data/minecraft/tags/blocks/mineable/pickaxe.json").getAsJsonArray("values");
        assertTrue(contains(values, "spd:blood_ash_ingot_block"));
    }

    @Test
    void hasChineseAndEnglishBlockNames() throws Exception {
        assertEquals("血烬锭块", resourceJson("/assets/spd/lang/zh_cn.json")
                .get("block.spd.blood_ash_ingot_block").getAsString());
        assertEquals("Blood Ash Ingot Block", resourceJson("/assets/spd/lang/en_us.json")
                .get("block.spd.blood_ash_ingot_block").getAsString());
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
