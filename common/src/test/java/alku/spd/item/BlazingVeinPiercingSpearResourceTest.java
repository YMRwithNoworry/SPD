package alku.spd.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.animatable.GeoItem;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BlazingVeinPiercingSpearResourceTest {
    @Test
    void usesTheSuppliedGeckoLibModelAndTexture() throws Exception {
        assertTrue(GeoItem.class.isAssignableFrom(BlazingVeinPiercingSpearItem.class));

        JsonObject geometry = resourceJson("/assets/spd/geo/blazing_vein_piercing_spear.geo.json");
        assertEquals("eaeb2b2f9bd97c61277979aa9bf1fc02e151048656695cf44f72c8aec3128ef2",
                sha256(resourceBytes("/assets/spd/geo/blazing_vein_piercing_spear.geo.json")));
        assertEquals("1.21.110", geometry.get("format_version").getAsString());
        JsonObject description = geometry.getAsJsonArray("minecraft:geometry")
                .get(0).getAsJsonObject()
                .getAsJsonObject("description");
        assertEquals(128, description.get("texture_width").getAsInt());
        assertEquals(128, description.get("texture_height").getAsInt());

        try (InputStream stream = getClass().getResourceAsStream("/assets/spd/textures/item/blazing_vein_piercing_spear.png")) {
            assertNotNull(stream);
            byte[] textureBytes = stream.readAllBytes();
            assertEquals("6f3e02fd96d658f23941985ca8d443793d215007a2a9a932b3cccaf994fd92c1", sha256(textureBytes));

            BufferedImage texture = ImageIO.read(new ByteArrayInputStream(textureBytes));
            assertNotNull(texture);
            assertEquals(128, texture.getWidth());
            assertEquals(128, texture.getHeight());
        }
    }

    @Test
    void usesTheSuppliedDisplayPreset() throws Exception {
        JsonObject model = resourceJson("/assets/spd/models/item/blazing_vein_piercing_spear.json");
        assertEquals("builtin/entity", model.get("parent").getAsString());
        JsonObject display = model.getAsJsonObject("display");

        assertTransform(display, "thirdperson_righthand",
                new double[]{101.43D, -38.04D, -40.53D},
                new double[]{-21.5D, 19.25D, -28.25D},
                new double[]{0.85D, 0.85D, 0.85D});
        assertTransform(display, "thirdperson_lefthand",
                new double[]{101.43D, -38.04D, -40.53D},
                new double[]{-21.5D, 19.25D, -28.25D},
                new double[]{0.85D, 0.85D, 0.85D});
        assertTransform(display, "firstperson_righthand",
                new double[]{60.0D, 12.0D, 0.0D},
                new double[]{19.0D, -27.0D, -24.62D},
                new double[]{0.68D, 0.68D, 0.68D});
        assertTransform(display, "firstperson_lefthand",
                new double[]{60.0D, 12.0D, 0.0D},
                new double[]{9.5D, -27.0D, -24.62D},
                new double[]{0.68D, 0.68D, 0.68D});
        assertTransform(display, "gui",
                new double[]{27.0D, 0.24D, -41.07D},
                new double[]{-8.25D, -22.0D, 0.0D},
                new double[]{0.5D, 0.5D, 0.5D});
        assertTransform(display, "ground", null, null, new double[]{0.5D, 0.5D, 0.5D});
        assertTransform(display, "fixed",
                new double[]{68.25D, 38.92D, -113.19D},
                new double[]{-20.25D, -34.5D, 17.75D},
                null);
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

    private byte[] resourceBytes(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return stream.readAllBytes();
        }
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
