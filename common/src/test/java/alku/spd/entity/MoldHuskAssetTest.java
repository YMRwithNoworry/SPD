package alku.spd.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MoldHuskAssetTest {
    private static final String MODEL_PATH = "/assets/spd/geo/mold_husk.geo.json";
    private static final String ANIMATION_PATH = "/assets/spd/animations/mold_husk.animation.json";
    private static final String TEXTURE_PATH = "/assets/spd/textures/entity/mold_husk.png";

    @Test
    void packagesSuppliedAssets() throws Exception {
        byte[] model = resourceBytes(MODEL_PATH);
        byte[] animation = resourceBytes(ANIMATION_PATH);
        byte[] texture = resourceBytes(TEXTURE_PATH);
        assertEquals("1b9949a9af8d8bda8d497f0c437292ec3cf4ea4118951c5d10d31cb7d0dc3173", sha256(normalizeLines(model)));
        assertEquals("3ed669cbb181426f80441d1cf97306d9891e0acecf54e3422019f6236ea70a4f", sha256(normalizeLines(animation)));
        assertEquals("1b9e4ad08bcbc1c79175aa4a93c8ee51849878e4358c01b1d3df7355a647439f", sha256(texture));

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(texture));
        assertNotNull(image);
        assertEquals(128, image.getWidth());
        assertEquals(128, image.getHeight());
    }

    @Test
    void animationsOnlyReferenceModelBones() throws Exception {
        JsonObject model = json(MODEL_PATH);
        JsonObject animationFile = json(ANIMATION_PATH);
        Set<String> modelBones = new HashSet<>();
        for (JsonElement bone : model.getAsJsonArray("minecraft:geometry").get(0)
                .getAsJsonObject().getAsJsonArray("bones")) {
            modelBones.add(bone.getAsJsonObject().get("name").getAsString());
        }

        JsonObject animations = animationFile.getAsJsonObject("animations");
        assertTrue(animations.has("daizhe"));
        assertTrue(animations.has("paobu"));
        assertTrue(animations.has("追逐"));
        assertTrue(animations.has("gongji"));
        assertTrue(animations.has("siwang"));
        for (String animationName : animations.keySet()) {
            JsonObject animation = animations.getAsJsonObject(animationName);
            if (!animation.has("bones")) {
                continue;
            }
            for (String animatedBone : animation.getAsJsonObject("bones").keySet()) {
                assertTrue(modelBones.contains(animatedBone), animationName + " references missing bone " + animatedBone);
            }
        }
    }

    private JsonObject json(String path) throws Exception {
        return JsonParser.parseString(new String(resourceBytes(path), StandardCharsets.UTF_8)).getAsJsonObject();
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

    private static byte[] normalizeLines(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8);
    }
}
