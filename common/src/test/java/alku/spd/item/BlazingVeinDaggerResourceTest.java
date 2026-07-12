package alku.spd.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class BlazingVeinDaggerResourceTest {
    @Test
    void thirdPersonRightHandUsesTheSuppliedBlockbenchTransform() throws Exception {
        JsonObject model = resourceJson("/assets/spd/models/item/blazing_vein_dagger.json");
        JsonObject transform = model.getAsJsonObject("display").getAsJsonObject("thirdperson_righthand");

        assertArrayEquals(transform.getAsJsonArray("rotation"), 5.3D, -26.61D, -100.31D);
        assertArrayEquals(transform.getAsJsonArray("translation"), -6.75D, 10.5D, -2.5D);
        assertFalse(transform.has("scale"));
    }

    private static void assertArrayEquals(JsonArray actual, double x, double y, double z) {
        assertEquals(x, actual.get(0).getAsDouble(), 1.0E-9D);
        assertEquals(y, actual.get(1).getAsDouble(), 1.0E-9D);
        assertEquals(z, actual.get(2).getAsDouble(), 1.0E-9D);
    }

    private JsonObject resourceJson(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
