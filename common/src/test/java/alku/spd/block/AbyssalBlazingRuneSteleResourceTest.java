package alku.spd.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class AbyssalBlazingRuneSteleResourceTest {
    @Test
    void recipeUsesFourObsidianAroundOneAmethystShard() throws Exception {
        JsonObject recipe = resourceJson("/data/spd/recipes/abyssal_blazing_rune_stele.json");
        JsonArray pattern = recipe.getAsJsonArray("pattern");

        assertEquals(" O ", pattern.get(0).getAsString());
        assertEquals("OAO", pattern.get(1).getAsString());
        assertEquals(" O ", pattern.get(2).getAsString());
        assertEquals("minecraft:obsidian", recipe.getAsJsonObject("key").getAsJsonObject("O").get("item").getAsString());
        assertEquals("minecraft:amethyst_shard", recipe.getAsJsonObject("key").getAsJsonObject("A").get("item").getAsString());
        assertEquals("spd:abyssal_blazing_rune_stele", recipe.getAsJsonObject("result").get("item").getAsString());
        assertEquals(1, recipe.getAsJsonObject("result").get("count").getAsInt());
    }

    @Test
    void clientAndLootResourcesArePackaged() {
        assertNotNull(getClass().getResource("/assets/spd/geo/abyssal_blazing_rune_stele.geo.json"));
        assertNotNull(getClass().getResource("/assets/spd/textures/block/abyssal_blazing_rune_stele.png"));
        assertNotNull(getClass().getResource("/assets/spd/blockstates/abyssal_blazing_rune_stele.json"));
        assertNotNull(getClass().getResource("/assets/spd/models/item/abyssal_blazing_rune_stele.json"));
        assertNotNull(getClass().getResource("/data/spd/loot_tables/blocks/abyssal_blazing_rune_stele.json"));
    }

    @Test
    void modelIsCenteredOnThePlacedBlock() throws Exception {
        JsonObject model = resourceJson("/assets/spd/geo/abyssal_blazing_rune_stele.geo.json");
        JsonObject geometry = model.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        JsonObject mainCube = geometry.getAsJsonArray("bones").get(0).getAsJsonObject()
                .getAsJsonArray("cubes").get(0).getAsJsonObject();
        double originX = mainCube.getAsJsonArray("origin").get(0).getAsDouble();
        double width = mainCube.getAsJsonArray("size").get(0).getAsDouble();

        assertEquals(15.0D, width, 1.0E-9D);
        assertEquals(0.0D, originX + width / 2.0D, 1.0E-9D);
    }

    private JsonObject resourceJson(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
