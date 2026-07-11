package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.recipe.CultureMediumRecipe;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class SpdRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Spd.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<RecipeSerializer<CultureMediumRecipe>> CULTURE_MEDIUM =
            SERIALIZERS.register("culture_medium", () -> new SimpleCraftingRecipeSerializer<>(CultureMediumRecipe::new));

    private SpdRecipeSerializers() {
    }

    public static void register() {
        SERIALIZERS.register();
    }
}
