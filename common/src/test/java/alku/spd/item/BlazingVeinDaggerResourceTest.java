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
    void usesTheSuppliedDisplayPreset() throws Exception {
        JsonObject model = resourceJson("/assets/spd/models/item/blazing_vein_dagger.json");
        JsonObject display = model.getAsJsonObject("display");

        assertTransform(display, "thirdperson_righthand",
                new double[]{93.25D, -90.0D, 0.0D},
                new double[]{0.0D, 8.5D, -5.0D},
                new double[]{0.85D, 0.85D, 0.85D});
        assertTransform(display, "thirdperson_lefthand",
                new double[]{-84.69D, 75.78D, -172.88D},
                new double[]{-1.75D, 8.5D, -3.0D},
                new double[]{0.85D, 0.85D, 0.85D});
        assertTransform(display, "firstperson_righthand",
                new double[]{37.75D, -90.0D, 0.0D},
                new double[]{-0.12D, 1.2D, -3.87D},
                new double[]{0.68D, 0.68D, 0.68D});
        assertTransform(display, "firstperson_lefthand",
                new double[]{-109.75D, 85.75D, 142.25D},
                new double[]{-0.12D, 1.2D, -3.87D},
                new double[]{0.68D, 0.68D, 0.68D});
        assertTransform(display, "gui",
                new double[]{27.0D, 0.0D, -41.0D},
                new double[]{-7.5D, -3.75D, 0.0D},
                new double[]{0.9D, 0.9D, 0.9D});
        assertTransform(display, "fixed",
                new double[]{0.0D, 0.0D, -37.25D},
                new double[]{-8.0D, -4.75D, 0.25D},
                null);
        assertTransform(display, "ground",
                null,
                new double[]{-7.25D, -6.0D, 11.25D},
                new double[]{2.0D, 2.0D, 2.0D});
    }

    private static void assertTransform(JsonObject display, String context, double[] rotation, double[] translation,
                                        double[] scale) {
        JsonObject transform = display.getAsJsonObject(context);
        assertNotNull(transform, context);

        assertOptionalArrayEquals(transform, "rotation", rotation);
        assertOptionalArrayEquals(transform, "translation", translation);
        assertOptionalArrayEquals(transform, "scale", scale);
    }

    private static void assertOptionalArrayEquals(JsonObject transform, String member, double[] expected) {
        if (expected == null) {
            assertFalse(transform.has(member), member);
            return;
        }

        JsonArray actual = transform.getAsJsonArray(member);
        assertNotNull(actual, member);
        assertEquals(expected.length, actual.size(), member);
        for (int index = 0; index < expected.length; index++) {
            assertEquals(expected[index], actual.get(index).getAsDouble(), 1.0E-9D, member + "[" + index + "]");
        }
    }

    private JsonObject resourceJson(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
