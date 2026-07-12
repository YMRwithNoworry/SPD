package alku.spd.world;

import alku.spd.registry.SpdBiomes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.world.level.biome.Biome;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AbyssalCoastWorldgenTest {
    private static final Set<String> BIOMES = Set.of(
            "spd:abyssal_coast",
            "spd:fungal_shallows",
            "spd:chrome_seabed_caves");

    @Test
    void biomeKeysExposeAllThreeEcosystemBiomes() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        assertBiomeKey(SpdBiomes.ABYSSAL_COAST, "abyssal_coast");
        assertBiomeKey(SpdBiomes.FUNGAL_SHALLOWS, "fungal_shallows");
        assertBiomeKey(SpdBiomes.CHROME_SEABED_CAVES, "chrome_seabed_caves");
    }

    @Test
    void biomeResourcesParseWithExpectedWaterAndVanillaGeneration() throws Exception {
        JsonObject coast = biome("abyssal_coast");
        JsonObject shallows = biome("fungal_shallows");
        JsonObject caves = biome("chrome_seabed_caves");

        assertWater(coast);
        assertWater(shallows);
        assertTrue(coast.getAsJsonObject("carvers").getAsJsonArray("air").size() > 0);
        assertTrue(shallows.getAsJsonObject("carvers").getAsJsonArray("air").size() > 0);
        assertTrue(caves.getAsJsonObject("carvers").getAsJsonArray("air").size() > 0);
        assertEquals(11, coast.getAsJsonArray("features").size());
        assertEquals(11, shallows.getAsJsonArray("features").size());
        assertEquals(11, caves.getAsJsonArray("features").size());
        assertFalse(caves.toString().contains("ore_blazing_vein_chrome_caves"),
                "Task 1 must not reference the Task 2 ore feature");
    }

    @Test
    void ecosystemBiomesBelongToAllRequiredTags() throws Exception {
        assertTagContainsAll("/data/minecraft/tags/worldgen/biome/is_overworld.json");
        assertTagContainsAll("/data/spd/tags/worldgen/biome/is_abyssal_blood_desert.json");
        assertTagContainsAll("/data/spd/tags/worldgen/biome/abyssal_turtle_spawns.json");
    }

    @Test
    void terraBlenderMapsTheThreeClimatesAndKeepsBloodDesertRules() throws Exception {
        String region = source("world/AbyssalCoastRegion.java");
        String terraBlender = source("world/SpdTerraBlender.java");

        assertTrue(region.contains("SpdBiomes.ABYSSAL_COAST"));
        assertTrue(region.contains("SpdBiomes.FUNGAL_SHALLOWS"));
        assertTrue(region.contains("SpdBiomes.CHROME_SEABED_CAVES"));
        assertTrue(region.contains("Climate.Parameter.span(-0.19F, -0.11F)"), "coast needs low continentalness");
        assertTrue(region.contains("Climate.Parameter.span(-1.0F, -0.46F)"), "shallows need ocean continentalness");
        assertTrue(region.contains("Climate.Parameter.span(-1.0F, -0.1F)"), "caves need negative depth");

        assertTrue(terraBlender.contains("ABYSSAL_COAST_REGION_WEIGHT = 10"));
        assertTrue(terraBlender.contains("new AbyssalCoastRegion"));
        assertTrue(terraBlender.contains("SpdBlocks.ABYSSAL_BLOOD_SAND"));
        assertTrue(terraBlender.contains("SpdBlocks.SACRED_STIGMA"));
        assertTrue(terraBlender.contains("SpdBiomes.ABYSSAL_COAST"));
        assertTrue(terraBlender.contains("SpdBiomes.FUNGAL_SHALLOWS"));
        assertTrue(terraBlender.contains("SpdBiomes.CHROME_SEABED_CAVES"));
        assertTrue(terraBlender.contains("SurfaceRules.isBiome(SpdBiomes.ABYSSAL_BLOOD_DESERT)"));
    }

    private void assertBiomeKey(ResourceKey<Biome> key, String path) {
        assertEquals("spd", key.location().getNamespace());
        assertEquals(path, key.location().getPath());
    }

    private void assertWater(JsonObject biome) {
        JsonObject effects = biome.getAsJsonObject("effects");
        assertEquals(2754315, effects.get("water_color").getAsInt());
        assertEquals(1116168, effects.get("water_fog_color").getAsInt());
    }

    private void assertTagContainsAll(String path) throws Exception {
        JsonArray values = resourceJson(path).getAsJsonArray("values");
        Set<String> ids = values.asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        assertTrue(ids.containsAll(BIOMES), path + " is missing " + difference(ids));
    }

    private Set<String> difference(Set<String> actual) {
        return BIOMES.stream().filter(id -> !actual.contains(id)).collect(Collectors.toSet());
    }

    private JsonObject biome(String id) throws Exception {
        return resourceJson("/data/spd/worldgen/biome/" + id + ".json");
    }

    private JsonObject resourceJson(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private String source(String relativePath) throws Exception {
        return Files.readString(Path.of("src/main/java/alku/spd", relativePath), StandardCharsets.UTF_8);
    }
}
