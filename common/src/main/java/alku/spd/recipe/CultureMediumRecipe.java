package alku.spd.recipe;

import alku.spd.registry.SpdItems;
import alku.spd.registry.SpdRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class CultureMediumRecipe extends CustomRecipe {
    private static final Ingredient GLASS_INGREDIENT = Ingredient.of(Items.GLASS);
    private static final Ingredient SUGAR_INGREDIENT = Ingredient.of(Items.SUGAR);
    private static final Ingredient WATER_BOTTLE_INGREDIENT = Ingredient.of(
            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));

    public CultureMediumRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean glass = false;
        boolean sugar = false;
        boolean waterBottle = false;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.GLASS) && !glass) {
                glass = true;
            } else if (stack.is(Items.SUGAR) && !sugar) {
                sugar = true;
            } else if (isWaterBottle(stack) && !waterBottle) {
                waterBottle = true;
            } else {
                return false;
            }
        }
        return glass && sugar && waterBottle;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, net.minecraft.core.RegistryAccess registryAccess) {
        return SpdItems.CULTURE_MEDIUM.get().getDefaultInstance();
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return SpdItems.CULTURE_MEDIUM.get().getDefaultInstance();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.withSize(3, Ingredient.EMPTY);
        ingredients.set(0, GLASS_INGREDIENT);
        ingredients.set(1, SUGAR_INGREDIENT);
        ingredients.set(2, WATER_BOTTLE_INGREDIENT);
        return ingredients;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (isWaterBottle(stack)) {
                remaining.set(slot, Items.GLASS_BOTTLE.getDefaultInstance());
            }
        }
        return remaining;
    }

    private static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SpdRecipeSerializers.CULTURE_MEDIUM.get();
    }
}
