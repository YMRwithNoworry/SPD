package alku.spd.forge.client;

import alku.spd.Spd;
import alku.spd.recipe.CultureMediumRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;

import java.util.List;

@JeiPlugin
public final class SpdJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(Spd.MOD_ID, "jei_plugin");
    private static final ResourceLocation CULTURE_MEDIUM_RECIPE_ID =
            new ResourceLocation(Spd.MOD_ID, "culture_medium");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                RecipeTypes.CRAFTING,
                List.of(new CultureMediumRecipe(CULTURE_MEDIUM_RECIPE_ID, CraftingBookCategory.MISC)));
    }
}
