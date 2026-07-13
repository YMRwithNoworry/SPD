package alku.spd.entity;

import com.google.gson.JsonElement;
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
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BigEyesVariantAssetTest {
    private static final Variant WOLF = new Variant(
            "/assets/spd/geo/abyssal_wolf.geo.json",
            "/assets/spd/geo/abyssal_wolf_big_eyes.geo.json",
            "/assets/spd/textures/entity/abyssal_wolf_big_eyes.png",
            "c828c195c66bfb1f5245ee24febb0d9edf3d654b3e03dcb768ce703604252025",
            "05136fc31e114279288004692025701065410eb3685de03f0746bc540a57dae9",
            64, 64);
    private static final Variant FOX = new Variant(
            "/assets/spd/geo/abyssal_fox.geo.json",
            "/assets/spd/geo/abyssal_fox_big_eyes.geo.json",
            "/assets/spd/textures/entity/abyssal_fox_big_eyes.png",
            "d453542b5be0904b4b7612340fea658c57938656968524783df60774b9cb3c7f",
            "7a12274ff5c94001f7bd80f1b33190d8c78790fb7e7052deaa86838b678b41af",
            64, 64);
    private static final Variant TURTLE = new Variant(
            "/assets/spd/geo/abyssal_turtle.geo.json",
            "/assets/spd/geo/abyssal_turtle_big_eyes.geo.json",
            "/assets/spd/textures/entity/abyssal_turtle_big_eyes.png",
            "aae97a48742e4eaded5b8c728ee7d71834fd8f7bf4c6ccf8bb0e6769ad060bba",
            "5db1b76a7075b3e9e6ef93b61e7ce90acc942059eb9eb4608287504773d77d81",
            128, 128);
    private static final Variant MOLD_ZOMBIE = new Variant(
            "/assets/spd/geo/mold_zombie.geo.json",
            "/assets/spd/geo/mold_zombie_big_eyes.geo.json",
            "/assets/spd/textures/entity/mold_zombie_big_eyes.png",
            "69845e4e6127c0b28de6b839167d17b818a5478da422a2c8c2af69fa779e5f65",
            "0ba00f403b86ddc51740d14daf4d8d21bb2c2c3e343885f37971e9d4d61641c3",
            128, 128);

    @Test
    void packagesSuppliedWolfVariant() throws Exception {
        assertVariant(WOLF);
    }

    @Test
    void packagesSuppliedFoxVariant() throws Exception {
        assertVariant(FOX);
    }

    @Test
    void packagesSuppliedTurtleVariant() throws Exception {
        assertVariant(TURTLE);
    }

    @Test
    void packagesSuppliedMoldZombieVariant() throws Exception {
        assertVariant(MOLD_ZOMBIE);
    }

    private void assertVariant(Variant variant) throws Exception {
        byte[] modelBytes = resourceBytes(variant.variantModel());
        byte[] normalizedModelBytes = new String(modelBytes, StandardCharsets.UTF_8)
                .replace("\r\n", "\n")
                .getBytes(StandardCharsets.UTF_8);
        assertEquals(variant.modelSha256(), sha256(normalizedModelBytes));

        JsonObject normalModel = resourceJson(variant.normalModel());
        JsonObject bigEyesModel = JsonParser.parseString(new String(normalizedModelBytes, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(modelBones(normalModel), modelBones(bigEyesModel));

        byte[] textureBytes = resourceBytes(variant.texture());
        assertEquals(variant.textureSha256(), sha256(textureBytes));
        BufferedImage texture = ImageIO.read(new ByteArrayInputStream(textureBytes));
        assertNotNull(texture, variant.texture());
        assertEquals(variant.textureWidth(), texture.getWidth());
        assertEquals(variant.textureHeight(), texture.getHeight());
    }

    private static Set<String> modelBones(JsonObject model) {
        Set<String> bones = new HashSet<>();
        for (JsonElement bone : model.getAsJsonArray("minecraft:geometry")
                .get(0).getAsJsonObject().getAsJsonArray("bones")) {
            bones.add(bone.getAsJsonObject().get("name").getAsString());
        }
        assertTrue(bones.size() > 1);
        return bones;
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

    private record Variant(String normalModel, String variantModel, String texture,
                           String modelSha256, String textureSha256,
                           int textureWidth, int textureHeight) {
    }
}
