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

final class TierThreeCompositeBlockResourceTest {
    private static final String TEXTURE_SHA256 = "030294f6b3c3bdd755c33363dbc8c7b982d979ebe89b5c7f27463fc395f2376a";

    @Test
    void registersTheBlockAndItsPlaceableItem() throws Exception {
        assertNotNull(SpdBlocks.class.getDeclaredField("TIER_THREE_COMPOSITE_BLOCK"));
        assertNotNull(SpdItems.class.getDeclaredField("TIER_THREE_COMPOSITE_BLOCK"));
    }

    @Test
    void packagesCubeResourcesAndUsesTheSuppliedTexture() throws Exception {
        JsonObject blockState = resourceJson("/assets/spd/blockstates/tier_three_composite_block.json");
        assertEquals("spd:block/tier_three_composite_block", blockState.getAsJsonObject("variants")
                .getAsJsonObject("").get("model").getAsString());

        JsonObject blockModel = resourceJson("/assets/spd/models/block/tier_three_composite_block.json");
        assertEquals("minecraft:block/cube_all", blockModel.get("parent").getAsString());
        assertEquals("spd:block/tier_three_composite_block", blockModel.getAsJsonObject("textures").get("all").getAsString());

        JsonObject itemModel = resourceJson("/assets/spd/models/item/tier_three_composite_block.json");
        assertEquals("spd:block/tier_three_composite_block", itemModel.get("parent").getAsString());

        try (InputStream stream = getClass().getResourceAsStream("/assets/spd/textures/block/tier_three_composite_block.png")) {
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
    void dropsItselfAndRequiresAnIronBlockEquivalentTool() throws Exception {
        JsonObject lootTable = resourceJson("/data/spd/loot_tables/blocks/tier_three_composite_block.json");
        JsonObject entry = lootTable.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject();
        assertEquals("spd:tier_three_composite_block", entry.get("name").getAsString());

        assertTrue(contains(resourceJson("/data/minecraft/tags/blocks/mineable/pickaxe.json")
                .getAsJsonArray("values"), "spd:tier_three_composite_block"));
        assertTrue(contains(resourceJson("/data/minecraft/tags/blocks/needs_stone_tool.json")
                .getAsJsonArray("values"), "spd:tier_three_composite_block"));
    }

    @Test
    void hasChineseAndEnglishBlockNames() throws Exception {
        assertEquals("三级复合块", resourceJson("/assets/spd/lang/zh_cn.json")
                .get("block.spd.tier_three_composite_block").getAsString());
        assertEquals("Tier Three Composite Block", resourceJson("/assets/spd/lang/en_us.json")
                .get("block.spd.tier_three_composite_block").getAsString());
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
