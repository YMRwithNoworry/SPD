package alku.spd.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MoldZombieAssetTest {
    private static final String MODEL_PATH = "/assets/spd/geo/mold_zombie.geo.json";
    private static final String ANIMATION_PATH = "/assets/spd/animations/mold_zombie.animation.json";
    private static final String MODEL_SHA256 = "0cf71839ad0d5dbe8d48f46562b682b248b09bf1a9bba212928839fa69ded3d0";

    @Test
    void modelMatchesTheSuppliedGeckoLibAsset() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(MODEL_PATH)) {
            assertNotNull(stream, MODEL_PATH);
            byte[] normalized = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n")
                    .getBytes(StandardCharsets.UTF_8);
            String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(normalized));

            assertEquals(MODEL_SHA256, actual);
        }
    }

    @Test
    void modelContainsEveryBoneUsedByItsAnimations() throws Exception {
        JsonObject model = resourceJson(MODEL_PATH);
        JsonObject animationFile = resourceJson(ANIMATION_PATH);
        Set<String> modelBones = new HashSet<>();
        for (JsonElement bone : model.getAsJsonArray("minecraft:geometry")
                .get(0).getAsJsonObject().getAsJsonArray("bones")) {
            modelBones.add(bone.getAsJsonObject().get("name").getAsString());
        }

        JsonObject animations = animationFile.getAsJsonObject("animations");
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

    private JsonObject resourceJson(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
