package alku.spd.forge.client;

import mezz.jei.api.IModPlugin;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SpdJeiPluginTest {
    @Test
    void declaresCultureMediumCraftingRegistration() throws Exception {
        Class<?> pluginClass = Class.forName("alku.spd.forge.client.SpdJeiPlugin");
        assertTrue(IModPlugin.class.isAssignableFrom(pluginClass));
        try (InputStream classFile = pluginClass.getResourceAsStream("/alku/spd/forge/client/SpdJeiPlugin.class")) {
            assertNotNull(classFile);
            String bytecode = new String(classFile.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertTrue(bytecode.contains("Lmezz/jei/api/JeiPlugin;"));
            assertTrue(bytecode.contains("mezz/jei/api/constants/RecipeTypes"));
            assertTrue(bytecode.contains("alku/spd/recipe/CultureMediumRecipe"));
            assertTrue(bytecode.contains("addRecipes"));
        }

        Constructor<?> constructor = pluginClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        IModPlugin plugin = (IModPlugin) constructor.newInstance();

        assertEquals(new ResourceLocation("spd", "jei_plugin"), plugin.getPluginUid());
    }
}
