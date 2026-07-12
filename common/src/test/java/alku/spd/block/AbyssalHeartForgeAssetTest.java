package alku.spd.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class AbyssalHeartForgeAssetTest {
    private static final String TEXTURE_SHA256 = "31d2af4eec58698538290950f1d6231ed724c5835b0c21c699b6fdc3e9ba8013";

    @Test
    void modelUsesTheSuppliedTwoLayerPlatformGeometry() throws Exception {
        JsonObject model = resourceJson("/assets/spd/geo/abyssal_heart_forge.geo.json");
        JsonObject geometry = model.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        JsonArray cubes = geometry.getAsJsonArray("bones").get(0).getAsJsonObject().getAsJsonArray("cubes");

        assertEquals(2, cubes.size());
        assertVector(cubes.get(0).getAsJsonObject().getAsJsonArray("origin"), -8.0D, 8.0D, -8.0D);
        assertVector(cubes.get(0).getAsJsonObject().getAsJsonArray("size"), 16.0D, 8.0D, 16.0D);
        assertVector(cubes.get(1).getAsJsonObject().getAsJsonArray("origin"), -8.0D, 0.0D, -8.0D);
        assertVector(cubes.get(1).getAsJsonObject().getAsJsonArray("size"), 16.0D, 8.0D, 16.0D);
    }

    @Test
    void textureMatchesTheSuppliedImage() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/assets/spd/textures/block/abyssal_heart_forge.png")) {
            assertNotNull(stream);
            assertEquals(TEXTURE_SHA256, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));
        }
    }

    private static void assertVector(JsonArray actual, double x, double y, double z) {
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
